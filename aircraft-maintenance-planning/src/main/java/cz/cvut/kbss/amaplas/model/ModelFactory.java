package cz.cvut.kbss.amaplas.model;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;


/**
 * The factory will generate new instances for the same input. The user of the class should manage reuse of existing
 * instances, i.e. not generating new instances with the same property state.
 */
public class ModelFactory {

    protected Long lastId = null;

    public TaskType newTaskType(String code, String label, String area, String taskType, String scope, String phase, String taskCat){
        TaskType tt = new TaskType(code, label);
        tt.setArea(area);
        tt.setTaskType(taskType);
        tt.setScope(scope);
        tt.setPhase(phase);
        tt.setTaskcat(taskCat);
        return tt;
    }



    public TaskPlan newTaskPlan(TaskType type){
        return newTaskPlan(type, null, null, null, null, null, null);
    }

    public TaskPlan newTaskPlan(TaskType type, Date plannedStart, Date plannedEnd, Long plannedWorkTime){
        return newTaskPlan(type, plannedStart, plannedEnd, null, null, plannedWorkTime, null);
    }

    public TaskPlan newTaskPlan(TaskType type, Date plannedStart, Date plannedEnd, Date start, Date end, Long plannedWorkTime, Long workTime ){
        TaskPlan p = newPlan(TaskPlan.class, plannedStart, plannedEnd, start, end, plannedWorkTime, workTime);
        if(type != null) {
            p.setTaskType(type);
            p.setTitle(type.getViewLabel());
        }
        return p;
    }
    public TaskPlan newTaskPlan(String label){
        return newPlan(TaskPlan.class, null, null, null, null, null, null);
    }

    public GeneralTaskPlan newGeneralTaskPlan(String label){
        GeneralTaskPlan generalTaskPlan = newEntity(GeneralTaskPlan.class, label);
        return generalTaskPlan;
    }
//
//    public PhasePlan newPhasePlan(){
//
//    }


//    public ResourceCombination newResourceCombination(AircraftArea aircraftArea, Resource resource){
//        String label = resource.getTitle();
//        ResourceCombination resourceCombination = newResource(ResourceCombination.class, label);
//        return resourceCombination;
//    }

    public AircraftArea newAircraftArea(String name){
        AircraftArea aircraftArea = newResource(AircraftArea.class, name);
        return aircraftArea;
    }

    public MaintenanceGroup newMaintenanceGroup(String name){
        MaintenanceGroup maintenanceGroup = newResource(MaintenanceGroup.class, name);
        return maintenanceGroup;
    }


    public Mechanic newMechanic(String label){
        Mechanic mechanic = newResource(Mechanic.class, label);
        return mechanic;
    }

    public <T extends Resource> T newResource(Class<T> resourceClass, String name){
        T resource = newEntity(resourceClass, name);
        return resource;
    }

    public RevisionPlan newRevisionPlan(String label){
        RevisionPlan revisionPlan = newEntity(RevisionPlan.class, label);
        return revisionPlan;
    }

    public PhasePlan newPhasePlan(String label){
        PhasePlan phasePlan = newEntity(PhasePlan.class, label);
        return phasePlan;
    }

    public SessionPlan newSessionPlan(Date startTime, Date endTime){
        SessionPlan p = newPlan(SessionPlan.class, null, null, startTime, endTime, null, null);
        return p;
    }

    public SessionPlan newSessionPlan(Mechanic mechanic, Date startTime, Date endTime){
        SessionPlan p = newSessionPlan(startTime, endTime);
        p.setWorkTime(p.getDuration());
        p.setResource(mechanic);
        return p;
    }

    public SessionPlan newSessionPlan(Mechanic mechanic, TaskPlan taskPlan, Date startTime, Date endTime){
        SessionPlan p = newSessionPlan(mechanic, startTime, endTime);
        taskPlan.getPlanParts().add(p);
        return p;
    }

    public <T extends AbstractEntity> T newEntity(Class<T> cls, String label){
        try {
            T entity = cls.getConstructor().newInstance();
            entity.setId(generateId());
//            Thread.sleep(1);
            entity.setTitle(label);
            return entity;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
        }
        return null;
    }

    public <T extends AbstractPlan>  T newPlan(Class<T> cls, Date plannedStart, Date plannedEnd, Date start, Date end, Long plannedWorkTime, Long workTime ){
        T plan = newEntity(cls, null);
        plan.setPlannedStartTime(plannedStart);
        plan.setPlannedEndTime(plannedEnd);
        plan.setDuration(duration(plan.getPlannedStartTime(), plan.getPlannedEndTime()));
        plan.setStartTime(start);
        plan.setEndTime(end);
        plan.setDuration(duration(plan.getStartTime(), plan.getEndTime()));

        plan.setPlannedWorkTime(plannedWorkTime);
        plan.setWorkTime(workTime);

        plan.setId(generateId());
        return plan;
    }

    public Long generateId(){
        long newId = System.currentTimeMillis();
        while(lastId != null && newId <= lastId) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            newId = System.currentTimeMillis();
        }
        lastId = newId;
        return newId;
    }

    public Long duration(Date from, Date to){
        if(from == null || to == null)
            return null;
        return to.getTime() - from.getTime();
    }

}
