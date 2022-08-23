package cz.cvut.kbss.amaplas.model;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.function.Supplier;


/**
 * The factory will generate new instances for the same input. The user of the class should manage reuse of existing
 * instances, i.e. not generating new instances with the same property state.
 */
public class ModelFactory {

    protected Long lastId = null;
    protected Long lastIdSlot = -1l;

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
        TaskPlan p = newPlan(() -> new TaskPlan(), plannedStart, plannedEnd, start, end, plannedWorkTime, workTime);
        if(type != null) {
            p.setTaskType(type);
            p.setTitle(type.getViewLabel());
        }
        return p;
    }
    public TaskPlan newTaskPlan(String label){
        return newPlan(() -> new TaskPlan(), null, null, null, null, null, null);
    }

    public GeneralTaskPlan newGeneralTaskPlan(String label){
        GeneralTaskPlan generalTaskPlan = newEntity(() -> new GeneralTaskPlan(), label);
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
        AircraftArea aircraftArea = newResource(() -> new AircraftArea(), name);
        return aircraftArea;
    }

    public MaintenanceGroup newMaintenanceGroup(String name){
        MaintenanceGroup maintenanceGroup = newResource(() -> new MaintenanceGroup(), name);
        return maintenanceGroup;
    }


    public Mechanic newMechanic(String label){
        Mechanic mechanic = newResource(() -> new Mechanic(), label);
        return mechanic;
    }

    public <T extends Resource> T newResource(Supplier<T> entityFactory, String name){
        T resource = newEntity(entityFactory, name);
        return resource;
    }

    public RevisionPlan newRevisionPlan(String label){
        RevisionPlan revisionPlan = newEntity(() -> new RevisionPlan(), label);
        return revisionPlan;
    }

    public PhasePlan newPhasePlan(String label){
        PhasePlan phasePlan = newEntity(() -> new PhasePlan(), label);
        return phasePlan;
    }

    public SessionPlan newSessionPlan(Date startTime, Date endTime){
        SessionPlan p = newPlan(() -> new SessionPlan(), null, null, startTime, endTime, null, null);
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

    public <T extends AbstractEntity> T newEntity(Supplier<T> entityFactory, String label){
        T entity = entityFactory.get();
        entity.setId(generateId());
        entity.setTitle(label);
        return entity;
    }

    public <T extends AbstractPlan>  T newPlan(Supplier<T> entityFactory, Date plannedStart, Date plannedEnd, Date start, Date end, Long plannedWorkTime, Long workTime ){
        T plan = newEntity(entityFactory, null);
        plan.setPlannedStartTime(plannedStart);
        plan.setPlannedEndTime(plannedEnd);
        plan.setDuration(duration(plan.getPlannedStartTime(), plan.getPlannedEndTime()));
        plan.setStartTime(start);
        plan.setEndTime(end);
        plan.setDuration(duration(plan.getStartTime(), plan.getEndTime()));

        plan.setPlannedWorkTime(plannedWorkTime);
        plan.setWorkTime(workTime);
        return plan;
    }

    public Long generateId(){
        long idSlot = System.currentTimeMillis();
        long newId = -1;
        // create multiple ids per millisecond to speed up id generation
        if(lastIdSlot != idSlot){
            lastIdSlot = idSlot;
            newId = lastIdSlot*10000;
        }else{
            newId = lastId + 1;
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
