package cz.cvut.kbss.amaplas.model;

import java.awt.geom.Area;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


public class ModelBuilder {
    protected ModelFactory factory;

    protected Map<Object, Map<String, AbstractEntityWithDescription>> entityMaps = new HashMap<>();

    public ModelBuilder() {
    }

    public ModelBuilder(ModelFactory factory) {
        this.factory = factory;
    }

    public ModelFactory getFactory() {
        return factory;
    }

    public void setFactory(ModelFactory factory) {
        this.factory = factory;
    }

//    public TaskType getTaskType(String code, String label, String area, String taskType, String scope, String phase, String taskCat) {
//        return getEntity(code, "task-type", () -> factory.newTaskType(code, label, area, taskType, scope, phase, taskCat));
//    }

    public GeneralTaskPlan getGeneralTaskPlan(String phase, String label) {
        return getEntity(phase, label, () -> factory.newGeneralTaskPlan(label));
    }

    public AircraftArea getAircraftArea(String name) {
        return factory.newAircraftArea(name);
    }

    public MaintenanceGroup newMaintenanceGroup(String name) {
        return factory.newMaintenanceGroup(name);
    }

    public Mechanic newMechanic(Area area, String label) {
        return factory.newMechanic(label);
    }

    public <T extends Resource> T newResource(Supplier<T> entityFactory, String name) {
        return factory.newResource(entityFactory, name);
    }

    public RevisionPlan newRevisionPlan(String label) {
        return factory.newRevisionPlan(label);
    }

    public PhasePlan newPhasePlan(String label) {
        return factory.newPhasePlan(label);
    }

    public SessionPlan getSessionPlan(Mechanic mechanic, Date startTime, Date endTime) {
        return factory.newSessionPlan(mechanic, startTime, endTime);
    }

    public SessionPlan newSessionPlan(Mechanic mechanic, TaskPlan taskPlan, Date startTime, Date endTime) {
        return factory.newSessionPlan(mechanic, taskPlan, startTime, endTime);
    }

    public <T extends AbstractEntityWithDescription> T newEntity(Supplier<T> entityFactory, String label) {
        return factory.newEntity(entityFactory, label);
    }

    public <T extends AbstractPlan> T newPlan(Supplier<T> entityFactory, Date plannedStart, Date plannedEnd, Date start, Date end, Long plannedWorkTime, Long workTime) {
        return factory.newPlan(entityFactory, plannedStart, plannedEnd, start, end, plannedWorkTime, workTime);
    }

    public Long duration(Date from, Date to) {
        return factory.duration(from, to);
    }

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
}
