package cz.cvut.kbss.amaplas.exp.optaplanner.model;

import org.apache.commons.lang3.StringUtils;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@PlanningSolution
public class MaintenanceSchedule {
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "maintenanceTaskRange")
    private List<MaintenanceTask> tasks;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "timeSlotRange")
    private List<TimeSlot> timeSlots;

    @PlanningEntityCollectionProperty
    private List<MaintenanceTaskAssignment> maintenanceTaskAssignmentList;

    @PlanningScore
    private HardSoftScore score;

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(List<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots;
    }

    public List<MaintenanceTaskAssignment> getMaintenanceTaskAssignmentList() {
        return maintenanceTaskAssignmentList;
    }

    public void setMaintenanceTaskAssignmentList(List<MaintenanceTaskAssignment> maintenanceTaskAssignmentList) {
        this.maintenanceTaskAssignmentList = maintenanceTaskAssignmentList;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public List<MaintenanceTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<MaintenanceTask> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return "MaintenanceSchedule{" +
                "tasks=[" + tasks.stream().map(t -> t.toString()).collect(Collectors.joining(", ")) + "]" +
                ", timeSlots=[" + timeSlots.stream().map(t -> t.toString()).collect(Collectors.joining(", ")) + "]" +
                ", maintenanceTaskAssignmentList=[" + maintenanceTaskAssignmentList.stream().map(t -> t.toString()).collect(Collectors.joining(", ")) + "]" + "]" +
                ", score=" + score.toString() +
                '}';
    }
}
