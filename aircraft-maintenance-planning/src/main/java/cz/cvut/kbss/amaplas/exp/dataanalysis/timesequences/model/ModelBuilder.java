package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import java.awt.geom.Area;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


public class ModelBuilder {
    protected ModelFactory factory;

    protected Map<Object, Map<String, AbstractEntity>> entityMaps = new HashMap<>();

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

    public TaskType getTaskType(String code, String label, String area, String taskType, String scope, String phase, String taskCat) {
        return getEntity(code, "task-type", () -> factory.newTaskType(code, label, area, taskType, scope, phase, taskCat));
    }

    public GeneralTaskPlan getGeneralTaskPlan(String phase, String label) {
        return getEntity(phase, label, () -> factory.newGeneralTaskPlan(label));
    }

//    public ResourceCombination getResourceCombination(AircraftArea aircraftArea, Resource resource) {
////        return null;//factory.newResourceCombination(aircraftArea, resource);
//        return getEntity(aircraftArea.title, resource.getTitle(), () -> factory.newR(aircraftArea, resource));
//    }

    public AircraftArea getAircraftArea(String name) {
        return factory.newAircraftArea(name);
    }

    public MaintenanceGroup newMaintenanceGroup(String name) {
        return factory.newMaintenanceGroup(name);
    }

    public Mechanic newMechanic(Area area, String label) {
        return factory.newMechanic(label);
    }

    public <T extends Resource> T newResource(Class<T> resourceClass, String name) {
        return factory.newResource(resourceClass, name);
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

    public <T extends AbstractEntity> T newEntity(Class<T> cls, String label) {
        return factory.newEntity(cls, label);
    }

    public <T extends AbstractPlan> T newPlan(Class<T> cls, Date plannedStart, Date plannedEnd, Date start, Date end, Long plannedWorkTime, Long workTime) {
        return factory.newPlan(cls, plannedStart, plannedEnd, start, end, plannedWorkTime, workTime);
    }

    public Long duration(Date from, Date to) {
        return factory.duration(from, to);
    }

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

//    public interface ResourceFactoryMethod<T>{
//        T createResource(Class<T> resourceClass, String name);
//    }
}
