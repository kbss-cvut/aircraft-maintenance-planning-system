package cz.cvut.kbss.amaplas.exp.optaplanner.model;

import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaintenanceTask {

    @PlanningId
    protected String name;
    protected int timeSlotsRequired;


    public MaintenanceTask() {
    }

    public MaintenanceTask(String name, int minimumRequiredTimeSlots) {
        this.name = name;
        this.timeSlotsRequired = minimumRequiredTimeSlots;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTimeSlotsRequired() {
        return timeSlotsRequired;
    }

    public void setTimeSlotsRequired(int timeSlotsRequired) {
        this.timeSlotsRequired = timeSlotsRequired;
    }

    @Override
    public String toString() {
        return "MaintenanceTask{" +
                "name='" + name + '\'' +
                ", timeSlotsRequired=" + timeSlotsRequired +
                '}';
    }

    public boolean isUnassigned(List<MaintenanceTaskAssignment> assignmentList){
        return isNullOrEmpty(assignmentList);
    }

    public boolean isCorrectlyAssigned(List<MaintenanceTaskAssignment> assignmentList){
        int length = assignmentList != null ? assignmentList.size() : 0;
        return length == this.getTimeSlotsRequired();
    }

    public boolean isCorrectlyAssigned(int assignments){
        return assignments == this.getTimeSlotsRequired();
    }


    public static boolean isNullOrEmpty(Collection<?> in){
        return in == null || in.isEmpty();
    }
}
