package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.controller.dto.EntityReferenceDTO;
import cz.cvut.kbss.amaplas.controller.dto.RelationDTO;
import cz.cvut.kbss.amaplas.exceptions.ValidationException;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.ExtractData;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.ToGraphml;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.*;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.builders.ImplicitPlanBuilder;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.ops.CopySimplePlanProperties;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.planners.OriginalPlanner;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.planners.ReuseBasedPlanner;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.planners.TaskTypePlanValidator;
import cz.cvut.kbss.amaplas.persistence.dao.GenericPlanDao;
import cz.cvut.kbss.amaplas.persistence.dao.PlanTypeDao;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AircraftRevisionPlannerService extends BaseService{

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
                ExtractData::calculateSetSimilarity,
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
//                                .stream().mapToDouble(i -> i.getLength()).sum()/1000/3600) // in hours
        );

        tes.entrySet().forEach(e ->
                workTime.put(
                        e.getKey(),
                        e.getValue().stream().mapToLong(r -> r.dur).sum()) // in milli seconds
//                        e.getValue().stream().mapToDouble(r -> r.dur).sum()/ 1000/3600) // in hours
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
        // get all work sessions of the planned revision
        List<Result> results = revisionHistory.getClosedRevisionWorkLog(revisionId);

        // find the start date-time of the first work session of the revision
        OptionalLong startTimeStamp = results.stream().mapToLong(r -> r.getStart())
                .min();
        Date revisionStartDate = null;
        if(startTimeStamp.isPresent())
            revisionStartDate = new Date(startTimeStamp.getAsLong());

        // create and return revision plan
        RevisionPlan revisionPlan = createRevision(revisionId, results, revisionStartDate);

        // deduce schedule from revision work sessions
        // 1.a Create session schedules - from session logs
        revisionPlan.streamPlanParts()
                .filter(p -> p instanceof SessionPlan)
                .map(p -> (SessionPlan)p)
                .forEach(sp -> {
                    long duration = sp.getEndTime().getTime() - sp.getStartTime().getTime();
                    sp.setDuration(duration);
                    sp.setWorkTime(duration);
                    sp.setPlannedStartTime(sp.getStartTime());
                    sp.setPlannedEndTime(sp.getEndTime());
                    sp.setPlannedDuration(duration);
                    sp.setPlannedWorkTime(duration);
                });
//        // 1.b Create session schedules - fix non defined session logs
//        revisionPlan.streamPlanParts()
//                .filter(p -> p instanceof SessionPlan)
        // 2. update plan parts bottom up
        revisionPlan.applyOperationBottomUp( p -> p.updateTemporalAttributes());

        // 3. simplify plan
//        revisionPlan.applyOperationBottomUp( p -> {
//            if(p instanceof TaskPlan)
//                p.setPlanParts(new HashSet<>());
//        });
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
                ExtractData::calculateSetSimilarity,
                rid -> revisionToIgnore.contains(rid));
        // distance between task cards

        DefaultDirectedGraph<TaskType, SequencePattern> dg = ToGraphml.toGraph(rawPlan);
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
    public AbstractPlan getPlan(EntityReferenceDTO entityReferenceDTO){
        return getPlan(entityReferenceDTO.getEntityURI());
    }

    @Transactional
    public AbstractPlan getPlan(URI uri){
        return planDao.find(uri).get();
    }

    @Transactional
    public void addPlanPart(RelationDTO relationDTO) {
        AbstractComplexPlan planWhole = (AbstractComplexPlan)planDao.find(relationDTO.getLeft()).get();
        AbstractPlan planPart = planDao.find(relationDTO.getRight()).get();
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
    public void deletePlanPart(RelationDTO relationDTO){
        AbstractComplexPlan planWhole = (AbstractComplexPlan)planDao.find(relationDTO.getLeft()).get();
        AbstractPlan planPart = planDao.find(relationDTO.getRight()).get();
        deletePlanPart(planWhole, planPart);
    }

    @Transactional
    public void deletePlanPart(AbstractComplexPlan planWhole, AbstractPlan planPart){
        if(planWhole.getPlanParts() != null) {
            planWhole.getPlanParts().remove(planPart);
        }

        planDao.update(planWhole);
    }

    public void deletePlan(EntityReferenceDTO entityReferenceDTO){
        AbstractPlan plan = planDao.find(entityReferenceDTO.getEntityURI()).get();
        if(plan != null)
            planDao.remove(plan);
    }

    @Transactional
    public void updatePlanSimpleProperties(AbstractPlan plan){
        verifyEntityHasId(plan);
        verifyPlanType(plan);

        // update only simple properties.
        AbstractPlan oldPlan = planDao.find(plan.getEntityURI()).orElse(null);

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

//    public static void main(String[] args) {
//        List<Result> results = new ArrayList<>();
//        results.add(new Result());
//        results.add(new Result());
//        results.add(new Result());
//        results.get(0).wp = "1";
//        results.get(1).wp = "2";
//        results.get(2).wp = "3";
//        List<Result> resultCopy = new ArrayList(results);
//        results.sort(Comparator.comparing((Result r) -> r.wp).reversed());
//        for(Result r : results){
//            System.out.println(r.wp);
//        }
//        for(Result r : resultCopy){
//            System.out.println(r.wp);
//        }
//    }
}
