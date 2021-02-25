package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import java.util.*;
import java.util.stream.Collectors;

public class TaskType extends EventType {
    public String id;
    public String type;
    public String label;
    public String taskcat;
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
                        e.getValue().stream().sorted(Comparator.comparing(t -> t.label.length())).findFirst().get()
                ));
        return  taskTypeMap;
    }
}
