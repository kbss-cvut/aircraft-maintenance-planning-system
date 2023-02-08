package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.exceptions.NotFoundException;
import cz.cvut.kbss.amaplas.exceptions.UnsupportedOperationException;
import cz.cvut.kbss.amaplas.exceptions.ValidationException;
import cz.cvut.kbss.amaplas.model.*;
import cz.cvut.kbss.amaplas.model.builders.PlanBuilderInput;
import cz.cvut.kbss.amaplas.model.builders.TaskTypeBasedPlanBuilder;
import cz.cvut.kbss.amaplas.model.scheduler.NaivePlanScheduler;
import cz.cvut.kbss.amaplas.model.scheduler.PlanScheduler;
import cz.cvut.kbss.amaplas.model.builders.WorkSessionBasedPlanBuilder;
import cz.cvut.kbss.amaplas.model.ops.CopySimplePlanProperties;
import cz.cvut.kbss.amaplas.model.scheduler.SimilarPlanScheduler;
import cz.cvut.kbss.amaplas.persistence.dao.GenericPlanDao;
import cz.cvut.kbss.amaplas.persistence.dao.PlanTypeDao;
import cz.cvut.kbss.amaplas.algs.SimilarityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cz.cvut.kbss.amaplas.planners.*;
import java.net.URI;
import java.time.ZoneId;
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

    public GenericPlanDao getPlanDao() {
        return planDao;
    }

    public Map<String, PlanGraph> scopeGraphPlansFromSimilarRevisions(List<TaskType> toPlan, Collection<String> revisionsToIgnore){

        // create a plan graph for each scope
        Map<String, List<TaskType>> tasksByScope = toPlan.stream().collect(Collectors.groupingBy(t ->  t.getScope()));

        Set<String> revsToIgnoreSet = new HashSet<>(revisionsToIgnore);
        Map<String, List<Result>> historyPlans = revisionHistory.getMainScopeSessionsByRevisionId();
        revisionsToIgnore.forEach(historyPlans::remove);

        Map<String, PlanGraph> scopePlans = new HashMap<>();
        for(Map.Entry<String, List<TaskType>> scopeTasks : tasksByScope.entrySet()){
            // the result does not contain information regarding instances of the TC execution
            PlanGraph rawScopePlan = ReuseBasedPlanner.planner.planConnected(
                    historyPlans,
                    new HashSet<>(scopeTasks.getValue()),
                    SimilarityUtils::calculateSetSimilarity,
                    revisionId -> revsToIgnoreSet.contains(revisionId));
            scopePlans.put(scopeTasks.getKey(), rawScopePlan);
        }

        return scopePlans;
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
        Workpackage workpackage = revisionHistory.getWorkpackage(revisionId);

        // create and return revision plan
        WorkSessionBasedPlanBuilder builder = new WorkSessionBasedPlanBuilder();
        RevisionPlan revisionPlan = builder.createRevision(new PlanBuilderInput<>(results, workpackage));
        PlanScheduler planScheduler = new NaivePlanScheduler();
        planScheduler.schedule(revisionPlan);

        builder.addRestrictionPlans(revisionPlan);

        return revisionPlan;
    }

    /**
     * Create a RevisionPlan from task types and work sessions of workpackage with revisionId.
     * @param revisionId
     * @return
     */
    public RevisionPlan createRevisionPlanScheduleDeducedFromSimilarRevisions(String revisionId){
        List<Result> results = revisionHistory.getClosedRevisionWorkLog(revisionId);
        List<TaskType> taskTypes = results.stream().map(r -> r.taskType).distinct().collect(Collectors.toList());
        Workpackage wp = revisionHistory.getWorkpackage(revisionId);

        WorkSessionBasedPlanBuilder workSessionBasedPlanBuilder = new WorkSessionBasedPlanBuilder();
        RevisionPlan revisionPlan = workSessionBasedPlanBuilder.createRevision(new PlanBuilderInput<>(results, wp));
        TaskTypeBasedPlanBuilder builder = new TaskTypeBasedPlanBuilder(workSessionBasedPlanBuilder);

        builder.addMissingTaskPlansToRevision(new PlanBuilderInput<>(taskTypes, wp), revisionPlan);

        // scheduling task plans
        // first schedule according to similar plans starting at the planned start date of the workpackage
        // scheduling is done per scope group using the partial order of task types extracted from similar plans
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Map<String, PlanGraph> partialTaskOrderByScope = scopeGraphPlansFromSimilarRevisions(
                taskTypes,
                Arrays.asList(revisionId)
        );
        SimilarPlanScheduler similarPlanScheduler = new SimilarPlanScheduler(
                Date.from(wp.getPlannedStartTime().atStartOfDay(defaultZoneId).toInstant()),
                partialTaskOrderByScope
        );
        similarPlanScheduler.schedule(revisionPlan);

        // second schedule work session plans and reschedule affected plans in the plan partonomy
        NaivePlanScheduler scheduler = new NaivePlanScheduler();
        scheduler.schedule(revisionPlan);
        revisionPlan.applyOperationBottomUp( p -> p.updateTemporalAttributes());
        workSessionBasedPlanBuilder.addRestrictionPlans(revisionPlan);

        return revisionPlan;
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
