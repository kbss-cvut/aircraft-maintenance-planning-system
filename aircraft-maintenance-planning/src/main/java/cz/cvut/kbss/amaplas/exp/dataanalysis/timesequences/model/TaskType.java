package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.cvut.kbss.amaplas.exp.dataanalysis.inputs.AnalyzeTCCodes;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class TaskType extends EventType<String> {
//    public String type = TaskType.class.getSimpleName();

//    @JsonProperty("id")
    public String code; // the code of the task
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

    @JsonIgnore
    public TaskType definition;

    public TaskType() {
    }

    public TaskType(TaskType t){
        this(t.code,t.label, t.taskcat, t.acmodel);
    }

    public TaskType(String code){
        this(code, code);
    }
    public TaskType(String code, String label) {
        setCode(code);
        this.id = code;
        this.label = label;
        this.title = label;
        this.viewLabel = code + "\n" + label;
    }

    public TaskType(String code, String label, String taskcat, String acmodel) {
        this(code, label);
        this.taskcat = taskcat;
        this.acmodel = acmodel;
    }

//    public String getId() {
//        return id;
//    }

    public void setCode(String code) {
        this.code = code;
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
    public String getCode() {
        return code;
    }

    @Override
    public String typeLabel () {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskType)) return false;
        TaskType taskType = (TaskType) o;
        return code.equals(taskType.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }

    public static Map<String, TaskType> taskTypeMap;

    public static Map<String, TaskType> normalizeTaskTypes(List<TaskType> taskTypes){
        // normalize type codes
        taskTypes.forEach(t ->
                t.code = Optional
                        .of(t.code)
                        .map(l -> l.replace("%20", " "))
                        .orElse(null)
        );

        Map<String, TaskType> taskTypeMap = new HashMap<>();
        taskTypes.stream()
                .collect(Collectors.groupingBy(t -> t.code)).entrySet().stream()
                .forEach(e -> taskTypeMap.put(
                        e.getKey(),
                        // FIX bug - selecting longest task type label
                        e.getValue().stream().sorted(Comparator.comparing((TaskType t) -> t.label.length()).reversed()).findFirst().get()
                ));
        TaskType.taskTypeMap = taskTypeMap;
        return  taskTypeMap;
    }

    public static List<TaskType> taskTypeDefinitions;
    public static Map<String, List<TaskType>> taskTCCode2TCDefinitionMap;


    public static void setTaskTypeDefinitions(List<TaskType> taskTypeDefinitions){
        TaskType.taskTypeDefinitions = taskTypeDefinitions;
    }

    /**
     * taskTypeDefinitions should be set first
     * @param results
     */
    public static void initTC2TCDefMap(List<Result> results){
        taskTCCode2TCDefinitionMap = new HashMap<>();
        results.stream()
                .map(r -> r.taskType)
                .filter(t -> t != null && t.code != null)
                .forEach(t -> taskTCCode2TCDefinitionMap.put(t.code, findMatchingTCDef(t)));

    }

    protected static List<TaskType> findMatchingTCDef(TaskType tt){
        List<TaskType> matches = findMatchingTCDef(tt.code, TaskType::getCode);

        if(matches.isEmpty()) {
            matches = findMatchingTCDef(tt.code, TaskType::getMpdtask);
            matches.sort(Comparator.comparing(t -> t.getMpdtask().length()));
        }else{
            matches.sort(Comparator.comparing(t -> t.code.length()));
        }

        return matches;
    }

    protected static List<TaskType> findMatchingTCDef(String tcCode, Function<TaskType, String> idfunc){
        return taskTypeDefinitions.stream()
                .filter(t -> AnalyzeTCCodes.is_TCCode_Match_v3(idfunc.apply(t), tcCode))
                .collect(Collectors.toList());
    }

    public static TaskType getTaskTypeDefinition(Result r){
        if(r == null)
            return null;

        return getTaskTypeDefinition(r.taskType);
    }

    public static TaskType getTaskTypeDefinition(TaskType tt){
        if(tt == null || tt.code == null)
            return null;

        List<TaskType> res = taskTCCode2TCDefinitionMap.get(tt.code);
        return Optional.ofNullable(res).map(l -> l.isEmpty() ? null : l.get(0)).orElse(null);
    }
}
