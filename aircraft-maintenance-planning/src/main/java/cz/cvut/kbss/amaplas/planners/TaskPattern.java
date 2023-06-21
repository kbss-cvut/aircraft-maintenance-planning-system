package cz.cvut.kbss.amaplas.planners;

import cz.cvut.kbss.amaplas.model.TaskExecution;
import cz.cvut.kbss.amaplas.model.TaskType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class TaskPattern {
    public List<TaskExecution> instances = new ArrayList<>();
    protected TaskType taskType;
    protected boolean planned;

    public TaskPattern() {
    }

    public TaskPattern(TaskType taskType) {
        this.taskType = taskType;
    }

    public TaskPattern(TaskType taskType, boolean planned) {
        this.taskType = taskType;
        this.planned = planned;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public boolean isPlanned() {
        return planned;
    }

    public void setPlanned(boolean planned) {
        this.planned = planned;
    }

    public List<TaskExecution> getInstances() {
        return instances;
    }

    public void setInstances(List<TaskExecution> instances) {
        this.instances = instances;
    }

    public TaskExecution getInstance(Predicate<TaskExecution> predicate){
        return instances.stream()
            .filter(predicate)
            .findFirst().orElse(null);
    }

    public <T> TaskExecution getInstance(TaskExecution taskExecution, Function<TaskExecution, T> function){
        T val = getValue(taskExecution, function);
        return instances.stream()
                .filter(te -> Objects.equals(getValue(te, function), val))
                .findFirst()
                .orElse(null);
    }

    public static <T> T getValue(TaskExecution taskExecution, Function<TaskExecution, T> function){
        return function.apply(new TaskExecution());
    }

}
