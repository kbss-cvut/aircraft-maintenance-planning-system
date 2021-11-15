package cz.cvut.kbss.amaplas.exp.optaplanner.model;


import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@PlanningEntity
public class MaintenanceTaskAssignment {

    @PlanningId
    private Long id;

    @PlanningVariable(valueRangeProviderRefs = "maintenanceTaskRange")
    private MaintenanceTask maintenanceTask;

    @PlanningVariable(valueRangeProviderRefs = "timeSlotRange")
    private TimeSlot timeSlot;

    public MaintenanceTaskAssignment() {
    }

    public MaintenanceTaskAssignment(Long id, MaintenanceTask maintenanceTask, TimeSlot timeSlots) {
        Objects.nonNull(maintenanceTask);
        this.id = id;
        this.maintenanceTask = maintenanceTask;
        this.timeSlot = timeSlots;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public MaintenanceTask getMaintenanceTask() {
        return maintenanceTask;
    }

    public void setMaintenanceTask(MaintenanceTask maintenanceTask) {
        this.maintenanceTask = maintenanceTask;
    }


    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    @Override
    public String toString() {
        return "MaintenanceTaskAssignment{" +
                "id=" + id +
                ", maintenanceTask=" + maintenanceTask.toString() +
                ", timeSlot=" + timeSlot.toString() +
                '}';
    }

    //    public boolean isUnassigned(){
//        return isNullOrEmpty(this.getTimeSlot());
//    }

//    public boolean isCorrectlyAssigned(){
//        int length = timeSlot != null ? timeSlot.size() : 0;
//        return length == maintenanceTask.getTimeSlotsRequired();
//    }

//    public boolean isOverlapping(MaintenanceTaskAssignment other){
//        if(this.isUnassigned() || other.isUnassigned())
//            return false;
//        return overlaps(this.getTimeSlot(), other.getTimeSlot());
//    }



//    public static boolean overlaps(MaintenanceTaskAssignment a1, MaintenanceTaskAssignment a2){
//        return a1.isOverlapping(a2);
//    }

//    public static boolean overlaps(Set<?> s1, Set<?> s2){
//        Set<?> overlaps = new HashSet<>(s1);
//        overlaps.removeAll(s2);
//        return !overlaps.isEmpty();
//    }

}
