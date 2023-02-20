package cz.cvut.kbss.amaplas.planners;

import cz.cvut.kbss.amaplas.model.Result;
import cz.cvut.kbss.amaplas.model.TaskType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class TaskPattern {
    public List<List<Result>> instances = new ArrayList<>();
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

    public List<List<Result>> getInstances() {
        return instances;
    }

    public void setInstances(List<List<Result>> instances) {
        this.instances = instances;
    }

    public List<Result> getInstance(Predicate<Result> predicate){
        return instances.stream()
            .filter(i -> !i.stream().filter(predicate).findFirst().isEmpty())
            .findFirst().orElse(null);
    }

    public <T> List<Result> getInstance(List<Result> sessions, Function<Result, T> function){
        T val = getValue(sessions, function);
        return instances.stream()
                .filter(l -> Objects.equals(getValue(l, function), val))
                .findFirst()
                .orElse(null);
    }

    public static <T> T getValue(List<Result> r, Function<Result, T> function){
        return r.stream().map(function).filter(v -> v != null).findFirst().orElse(null);
    }

}
