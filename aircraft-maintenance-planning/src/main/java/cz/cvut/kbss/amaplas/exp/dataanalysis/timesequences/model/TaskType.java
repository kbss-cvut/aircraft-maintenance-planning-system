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
    public String acmodel;
    public String area;
    public String mpdtask;
    public String phase;
    public String taskType;

    public TaskType() {
    }

    public TaskType(TaskType t){
        this(t.type,t.label, t.taskcat, t.acmodel);
    }

    public TaskType(String type){
        this(type,type);
    }
    public TaskType(String type, String label) {
        this.type = type;
        this.label = label;
        this.viewLabel = type + "\n" + label;
    }

    public TaskType(String type, String label, String taskcat, String acmodel) {
        this(type, label);
        this.taskcat = taskcat;
        this.acmodel = acmodel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getTaskcat() {
        return taskcat;
    }

    public void setTaskcat(String taskcat) {
        this.taskcat = taskcat;
    }

    public String getViewLabel() {
        return viewLabel;
    }

    public void setViewLabel(String viewLabel) {
        this.viewLabel = viewLabel;
    }

    public String getAcmodel() {
        return acmodel;
    }

    public void setAcmodel(String acmodel) {
        this.acmodel = acmodel;
    }

    public String getMpdtask() {
        return mpdtask;
    }

    public void setMpdtask(String mpdtask) {
        this.mpdtask = mpdtask;
    }

    public static Map<String, TaskType> getTaskTypeMap() {
        return taskTypeMap;
    }

    public static void setTaskTypeMap(Map<String, TaskType> taskTypeMap) {
        TaskType.taskTypeMap = taskTypeMap;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String typeLabel () {
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
