package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.builders;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class ImplicitPlanBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ImplicitPlanBuilder.class);

    private final ModelFactory modelFactory = new ModelFactory();
    protected Map<Object, Map<String, AbstractEntity>> entityMaps = new HashMap<>();

    public PlanningResult createRevision(List<Result> results){
        Aircraft aircraft = results.stream().map(r -> getAircraft(r)).filter(a -> a != null).findFirst().orElse(null);
        // create bottom part of hierarchical plan -  create to task plans
        RevisionPlan revisionPlan = new RevisionPlan();
        String revisionCode = results.stream()
                .map(r -> r.wp)
                .filter(s -> s != null && !s.isEmpty())
                .findFirst().orElse(null);

        Long revisionId = revisionCode != null ? (long)revisionCode.hashCode() : null;
        revisionPlan.setTitle(revisionCode);
        revisionPlan.setId(revisionId);

        revisionPlan.setResource(aircraft);
//        List<TaskPlan> taskPlans = new ArrayList<>();
        PlanningResult result = new PlanningResult(revisionPlan);

        for(Result r : results) {
            PhasePlan phasePlan = getPhasePlan(r);
            revisionPlan.getPlanParts().add(phasePlan);

            GeneralTaskPlan generalTaskPlan = getGeneralTaskPlanInCtx(r, phasePlan);
            phasePlan.getPlanParts().add(generalTaskPlan);

            TaskPlan taskPlan = getTaskPlan(r);
            generalTaskPlan.getPlanParts().add(taskPlan);

            SessionPlan sessionPlan = getSessionPlan(r);
            taskPlan.getPlanParts().add(sessionPlan);

        }
        return result;
    }
//    public RevisionPlan createRevisionBottomUp(List<Result> results){
//
//        // create bottom part of hierarchical plan -  create to task plans
//        RevisionPlan revisionPlan = new RevisionPlan();
////        List<TaskPlan> taskPlans = new ArrayList<>();
//        for(Result r : results) {
//            SessionPlan sessionPlan = getSessionPlan(r);
//
//            TaskPlan taskPlan = getTaskPlan(r);
//            taskPlan.getPlanParts().add(sessionPlan);
//
//            GeneralTaskPlan generalTaskPlan = getGeneralTaskPlan(r);
//            generalTaskPlan.getPlanParts().add(taskPlan);
//
//            PhasePlan phasePlan = getPhasePlan(r);
//            phasePlan.getPlanParts().add(generalTaskPlan);
//            revisionPlan.getPlanParts().add(phasePlan);
//        }
//
//        return revisionPlan;
//    }

//    public RevisionPlan groupRevisionPla(RevisionPlan revisionPlan){
//        // break - mechanics, scopes,
//        return null;
//    }

    public SessionPlan getSessionPlan(Result r){
        SessionPlan sessionPlan = modelFactory.newSessionPlan(r.start, r.end);
        Mechanic mechanic = getMechanic(r);
        sessionPlan.setResource(mechanic);
        return sessionPlan;
    }

    public Mechanic getMechanic(Result r){
        String mechanicTitle = getMechanicLabel(r);
        return getEntity(
                mechanicTitle,
                "mechanic",
                () -> modelFactory.newMechanic(mechanicTitle)
        );
    }

    public MaintenanceGroup getMaintenanceGroup(Result r){
        String maintenanceGroupLabel = getMaintenanceGroupLabel(r);
        return getEntity(maintenanceGroupLabel, "maintenance-group", () -> modelFactory.newMaintenanceGroup(maintenanceGroupLabel));
    }

    public MaintenanceGroup getMaintenanceGroupInCtx(Result r, AircraftArea area){
        String maintenanceGroupLabel = getMaintenanceGroupLabel(r);
        MaintenanceGroup maintenanceGroup = getEntity(maintenanceGroupLabel, area, () -> modelFactory.newMaintenanceGroup(maintenanceGroupLabel));
        return maintenanceGroup;
    }

    public Mechanic getMechanicInCtx(Result r){
        try {

            String mechanicTitle = getMechanicLabel(r);
            String area = getAreaLabel(r);
            return getEntity(
                    mechanicTitle,
                    area,
                    () -> modelFactory.newMechanic(mechanicTitle)
            );
        }catch (Exception e){
            LOG.info("Error getting/creating mechanic", e);
        }
        return null;
    }

    public AircraftArea getAircraftArea(Result r){
        String area = getAreaLabel(r);
        return getEntity(area, "aircraft-area", () -> modelFactory.newAircraftArea(area));
    }

    public AircraftArea getAircraftAreaInCtx(Result r, PhasePlan phasePlan){
        String area = getAreaLabel(r);
        return getEntity(area, phasePlan, () -> modelFactory.newAircraftArea(area));
    }

    public Aircraft getAircraft(Result r){
        String aircraftModel = getAircraftModelLabel(r);
        return getEntity(aircraftModel, "aircraft", () -> modelFactory.newEntity(Aircraft.class, aircraftModel));
    }

    public TaskPlan getTaskPlan(Result r){
        TaskType taskTypeCode = getTaskType(r).orElse(null);

        AircraftArea area = getAircraftArea(r);

        TaskPlan taskPlan = null;
        if(taskTypeCode != null)
            taskPlan = getEntity(taskTypeCode.getCode(), "task-plan", () -> {
                TaskPlan p = modelFactory.newTaskPlan(taskTypeCode);
                MaintenanceGroup group = getMaintenanceGroupInCtx(r, area);
                p.setResource(group);
                return p;
            });
        else
            taskPlan = getEntity("", "task-plan", () -> {
                TaskPlan p = modelFactory.newTaskPlan("");
                MaintenanceGroup group = getMaintenanceGroupInCtx(r, area);
                p.setResource(group);
                return p;
            });

        return  taskPlan;
    }

    public GeneralTaskPlan getGeneralTaskPlan(Result r){
        String generalTaskType = getGeneralTaskTypeLabel(r);
        GeneralTaskPlan generalTaskPlan = getEntity(
                generalTaskType,
                "general-task-type",
                () -> createGeneralTasPlan(r, generalTaskType)
        );
        return generalTaskPlan;
    }

    public GeneralTaskPlan getGeneralTaskPlanInCtx(Result r, PhasePlan phasePlan){
        // general task plan <=> different session.type.type per session.type.area
        String generalTaskType = getGeneralTaskTypeLabel(r);
        GeneralTaskPlan generalTaskPlan = getEntity(
                generalTaskType,
                phasePlan,
                () -> createGeneralTasPlan(r, generalTaskType));
        return generalTaskPlan;
    }

    private GeneralTaskPlan createGeneralTasPlan(Result r, String generalTaskType){
        GeneralTaskPlan p = modelFactory.newGeneralTaskPlan(generalTaskType);
        AircraftArea area = getAircraftArea(r);
        p.setResource(area);
        return p;
    }

    public PhasePlan getPhasePlan(Result r){
        String phaseLabel = getPhaseLabel(r);
        PhasePlan phasePlan = getEntity(phaseLabel, "phase", () -> {
            PhasePlan p = modelFactory.newPhasePlan(phaseLabel);
            Aircraft aircraft = getAircraft(r);
            p.setResource(aircraft);
            return p;
        });
        return phasePlan;
    }



    // this is a simplification
    public String getMechanicLabel(Result r){
        return Optional.ofNullable(r.getMechanic()).map(Mechanic::getTitle).orElse(modelFactory.generateId() + "");
    }

    public String getAircraftModelLabel(Result r){
        return getTaskTypeDefinition(r).map(TaskType::getAcmodel).orElse("");
    }

    public String getAreaLabel(Result r){
        return getTaskTypeDefinition(r).map(TaskType::getArea).orElse("");
    }

    public String getMaintenanceGroupLabel(Result r){
        return getTaskTypeDefinition(r).map(TaskType::getScope).orElse("");
    }

    public String getGeneralTaskTypeLabel(Result r){
        return getTaskTypeDefinition(r).map(TaskType::getTaskType).orElse("");
    }

    public String getPhaseLabel(Result r){
        return getTaskTypeDefinition(r).map(TaskType::getPhase).orElse("");
    }

    public Optional<TaskType> getTaskType(Result r){
        return Optional.of(r.taskType);
    }

    public Optional<TaskType> getTaskTypeDefinition(Result r){
        return getTaskType(r).map(tt -> tt.getDefinition());
    }


    /**
     *
     * @param id - the id of the object to be fetched
     * @param context - equals and hash should be implemented to follow that two different instances with the same
     *               attribute are interpreted as equal and have the same hash code
     * @param generator
     * @param <T>
     * @return
     */
    public <T extends AbstractEntity> T getEntity(String id, Object context, Supplier<T> generator){
        Map<String, AbstractEntity> entityMap = entityMaps.get(context);
        if(entityMap == null) {
            entityMap = new HashMap<>();
            entityMaps.put(context, entityMap);
        }
        AbstractEntity r = entityMap.get(id);
        if(r == null) {
//                r = newResource(resourceClass, name);
            r = generator.get();
            entityMap.put(id, r);
        }
        return (T)r;
    }

    public static class PlanningResult{
        protected RevisionPlan revisionPlan;
        protected Map<Result, SessionPlan> sessionPlans = new HashMap<>();

        public PlanningResult(RevisionPlan revisionPlan) {
            this.revisionPlan = revisionPlan;
        }

        public RevisionPlan getRevisionPlan() {
            return revisionPlan;
        }

        public void setRevisionPlan(RevisionPlan revisionPlan) {
            this.revisionPlan = revisionPlan;
        }

        public Map<Result, SessionPlan> getSessionPlans() {
            return sessionPlans;
        }

        public void setSessionPlans(Map<Result, SessionPlan> sessionPlans) {
            this.sessionPlans = sessionPlans;
        }
    }

    public static void main(String[] args) {
        Set<String> s = new HashSet<>();
    }
}
