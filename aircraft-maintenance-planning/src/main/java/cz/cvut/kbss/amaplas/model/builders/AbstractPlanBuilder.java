package cz.cvut.kbss.amaplas.model.builders;

import cz.cvut.kbss.amaplas.model.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class AbstractPlanBuilder<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPlanBuilder.class);


    protected final ModelFactory modelFactory;
    protected Map<Object, Map<String, AbstractEntityWithDescription>> entityMaps = new HashMap<>();
    protected Defaults defaults = new Defaults();
    protected Map<String, String> messages = new HashMap<>();


    public AbstractPlanBuilder() {
        modelFactory = new ModelFactory();
    }

    /**
     * Create a plan builder reusing the state of a different plan builder, i.e. model factory, entityMaps, messages and
     * defaults.
     * @param planBuilder
     */
    public AbstractPlanBuilder(AbstractPlanBuilder planBuilder) {
        this.modelFactory = planBuilder.modelFactory;
        this.entityMaps = planBuilder.entityMaps;
        this.defaults = planBuilder.defaults;
        this.messages = planBuilder.messages;
    }

    protected abstract RevisionPlan createRevision(Workpackage wp);

    public TaskPlan getTaskPlan(final TaskType taskType, Object context, String aircraftAreaLabel){
        final String maintenanceGroupLabel = getMaintenanceGroupLabel(optionalTaskType(taskType));

        TaskPlan taskPlan = null;
        if (taskType != null){
            String taskTypeCode = taskType.getCode();
            taskPlan = getEntity(taskTypeCode, context, () -> {
                TaskPlan p = modelFactory.newTaskPlan(taskType);
                MaintenanceGroup group = getMaintenanceGroupInCtx(maintenanceGroupLabel, aircraftAreaLabel);
                p.setResource(group);
                return p;
            });
        }else {
            LOG.warn("TaskType is null!!!");
        }
        return  taskPlan;
    }


    public MaintenanceGroup getMaintenanceGroupInCtx(String maintenanceGroupLabel, String area){
        Pair pair = Pair.of("group", area);
        MaintenanceGroup maintenanceGroup = getEntity(maintenanceGroupLabel, pair, () -> modelFactory.newMaintenanceGroup(maintenanceGroupLabel));
        return maintenanceGroup;
    }

    public AircraftArea getAircraftArea(Optional<TaskType> optionalTaskType){
        String aircraftAreaLabel = getAircraftAreaLabel(optionalTaskType);
        return getEntity(aircraftAreaLabel, "aircraft-area", () -> modelFactory.newAircraftArea(aircraftAreaLabel));
    }

    public AircraftArea getAircraftArea(String area){
        return getEntity(area, "aircraft-area", () -> modelFactory.newAircraftArea(area));
    }

    public Aircraft getAircraft(Optional<TaskType> taskType){
        return getAircraft(getAircraftModelLabel(taskType));
    }

    public Aircraft getAircraft(String aircraftModelLabel){
        return getEntity(aircraftModelLabel, "aircraft", () -> modelFactory.newEntity(() -> new Aircraft(), aircraftModelLabel));
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
    public <T extends AbstractEntityWithDescription> T getEntity(String id, Object context, Supplier<T> generator){
        Map<String, AbstractEntityWithDescription> entityMap = entityMaps.get(context);
        if(entityMap == null) {
            entityMap = new HashMap<>();
            entityMaps.put(context, entityMap);
        }
        AbstractEntityWithDescription r = entityMap.get(id);
        if(r == null) {
            r = generator.get();
            entityMap.put(id, r);
        }
        return (T)r;
    }

    public GeneralTaskPlan getGeneralTaskPlanInCtx(Optional<TaskType> optionalTaskType, PhasePlan phasePlan, AircraftArea aircraftArea){
        // general task plan <=> different session.type.type per session.type.area
        String generalTaskType = getGeneralTaskTypeLabel(optionalTaskType);
        Pair<PhasePlan, AircraftArea> context = Pair.of(phasePlan, aircraftArea);
        GeneralTaskPlan generalTaskPlan = getEntity(
                generalTaskType,
                context,
                () -> createGeneralTaskPlan(aircraftArea, generalTaskType));
        return generalTaskPlan;
    }

    private GeneralTaskPlan createGeneralTaskPlan(AircraftArea aircraftArea, String generalTaskType){
        GeneralTaskPlan p = modelFactory.newGeneralTaskPlan(generalTaskType);
        p.setResource(aircraftArea);
        return p;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //// Accessors  ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getMaintenanceGroupLabel(Optional<TaskType> optionalTaskType) {
        return optionalTaskType.map(TaskType::getScope)
                .map(MaintenanceGroup::getAbbreviation) // TODO check if this field is set up, check 1) dao sets up the field, 2) value visible in client
                .map(this::mapNull)
                .orElse(defaults.maintenanceGroupLabel);
    }

    public String getAircraftModelLabel(Optional<TaskType> optionalTaskType){
        return optionalTaskType.map(TaskType::getAcmodel)
                .map(this::mapNull).orElse(defaults.aircraftModelLabel);
    }

    public String getAircraftAreaLabel(Optional<TaskType> optionalTaskType){
        return optionalTaskType.map(TaskType::getArea)
                .map(this::mapNull).orElse(defaults.areaLabel);
    }

    public String getPhaseLabel(Optional<TaskType> optionalTaskType){
        return optionalTaskType.map(TaskType::getPhase)
                .map(this::mapNull).orElse(defaults.phaseLabel);
    }

    public String getGeneralTaskTypeLabel(Optional<TaskType> optionalTaskType){
        return optionalTaskType.map(TaskType::getTaskType)
                .map(this::mapNull).orElse(defaults.generalTaskTypeLabel);
    }

    protected Optional<TaskType> optionalTaskType(TaskType tt){
        return Optional.of(tt.getDefinition() != null ? tt.getDefinition() : tt);
    }

    protected String mapNull(String s){
        return isEmpty(s) ? null : s;
    }

    protected boolean isEmpty(String s){
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


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //// Messages //////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void info(String message){
        messages.put("INFO", message);
    }
    protected void warn(String message){
        messages.put("WARN", message);
    }
    protected void error(String message){
        messages.put("ERROR", message);
    }

}
