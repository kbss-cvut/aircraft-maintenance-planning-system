package cz.cvut.kbss.amaplas.exp.optaplanner.model;


import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class MaintenanceTask {

    @PlanningVariable(valueRangeProviderRefs = {"availableIndices"})
    protected Integer taskOrderIndex;

    protected String name;
    protected int val;

    public MaintenanceTask() {
    }

    public MaintenanceTask(String name, int val) {
        this.name = name;
        this.val = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public Integer getTaskOrderIndex() {
        return taskOrderIndex;
    }

    public void setTaskOrderIndex(Integer taskOrderIndex) {
        this.taskOrderIndex = taskOrderIndex;
    }

    @Override
    public String toString() {
        return "MaintenanceTask{" +
                "taskOrderIndex=" + taskOrderIndex +
                '}';
    }
}
