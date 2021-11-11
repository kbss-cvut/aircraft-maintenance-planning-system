package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskType extends EventType {

    @JsonIgnore
    public String id;
    @JsonProperty("id")
    public String type;
    public String label;
    public String scope;
    @JsonProperty("task-category")
    public String taskcat;
    @JsonIgnore
    public String viewLabel;

    public TaskType() {
    }

    public TaskType(TaskType t){
        this(t.type,t.label, t.taskcat);
    }

    public TaskType(String type){
        this(type,type);
    }
    public TaskType(String type, String label) {
        this.type = type;
        this.label = label;
        this.viewLabel = type + "\n" + label;
    }

    public TaskType(String type, String label, String taskcat) {
        this(type, label);
        this.taskcat = taskcat;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String typeLabel() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskType)) return false;
        TaskType taskType = (TaskType) o;
        return type.equals(taskType.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return type;
    }

    public static Map<String, TaskType> taskTypeMap;

    public static Map<String, TaskType> normalizeTaskTypes(List<TaskType> taskTypes){
        // normalize type codes
        taskTypes.forEach(t ->
                t.type = Optional
                        .of(t.type)
                        .map(l -> l.replace("%20", " "))
                        .orElse(null)
        );

        Map<String, TaskType> taskTypeMap = new HashMap<>();
        taskTypes.stream()
                .collect(Collectors.groupingBy(t -> t.type)).entrySet().stream()
                .forEach(e -> taskTypeMap.put(
                        e.getKey(),
                        // FIX bug - selecting longest task type label
                        e.getValue().stream().sorted(Comparator.comparing((TaskType t) -> t.label.length()).reversed()).findFirst().get()
                ));
        TaskType.taskTypeMap = taskTypeMap;
        return  taskTypeMap;
    }
}
