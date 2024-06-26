package cz.cvut.kbss.amaplas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.Transient;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@OWLClass(iri = Vocabulary.s_c_Maintenance_task)
public class TaskType extends EventType {
//    public String type = TaskType.class.getSimpleName();

//    @JsonProperty("id")
    // TODO - decide if we need both id and code?
    @Transient
    protected String code; // the code of the task

    // TODO - there should be a scopes collection because each task type can have work sessions performed by mechanics from different scope groups.
    @OWLObjectProperty(iri = Vocabulary.s_p_has_scope)
    protected Set<MaintenanceGroup> scopes;
    // TODO - the type should be URI and it should point to maintenance group
    // This is the main scope of the task type
    @OWLObjectProperty(iri = Vocabulary.s_p_has_main_scope)
    protected MaintenanceGroup scope;


    // TODO - this is a category (type) of the task. One way to fix this is to use different jopa classes to represent
    //  each task category, i.e. task card, maintenance work order and scheduled work order
    @JsonProperty("task-category")
//    @OWLDataProperty(iri = Vocabulary.s_c_category)
    @Transient
    protected String taskcat;

    @Transient
    @JsonIgnore
    protected String viewLabel;
    @OWLDataProperty(iri = Vocabulary.s_p_task_for_model)
    protected String acmodel;
    @OWLDataProperty(iri = Vocabulary.s_p_task_for_area)
    protected String area;
    // This is a property alternative to the id of the task card
    // - is this field required for work orders?
    // - is it the id of some other type?
    // task defined in a Maintenance planning document (MPD)
    @OWLDataProperty(iri = Vocabulary.s_p_has_mpdtask)
    protected String mpdtask;
    @OWLDataProperty(iri = Vocabulary.s_p_task_for_phase)
    protected String phase;

    @OWLDataProperty(iri = Vocabulary.s_p_has_general_task_type)
    protected String taskType;

    @Transient
    @JsonIgnore
    protected TaskType definition;

    @OWLDataProperty(iri = Vocabulary.s_p_required_el_power_restrictions)
    protected String elPowerRestrictions;
    @OWLDataProperty(iri = Vocabulary.s_p_required_hyd_power_restrictions)
    protected String hydPowerRestrictions;
    @OWLDataProperty(iri = Vocabulary.s_p_required_jack_restrictions)
    protected String jackRestrictions;

    @OWLDataProperty(iri = Vocabulary.s_p_time_estimate_in_hours)
    protected Double averageTime;

    @Transient
    protected List<TaskExecutionStatistics> executionStatistics;

    public TaskType() {
    }

    public TaskType(URI entityURI) {
        this.entityURI = entityURI;
    }

    public TaskType(TaskType t){
        this(t.getCode(), t.getTitle(), t.getTaskcat(), t.getAcmodel());
    }

    public TaskType(String code){
        this(code, code);
    }
    public TaskType(String code, String title) {
        this.code = code;
        this.setId(code);
        this.title = title;
        this.viewLabel = code + title != null ? "\n" + title : "";
    }

    public TaskType(String code, String title, String taskcat, String acmodel) {
        this(code, title);
        this.taskcat = taskcat;
        this.acmodel = acmodel;
    }

//    public String getId() {
//        return id;
//    }
    public String getTCOrMPDCode(){
        return code != null ? code : mpdtask;
    }

    public String getElPowerRestrictions() {
        return elPowerRestrictions;
    }

    public void setElPowerRestrictions(String elPowerRestrictions) {
        this.elPowerRestrictions = elPowerRestrictions;
    }

    public String getHydPowerRestrictions() {
        return hydPowerRestrictions;
    }

    public void setHydPowerRestrictions(String hydPowerRestrictions) {
        this.hydPowerRestrictions = hydPowerRestrictions;
    }

    public String getJackRestrictions() {
        return jackRestrictions;
    }

    public void setJackRestrictions(String jackRestrictions) {
        this.jackRestrictions = jackRestrictions;
    }

    public List<Pair<String,String>> getRestrictions(){
        return Stream.<Pair<String, Function<TaskType, String>>>of(
                        Pair.of(Vocabulary.s_c_el_dot__power, TaskType::getElPowerRestrictions),
                        Pair.of(Vocabulary.s_c_hyd_dot__power, TaskType::getHydPowerRestrictions),
                        Pair.of(Vocabulary.s_c_jack, TaskType::getJackRestrictions))
                .map(p -> Pair.of(p.getKey(), p.getValue().apply(this)))
                .filter(p -> p.getValue() != null)
                .collect(Collectors.toList());
    }

    public Set<MaintenanceGroup> getScopes() {
        return scopes;
    }

    public void setScopes(Set<MaintenanceGroup> scopes) {
        this.scopes = scopes;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public MaintenanceGroup getScope() {
        return scope;
    }

    public void setScope(MaintenanceGroup scope) {
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

    public String getCode() {
        return code;
    }

    public String typeLabel () {
        return getCode();
    }

    public void setDefinition(TaskType definition) {
        this.definition = definition;
    }

    public TaskType getDefinition() {
        return definition;
    }

    public Double getAverageTime() {
        return averageTime;
    }

    public void setAverageTime(Double averageTime) {
        this.averageTime = averageTime;
    }

    public List<TaskExecutionStatistics> getExecutionStatistics() {
        return executionStatistics;
    }

    public void setExecutionStatistics(List<TaskExecutionStatistics> executionStatistics) {
        this.executionStatistics = executionStatistics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskType)) return false;
        TaskType taskType = (TaskType) o;

        if(getCode() != null)
            return getCode().equals(taskType.getCode());
        if(getEntityURI() != null)
            return getEntityURI().equals(taskType.getEntityURI());

        return false;
    }

    @Override
    public int hashCode() {
        Object id = Optional.<Object>ofNullable(getCode()).orElse(getEntityURI());
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getTCOrMPDCode();
    }

    public static int is_TCCode_Match_v3(String sl, String sr){
        if(sl == null)
            return -1;
        String slFixed = prepareTCDef_Code(sl);
        return isStrMatch_v3(slFixed, sr) ? slFixed.length() : -1;
    }

    public static String prepareTCDef_Code(String s){
        return s.endsWith("/1.0") ? s.substring(0, s.length() - 4) : s;
    }

    /**
     * Checks if sl is a prefix of sr and if the character after the prefix is not alphanumeric.
     * @param sl
     * @param sr
     * @return
     */
    public static boolean isStrMatch_v3(String sl, String sr){
        // TODO - this is a simplified match, implement with split and test that it does not fail
        return sr.startsWith(sl) && (sr.length() == sl.length() || (
                !Character.isDigit(sr.charAt(sl.length())) &&
                        !Character.isAlphabetic(sr.charAt(sl.length()))
        )
        );
    }

//    public static TaskType getTaskTypeDefinition(Result r){
//        if(r == null)
//            return null;
//
//        return getTaskTypeDefinition(r.taskType);
//    }

//    public static TaskType getTaskTypeDefinition(TaskType tt){
//        if(tt == null || tt.getCode() == null)
//            return null;
//
//        List<TaskType> res = taskTCCode2TCDefinitionMap.get(tt.getCode());
//        return Optional.ofNullable(res).map(l -> l.isEmpty() ? null : l.get(0)).orElse(null);
//    }


}
