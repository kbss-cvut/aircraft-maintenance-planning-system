package cz.cvut.kbss.amaplas.exp.optaplanner_old.model;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;

@PlanningSolution
public class MaintenanceJob {
    protected List<Integer> taskOrderIndices;
    protected List<MaintenanceTask> tasks;

    protected HardSoftScore score;

    @ValueRangeProvider(id = "availableIndices")
    @ProblemFactCollectionProperty
    public List<Integer> getTaskOrderIndices() {
        return taskOrderIndices;
    }

    public void setTaskOrderIndices(List<Integer> taskOrderIndices) {
        this.taskOrderIndices = taskOrderIndices;
    }


    @PlanningEntityCollectionProperty
    public List<MaintenanceTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<MaintenanceTask> tasks) {
        this.tasks = tasks;
    }

    @PlanningScore
    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }


}
