package cz.cvut.kbss.amaplas.model.builders;

import cz.cvut.kbss.amaplas.model.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ImplicitPlanBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ImplicitPlanBuilder.class);

    private final ModelFactory modelFactory = new ModelFactory();
    protected Map<Object, Map<String, AbstractEntity>> entityMaps = new HashMap<>();
    protected Defaults defaults = new Defaults();

    public PlanningResult createRevision(List<Result> results){
        Aircraft aircraft = results.stream().map(r -> getAircraft(r)).filter(a -> !defaults.isDefault(a)).findFirst().orElse(null);
        if(aircraft == null)
            aircraft = results.stream().map(r -> getAircraft(r)).findFirst().orElse(null);
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

        PlanningResult result = new PlanningResult(revisionPlan);

        for(Result r : results) {
            PhasePlan phasePlan = getPhasePlan(r, aircraft);
            revisionPlan.getPlanParts().add(phasePlan);

            AircraftArea area = getAircraftArea(r);
            Pair generalTaskPlanContext = Pair.of(phasePlan, area);
            GeneralTaskPlan generalTaskPlan = getGeneralTaskPlanInCtx(r, generalTaskPlanContext);
            phasePlan.getPlanParts().add(generalTaskPlan);

            TaskPlan taskPlan = getTaskPlan(r, generalTaskPlan);
            generalTaskPlan.getPlanParts().add(taskPlan);

            SessionPlan sessionPlan = getSessionPlan(r);
            taskPlan.getPlanParts().add(sessionPlan);

        }
        return result;
    }

    public SessionPlan getSessionPlan(Result r){
        SessionPlan sessionPlan = modelFactory.newSessionPlan(r.start, r.end);
        Mechanic mechanic = getMechanic(r);
        sessionPlan.setResource(mechanic);
        return sessionPlan;
    }

    public Mechanic getMechanic(Result r){
        Mechanic m = r.getMechanic();
        return m == null ?
                null :
                getEntity(
                    (String)m.getId(),
                    "mechanic",
                    () -> m
                );
    }

    public MaintenanceGroup getMaintenanceGroup(Result r){
        String maintenanceGroupLabel = getMaintenanceGroupLabel(r);
        return getEntity(maintenanceGroupLabel, "maintenance-group", () -> modelFactory.newMaintenanceGroup(maintenanceGroupLabel));
    }

    public MaintenanceGroup getMaintenanceGroupInCtx(Result r, AircraftArea area){
        String maintenanceGroupLabel = getMaintenanceGroupLabel(r);
        Pair pair = Pair.of("group", area);
        MaintenanceGroup maintenanceGroup = getEntity(maintenanceGroupLabel, pair, () -> modelFactory.newMaintenanceGroup(maintenanceGroupLabel));
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
        return getEntity(aircraftModel, "aircraft", () -> modelFactory.newEntity(() -> new Aircraft(), aircraftModel));
    }

    public TaskPlan getTaskPlan(final Result r, GeneralTaskPlan gp){
        TaskType taskType = getTaskType(r).orElse(null);

        final AircraftArea area = getAircraftArea(r);
        TaskPlan taskPlan = null;
        Object context = gp;
        if (taskType != null){
            String taskTypeCode = taskType.getCode();
            taskPlan = getEntity(taskTypeCode, context, () -> {
                TaskPlan p = modelFactory.newTaskPlan(taskType);
                MaintenanceGroup group = getMaintenanceGroupInCtx(r, area);
                p.setResource(group);
                return p;
            });
        }else {
            LOG.warn("TaskType is null!!!");
        }
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

    public GeneralTaskPlan getGeneralTaskPlanInCtx(Result r, Object context){
        // general task plan <=> different session.type.type per session.type.area
        String generalTaskType = getGeneralTaskTypeLabel(r);
        GeneralTaskPlan generalTaskPlan = getEntity(
                generalTaskType,
                context,
                () -> createGeneralTasPlan(r, generalTaskType));
        return generalTaskPlan;
    }

    private GeneralTaskPlan createGeneralTasPlan(Result r, String generalTaskType){
        GeneralTaskPlan p = modelFactory.newGeneralTaskPlan(generalTaskType);
        AircraftArea area = getAircraftArea(r);
        p.setResource(area);
        return p;
    }

    public PhasePlan getPhasePlan(Result r, final Aircraft aircraft){
        String phaseLabel = getPhaseLabel(r);
        PhasePlan phasePlan = getEntity(phaseLabel, "phase", () -> {
            PhasePlan p = modelFactory.newPhasePlan(phaseLabel);
            Aircraft ac = getAircraft(r);
            p.setResource(defaults.isDefault(ac) ? aircraft : ac);
            return p;
        });
        return phasePlan;
    }



    // this is a simplification
    public String getMechanicLabel(Result r){
        return Optional.ofNullable(r.getMechanic()).map(Mechanic::getTitle)
                .map(this::mapNull).orElse(modelFactory.generateId() + "");
    }

    public String getAircraftModelLabel(Result r){
        return getTaskTypeDefinition(r).map(TaskType::getAcmodel)
                .map(this::mapNull).orElse(defaults.aircraftModelLabel);
    }

    public String getAreaLabel(Result r){
        return getTaskTypeDefinition(r).map(TaskType::getArea)
                .map(this::mapNull).orElse(defaults.areaLabel);
    }

    public String getMaintenanceGroupLabel(Result r){
        return getTaskTypeDefinition(r).map(TaskType::getScope)
                .map(this::mapNull).orElse(defaults.maintenanceGroupLabel);
    }

    public String getGeneralTaskTypeLabel(Result r){
        return getTaskTypeDefinition(r).map(TaskType::getTaskType)
                .map(this::mapNull).orElse(defaults.generalTaskTypeLabel);
    }

    public String getPhaseLabel(Result r){
        return getTaskTypeDefinition(r).map(TaskType::getPhase)
                .map(this::mapNull).orElse(defaults.phaseLabel);
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

    private String mapNull(String s){
        return isEmpty(s) ? null : s;
    }

    private boolean isEmpty(String s){
        return s == null || s.trim().isEmpty();
    }

    public static String DEFAULT = "unknown";
    public static class Defaults{
        public String aircraftModelLabel = DEFAULT;
        public String phaseLabel = DEFAULT;
        public String areaLabel = DEFAULT;
        public String maintenanceGroupLabel = DEFAULT;
        public String generalTaskTypeLabel = DEFAULT;
        public String taskTypeCode = DEFAULT;
        public String mechanicLabel = DEFAULT;
        public boolean isDefault(Aircraft ac){
            return aircraftModelLabel.equals(ac.getTitle());
        }

        public boolean isDefault(AircraftArea aa){
            return areaLabel.equals(aa.getTitle());
        }

        public boolean isDefault(MaintenanceGroup mg){
            return maintenanceGroupLabel.equals(mg.getTitle());
        }
    }
}
