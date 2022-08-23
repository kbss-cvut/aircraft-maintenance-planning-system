package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.exceptions.NotFoundException;
import cz.cvut.kbss.amaplas.exceptions.UnsupportedOperationException;
import cz.cvut.kbss.amaplas.exceptions.ValidationException;
import cz.cvut.kbss.amaplas.model.*;
import cz.cvut.kbss.amaplas.utils.GraphmlUtils;
import cz.cvut.kbss.amaplas.model.builders.ImplicitPlanBuilder;
import cz.cvut.kbss.amaplas.model.ops.CopySimplePlanProperties;
import cz.cvut.kbss.amaplas.planners.OriginalPlanner;
import cz.cvut.kbss.amaplas.planners.ReuseBasedPlanner;
import cz.cvut.kbss.amaplas.planners.TaskTypePlanValidator;
import cz.cvut.kbss.amaplas.persistence.dao.GenericPlanDao;
import cz.cvut.kbss.amaplas.persistence.dao.PlanTypeDao;
import cz.cvut.kbss.amaplas.algs.SimilarityUtils;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AircraftRevisionPlannerService extends BaseService{
    private static final Logger LOG = LoggerFactory.getLogger(AircraftRevisionPlannerService.class);

    private final RevisionHistory revisionHistory;
    private final TaskTypeService taskTypeService;
    private final PlanTypeDao planTypeDao;
    private final GenericPlanDao planDao;
    private final CopySimplePlanProperties copySimpleProperty = new CopySimplePlanProperties();

    public AircraftRevisionPlannerService(RevisionHistory revisionHistory, TaskTypeService taskTypeService, PlanTypeDao planTypeDao, GenericPlanDao planDao) {
        this.revisionHistory = revisionHistory;
        this.taskTypeService = taskTypeService;
        this.planTypeDao = planTypeDao;
        this.planDao = planDao;
    }

    public List<SequencePattern> planTaskTypeCodes(List<String> toPlan){
        List<TaskType> taskTypes = taskTypeService.getTaskTypes(toPlan);
        return planTaskTypes(taskTypes, Collections.EMPTY_LIST);
    }

    public List<TaskPlan> planRevision(String revisionId){
        List<Result> workLog = revisionHistory.getClosedRevisionWorkLog(revisionId);
        if(workLog == null || workLog.isEmpty())
            return null;

        TaskTypePlanValidator validator = new TaskTypePlanValidator();

        List<SequencePattern> currentPlan = OriginalPlanner.planner.plan(workLog);
        validator.validate(currentPlan, workLog);

        List<TaskType> toPlan = workLog.stream().map(r -> r.taskType).distinct().collect(Collectors.toList());
        List<TaskType> orderedTasks = planTaskTypes(toPlan, Arrays.asList(revisionId));
        validator.validate(workLog.stream().map(r -> r.taskType).collect(Collectors.toSet()), new HashSet<>(orderedTasks));
        List<TaskPlan> plannedRevision = asPlan(orderedTasks);
        calculateTimeEstimates(plannedRevision, workLog);
        return plannedRevision;
    }


    public List<SequencePattern> sequencePatternsFromSimilarRevisions(List<TaskType> toPlan, Collection<String> revisionsToIgnore){
        Set<String> revsToIgnoreSet = new HashSet<>(revisionsToIgnore
        );
        Map<String, List<Result>> historyPlans = revisionHistory.getStartSessionsOfMainScopeInClosedRevisions();
//        // copy history
//        historyPlans = new HashMap<>( historyPlans);
//        // remove
//        revisionsToIgnore.forEach(historyPlans::remove);
        List<SequencePattern> rawPlan = ReuseBasedPlanner.planner.planConnected(historyPlans, new HashSet<>(toPlan),
                SimilarityUtils::calculateSetSimilarity,
                revisionId -> revsToIgnoreSet.contains(revisionId));
        return rawPlan;
    }

    /**
     * plans the task
     * @param toPlan
     */
    public List<TaskType> planTaskTypes(List<TaskType> toPlan, List<String> revisionsToIgnore){
        List<SequencePattern> rawPlan = sequencePatternsFromSimilarRevisions(toPlan, revisionsToIgnore);
        List<TaskType> orderedTasks = ReuseBasedPlanner.planner.flattenPartialOrderBreadthFirst(rawPlan);
        return orderedTasks;
    }

    public List<TaskPlan> asPlan(List<TaskType> orderedTasks){
        List<TaskPlan> plan = new ArrayList<>();
        for(TaskType tt : orderedTasks) {
            plan.add(new TaskPlan(tt));
        }
        return plan;
    }


    public void calculateTimeEstimates(List<TaskPlan> plan, List<Result> workLog){
        // TODO
        Map<TaskType, List<Result>> tes = workLog.stream().collect(Collectors.groupingBy(r -> r.taskType));

        List<Date> startTimes = tes.values().stream()
                .map(l -> l.stream().min(Comparator.comparing(r -> r.start)).map(r -> r.start).orElse(null))
                .sorted()
                .collect(Collectors.toList());

        Map<TaskType, Long> durations = new HashMap<>();
        Map<TaskType, Long> workTime = new HashMap<>();
        tes.entrySet().forEach(e ->
                durations.put(
                        e.getKey(),
                        Result.mergeOverlaps(e.getValue().stream().sorted(Comparator.comparing(r -> r.start)).collect(Collectors.toList()))
                                .stream().mapToLong(i -> i.getLength()).sum()) // in milli seconds
        );

        tes.entrySet().forEach(e ->
                workTime.put(
                        e.getKey(),
                        e.getValue().stream().mapToLong(r -> r.dur).sum()) // in milli seconds
        );

        int c = 0;
        for(TaskPlan tp : plan){
            List<Result> te = tes.get(tp.getTaskType());
            if(te == null)
                continue;
            tp.setStartTime(startTimes.get(c));
            tp.setDuration(durations.get(tp.getTaskType()));
            tp.setWorkTime(workTime.get(tp.getTaskType()));
        }
    }

    /**
     * Create a revision plan from an existing revision execution. Each plan part is defined based on the existing work
     * log of the revision with revisionId.
     * @param revisionId
     * @return
     */
    public RevisionPlan createRevisionPlanScheduleDeducedFromRevisionExecution(String revisionId){
        LOG.info("creating plan \"createRevisionPlanScheduleDeducedFromRevisionExecution\" for revision with id \"{}\"", revisionId);
        // get all work sessions of the planned revision
        List<Result> results = revisionHistory.getClosedRevisionWorkLog(revisionId);

        // find the start date-time of the first work session of the revision
        OptionalLong startTimeStamp = results.stream().filter(r -> r.start != null).mapToLong(r -> r.getStart())
                .min();
        OptionalLong endTimeStamp = results.stream().filter(r -> r.end != null).mapToLong(r -> r.getEnd())
                .max();
        Date revisionStartDate = startTimeStamp.stream().mapToObj(t -> new Date(t)).findFirst().orElse(new Date());
        Date revisionEndDate = endTimeStamp.stream().mapToObj(t -> new Date(t)).findFirst().orElse(new Date(revisionStartDate.getTime() + 7 * 24 * 60 * 60 * 1000));
        long tmpDuration = 3*60*60*1000l;
        Long defaultDuration = tmpDuration > revisionEndDate.getTime() - revisionStartDate.getTime() ?
                (int)((revisionEndDate.getTime() - revisionStartDate.getTime()) * 0.1)  : tmpDuration;
        Date defaultTaskPlanEnd = new Date(revisionStartDate.getTime() + defaultDuration);

        // create and return revision plan
        RevisionPlan revisionPlan = createRevision(revisionId, results, revisionStartDate);

        // deduce schedule from revision work sessions
        // 1.a Create session schedules - from session logs
        revisionPlan.streamPlanParts()
                .filter(p -> p instanceof SessionPlan)
                .map(p -> (SessionPlan)p)
                .forEach(sp -> {
                    if(sp.getStartTime() == null)
                        sp.setStartTime(revisionStartDate);

                    if(sp.getEndTime() == null)
                        sp.setEndTime(defaultTaskPlanEnd);
                    long duration = sp.getEndTime().getTime() - sp.getStartTime().getTime();
                    sp.setDuration(duration);
                    sp.setWorkTime(duration);
                    sp.setPlannedStartTime(sp.getStartTime());
                    sp.setPlannedEndTime(sp.getEndTime());
                    sp.setPlannedDuration(duration);
                    sp.setPlannedWorkTime(duration);
                });
        // 2. update plan parts bottom up
        revisionPlan.applyOperationBottomUp( p -> p.updateTemporalAttributes());
        return revisionPlan;
    }

    public RevisionPlan createRevisionPlanScheduleDeducedFromSimilarRevisions(String revisionId, Date startDate){
        List<Result> results = revisionHistory.getClosedRevisionWorkLog(revisionId);

        // create sessionPlans
        RevisionPlan revisionPlan = createRevision(revisionId, results, startDate);
        Set<TaskPlan> taskPlans = revisionPlan.getPlanParts().stream().flatMap(
                pp -> pp.getPlanParts().stream()
                        .flatMap(gtp -> gtp.getPlanParts().stream()
                        )).collect(Collectors.toSet());

//        List<TaskType> taskTypes = planTaskTypes(results.stream().map(r -> r.taskType).distinct().collect(Collectors.toList()), Arrays.asList(revisionId));

        List<SequencePattern> sequencePatterns = sequencePatternsFromSimilarRevisions(results.stream().map(r -> r.taskType).distinct().collect(Collectors.toList()), Arrays.asList(revisionId));
        // TODO - revise the instances supporting the SequencePatterns to create a temporal schedule of the task types.
        // 1. split the list of sequence patterns into streaks of sequence patterns from individual revisions.
        // -  session
        // 1. create chains of work sessions of the planned TCs grouped by the WP from which they are being reused.
        // 2. order work sessions based on sequence patterns.
        // return the work session list so that we can use it calculate the temporal parameters of the plan.
        // 1. starting with the first work session in the list translate the work interval from its original start date
        // to the specified start date. Repeat until all task cards are


        List<TaskType> taskTypes = ReuseBasedPlanner.planner.flattenPartialOrderBreadthFirst(sequencePatterns);

        Map<String, List<Result>> historyPlans = revisionHistory.getStartSessionsOfMainScopeInClosedRevisions();
//        // copy history
//        historyPlans = new HashMap<>( historyPlans);
//        // remove
//        revisionsToIgnore.forEach(historyPlans::remove);
        Set<String> revisionToIgnore = new HashSet<>();
        revisionToIgnore.add(revisionId);
        List<SequencePattern> rawPlan = ReuseBasedPlanner.planner.planConnected(historyPlans, new HashSet<>(taskTypes),
                SimilarityUtils::calculateSetSimilarity,
                rid -> revisionToIgnore.contains(rid));
        // distance between task cards

        DefaultDirectedGraph<TaskType, SequencePattern> dg = GraphmlUtils.toGraph(rawPlan);
        Set<TaskType> roots = taskTypes.stream().filter(t -> dg.inDegreeOf(t) == 0).collect(Collectors.toSet());
        BreadthFirstIterator<TaskType, SequencePattern> breadthFirstIterator = new BreadthFirstIterator(dg, roots);
        while(breadthFirstIterator.hasNext()){

        }
        List<TaskType> orderedTasks = ReuseBasedPlanner.planner.flattenPartialOrderBreadthFirst(rawPlan);

        // calculate temporal parameters of the plan

        return revisionPlan;
    }


    public RevisionPlan createRevision(String revisionId, List<Result> results, Date startDate){
        // TODO - move to loading task type definitions
        // set the task card definition for each task card.
        for(Result r : results) {
            if(r.taskType == null)
                continue;
            TaskType tt = taskTypeService.getMatchingTaskTypeDefinition(r.taskType);
            r.taskType.setDefinition(tt);
        }

        // build a RevisionPlan, i.e. a hierarchical object model of the plan
        ImplicitPlanBuilder builder = new ImplicitPlanBuilder();
        ImplicitPlanBuilder.PlanningResult planningResult = builder.createRevision(results);
        return planningResult.getRevisionPlan();
    }

    public void setTemporalParameters(RevisionPlan revisionPlan, List<SequencePattern> sequencePatterns){

    }

    public SessionPlan toImplicitSessionPlan(Mechanic m, Result result){

//        modelFactory.newSessionPlan()
        return null;
    }

    /**
     * creates a session plan based on the average plan length
     * @return
     */
    public SessionPlan toAverageSessionPlan(){
        return null;
    }

    @Transactional
    public AbstractPlan createPlan(URI planType){
        verifyPlanType(planType);
        AbstractPlan p = planTypeDao.getNewPlanTypeInstance(planType);
        planDao.persist(p);
        return p;
    }

    @Transactional
    public AbstractPlan createPlan(AbstractPlan plan){
        assert plan != null;
        verifyPlanType(plan);
        planDao.persist(plan);
        return plan;
    }

    @Transactional
    public AbstractPlan getPlan(URI uri){
        return planDao.find(uri).orElseThrow(() -> NotFoundException.create("plan", uri));
    }

    @Transactional
    public Collection<AbstractPlan> getPlanParts(URI planURI){
        AbstractPlan plan = getPlan(planURI);
        return getPlanParts(plan);
    }

    @Transactional
    public Collection<AbstractPlan> getPlanParts(AbstractPlan plan){
        if(!(plan instanceof AbstractComplexPlan)){
            throw new UnsupportedOperationException(String.format("Plan is atomic and does not have parts, plan %s", plan));
        }
        return (Set<AbstractPlan>)((AbstractComplexPlan<?>) plan).getPlanParts();
    }

    @Transactional
    public void addPlanPart(URI planWholeUri, URI planPartUri) {
        AbstractComplexPlan planWhole = (AbstractComplexPlan)getPlan(planWholeUri);
        AbstractPlan planPart = getPlan(planPartUri);
        addPlanPart(planWhole, planPart);
    }

    @Transactional
    public void addPlanPart(AbstractComplexPlan planWhole, AbstractPlan planPart){
        if(planWhole.getPlanParts() == null)
            planWhole.setPlanParts(new HashSet());
        planWhole.getPlanParts().add(planPart);

        planDao.update(planWhole);
    }

    @Transactional
    public void deletePlanPart(URI planWholeUri, URI planPartUri) {
        AbstractComplexPlan planWhole = (AbstractComplexPlan)getPlan(planWholeUri);
        AbstractPlan planPart = getPlan(planPartUri);
        deletePlanPart(planWhole, planPart);
    }

    @Transactional
    public void deletePlanPart(AbstractComplexPlan planWhole, AbstractPlan planPart){
        if(planWhole.getPlanParts() != null) {
            planWhole.getPlanParts().remove(planPart);
        }

        planDao.update(planWhole);
    }

    @Transactional
    public void deletePlan(URI planUri){
        AbstractPlan plan = getPlan(planUri);
        if(plan != null)
            planDao.remove(plan);
    }

    @Transactional
    public void updatePlanSimpleProperties(AbstractPlan plan){
        verifyEntityHasId(plan);
        verifyPlanType(plan);

        // update only simple properties.
        AbstractPlan oldPlan = getPlan(plan.getEntityURI());

        copySimpleProperty.copyTo(plan, oldPlan);
        planDao.update(oldPlan);
    }

    protected void verifyPlanType(URI planType){
        if(!planTypeDao.isSupportedPlanType(planType))
            throw new ValidationException(String.format(
                    "Invalid plan type, plan type not supported <%s>",
                    planType.toString())
            );
    }

    protected void verifyPlanType(AbstractPlan plan){
        Set<String> planTypes = planTypeDao.getTypes(plan);
        if(!planTypeDao.containsSupportedPlanType(planTypes))
            throw new ValidationException(String.format(
                    "Invalid plan types, non of the plan types is supported,\n %s",
                    planTypes)
            );
    }
}
