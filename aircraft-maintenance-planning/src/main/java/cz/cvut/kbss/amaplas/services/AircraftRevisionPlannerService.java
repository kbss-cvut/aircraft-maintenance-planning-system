package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.controller.dto.RevisionPlanCSVConverter;
import cz.cvut.kbss.amaplas.exceptions.NotFoundException;
import cz.cvut.kbss.amaplas.exceptions.UnsupportedOperationException;
import cz.cvut.kbss.amaplas.exceptions.ValidationException;
import cz.cvut.kbss.amaplas.model.*;
import cz.cvut.kbss.amaplas.model.builders.TaskTypeBasedPlanBuilder;
import cz.cvut.kbss.amaplas.model.builders.WorkSessionBasedPlanBuilder;
import cz.cvut.kbss.amaplas.model.ops.CopySimplePlanProperties;
import cz.cvut.kbss.amaplas.model.scheduler.NaivePlanScheduler;
import cz.cvut.kbss.amaplas.model.scheduler.PlanScheduler;
import cz.cvut.kbss.amaplas.model.scheduler.SimilarPlanScheduler;
import cz.cvut.kbss.amaplas.model.values.DateUtils;
import cz.cvut.kbss.amaplas.persistence.dao.GenericPlanDao;
import cz.cvut.kbss.amaplas.persistence.dao.PlanTypeDao;
import cz.cvut.kbss.amaplas.persistence.dao.TaskStepPlanDao;
import cz.cvut.kbss.amaplas.planners.PlanGraph;
import cz.cvut.kbss.amaplas.planners.ReuseBasedPlanner;
import cz.cvut.kbss.amaplas.utils.GraphmlUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class AircraftRevisionPlannerService extends BaseService{
    private static final Logger LOG = LoggerFactory.getLogger(AircraftRevisionPlannerService.class);

    private final WorkpackageService workpackageService;
    private final PlanTypeDao planTypeDao;
    private final GenericPlanDao planDao;
    private final TaskStepPlanDao taskStepPlanDao;
    private final CopySimplePlanProperties copySimpleProperty = new CopySimplePlanProperties();

    public AircraftRevisionPlannerService(WorkpackageService workpackageService, PlanTypeDao planTypeDao, GenericPlanDao planDao, TaskStepPlanDao taskStepPlanDao) {
        this.workpackageService = workpackageService;
        this.planTypeDao = planTypeDao;
        this.planDao = planDao;
        this.taskStepPlanDao = taskStepPlanDao;
    }

    public GenericPlanDao getPlanDao() {
        return planDao;
    }

    public Map<String, PlanGraph> scopeGraphPlansFromSimilarRevisions(Workpackage toPlan, Collection<String> revisionsToIgnore) {
        MaintenanceGroup nullGroup = new MaintenanceGroup();
        // create a plan graph for each scope
        Map<MaintenanceGroup, List<TaskType>> tasksByScope = toPlan.getTaskTypes().stream().collect(Collectors.groupingBy(t ->  t.getScope() != null ? t.getScope() : nullGroup));

        List<Pair<Workpackage, Double>> _similarWPs = workpackageService.findSimilarWorkpackages(toPlan);
        // get Similar WPs
        List<Pair<Supplier<Workpackage>, Double>> similarWPs = _similarWPs
                .stream()
                .filter(p -> !revisionsToIgnore.contains(p.getKey().getEntityURI().toString()))
                .map(p -> Pair.of(
                        (Supplier<Workpackage>)() -> workpackageService.getWorkpackageWithTemporalProperties(p.getKey().getEntityURI()),
                        p.getRight())
                )
                .collect(Collectors.toList());

        Map<String, PlanGraph> scopePlans = new HashMap<>();
        for(Map.Entry<MaintenanceGroup, List<TaskType>> scopeTasks : tasksByScope.entrySet()){
            // the result does not contain information regarding instances of the TC execution
            PlanGraph rawScopePlan = ReuseBasedPlanner.planner.planConnected(
                    similarWPs,
                    new HashSet<>(scopeTasks.getValue())
            );
            scopePlans.put(scopeTasks.getKey().getAbbreviation(), rawScopePlan);
        }

        return scopePlans;
    }

    public Map<String, PlanGraph> scopeGraphPlansFromSimilarScopes(Workpackage toPlan, Collection<String> revisionsToIgnore) {
        // find similar wps for each scope group
        MaintenanceGroup nullGroup = new MaintenanceGroup();
        // create a plan graph for each scope
        Map<MaintenanceGroup, List<TaskType>> tasksByScope = toPlan.getTaskTypes().stream()
//                .filter(t -> (t.getTaskcat() != null && t.getTaskcat().contains("task-card")) )
                .collect(Collectors.groupingBy(t ->  t.getScope() != null ? t.getScope() : nullGroup));

        Map<String, PlanGraph> scopePlans = new HashMap<>();
        for(Map.Entry<MaintenanceGroup, List<TaskType>> scopeTasks : tasksByScope.entrySet()){
            List<Pair<Workpackage, Double>> _similarWPs = workpackageService
                    .findWorkpackagesWithSimilarScopes(toPlan, new HashSet<>(scopeTasks.getValue()));
            List<Pair<Supplier<Workpackage>, Double>> similarWPs = _similarWPs
                    .stream()
                    .filter(p -> !revisionsToIgnore.contains(p.getKey().getEntityURI().toString()))
                    .map(p -> Pair.of(
                            (Supplier<Workpackage>)() -> workpackageService.getWorkpackageWithTemporalProperties(p.getKey().getEntityURI()),
                            p.getRight())
                    )
                    .collect(Collectors.toList());

            // the result does not contain information regarding instances of the TC execution
            PlanGraph rawScopePlan = ReuseBasedPlanner.planner.planConnected(
                    similarWPs,
                    new HashSet<>(scopeTasks.getValue())
            );
            scopePlans.put(scopeTasks.getKey().getAbbreviation(), rawScopePlan);
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

    /**
     * Create a revision plan from an existing revision execution. Each plan part is defined based on the existing work
     * log of the revision with revisionId.
     * @param revisionId
     * @return
     */
    public RevisionPlan createRevisionPlanScheduleDeducedFromRevisionExecution(String revisionId){
        LOG.info("creating plan \"createRevisionPlanScheduleDeducedFromRevisionExecution\" for revision with id \"{}\"", revisionId);
        // get all work sessions of the planned revision
        Workpackage workpackage = workpackageService.getWorkpackage(revisionId);
        workpackageService.readTaskExecutions(workpackage);
        // create and return revision plan
        WorkSessionBasedPlanBuilder builder = new WorkSessionBasedPlanBuilder();
        RevisionPlan revisionPlan = builder.createRevision(workpackage);
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
    public RevisionPlan createRevisionPlanScheduleDeducedFromSimilarRevisions(String revisionId, boolean mixedSchedule) {
        Workpackage wp = workpackageService.getWorkpackageWithExecutionsAndSessions(revisionId);
        if(wp == null)
            return null;
        return createRevisionPlanScheduleDeducedFromSimilarRevisions(wp, mixedSchedule);
    }

    public RevisionPlan createRevisionPlanScheduleDeducedFromSimilarRevisions(Workpackage wp, boolean mixedSchedule) {

//        Set<TaskType> taskTypes = wp.getTaskTypes();

        WorkSessionBasedPlanBuilder workSessionBasedPlanBuilder = new WorkSessionBasedPlanBuilder();
        RevisionPlan revisionPlan = workSessionBasedPlanBuilder.createRevision(wp);
        TaskTypeBasedPlanBuilder builder = new TaskTypeBasedPlanBuilder(workSessionBasedPlanBuilder);

        builder.addMissingTaskPlansToRevision(wp, revisionPlan);

        List<TaskStepPlan> steps = taskStepPlanDao.listInWorkpackage(wp.getEntityURI());
        workSessionBasedPlanBuilder.addTaskSteps(revisionPlan, steps);

        // scheduling task plans
        // first schedule according to similar plans starting at the planned start date of the workpackage
        // scheduling is done per scope group using the partial order of task types extracted from similar plans
        List<String> revisionsToIgnore = new ArrayList<>();
        revisionsToIgnore.add(wp.getEntityURI().toString()); // ignore the scheduled WP
        workpackageService.getOpenedWorkpackages().stream().map(w -> w.getEntityURI().toString())
                .forEach(revisionsToIgnore::add);// ignore open WPs

        Map<String, PlanGraph> partialTaskOrderByScope = scopeGraphPlansFromSimilarRevisions(
                wp,
                revisionsToIgnore
        );
        // debug - print plan graphs in files
        for(Map.Entry<String, PlanGraph> e : partialTaskOrderByScope.entrySet()){
            String fileName = String.format("%s--%s--%s.gml",
                    wp.getId().replaceAll("[^\\w\\d]", "-"),
                    e.getKey(),
                    DateUtils.formatDate(new Date()).replace(":", "-")
                    );
            GraphmlUtils.write(e.getValue(), PlanGraph.exporter, fileName);
        }

        // calculate execution start
        Date startDate = wp.getPlannedStartTime() != null ?
                DateUtils.toDateAtTime(wp.getPlannedStartTime(), 7, 0) :
                wp.getStart() != null ?
                        wp.getStart() :
                        new Date();


        SimilarPlanScheduler similarPlanScheduler = new SimilarPlanScheduler(
                startDate,
                partialTaskOrderByScope
        );
        similarPlanScheduler.schedule(revisionPlan);

        if (mixedSchedule){
            // second schedule work session plans and reschedule affected plans in the plan partonomy
            NaivePlanScheduler scheduler = new NaivePlanScheduler();
            scheduler.schedule(revisionPlan);
        }
        revisionPlan.applyOperationBottomUp(p -> p.updateTemporalAttributes());
        workSessionBasedPlanBuilder.addRestrictionPlans(revisionPlan);

        return revisionPlan;
    }

    public String compareCSATPlanAutomatedPlanAndExecutions(String revisionId){
        Workpackage wp = workpackageService.getWorkpackageWithExecutionsAndSessions(revisionId);

        if(wp == null)
            return null;
        RevisionPlan revisionPlan = createRevisionPlanScheduleDeducedFromSimilarRevisions(wp, false);
        RevisionPlanCSVConverter c = new RevisionPlanCSVConverter();
        return c.convert(wp, revisionPlan);
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
