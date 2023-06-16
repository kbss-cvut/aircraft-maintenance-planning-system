package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.model.base.LongIntervalImpl;
import cz.cvut.kbss.amaplas.model.values.DateParserSerializer;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.Transient;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@OWLClass(iri = Vocabulary.s_c_work_session)
public class WorkSession extends Event {
    /** The id of the work session log */

    @OWLDataProperty(iri = Vocabulary.s_p_id)
    protected String id;

//    protected String sessionURI; // TODO - refactor to entityURI

    @OWLDataProperty(iri = Vocabulary.s_p_task_description)
    protected String description;


    @OWLDataProperty(iri = Vocabulary.s_p_has_shift)
    protected String shiftGroup;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_scope)
    protected MaintenanceGroup scope;



    @OWLObjectProperty(iri = Vocabulary.s_p_performed_by)
    protected Mechanic mechanic;
//    protected Double estMin;

    @OWLObjectProperty(iri = Vocabulary.s_p_is_part_of_maintenance_task)
    protected TaskExecution taskExecution;

    public WorkSession() {
    }

    public WorkSession(WorkSession r) {
        this.scope = r.scope;
        this.date = r.date;
        this.start = r.start;
        this.end = r.end;
        this.dur = r.dur;
    }

    public WorkSession getWrapped(){
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Mechanic getMechanic() {
        return mechanic;
    }

    public void setMechanic(Mechanic mechanic) {
        this.mechanic = mechanic;
    }

    public TaskExecution getTaskExecution() {
        return taskExecution;
    }

    public void setTaskExecution(TaskExecution taskExecution) {
        this.taskExecution = taskExecution;
    }

    public static List<String> cols() {
        return Stream.of(Result.class.getDeclaredFields()).map(f -> f.getName()).collect(Collectors.toList());
    }

    public static String header() {
        return header(",");
    }

    public static String header(String sep) {
        return cols().stream().collect(Collectors.joining(sep));
    }



    @Override
    public String toString() {
        return toString(",");
    }

    public String toString(String sep) {
        return Stream.of(entityURI.toString(), description, scope.applicationType,
                        date, start.toString(), end.toString(), dur.toString())
                .collect(Collectors.joining(sep));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Result)) return false;
        WorkSession workSession = (WorkSession) o;
        return Objects.equals(entityURI, workSession.entityURI) &&
                taskExecution.equals(workSession.taskExecution) &&
                start.equals(workSession.start) &&
                end.equals(workSession.end);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShiftGroup() {
        return shiftGroup;
    }

    public void setShiftGroup(String shiftGroup) {
        this.shiftGroup = shiftGroup;
    }

    public MaintenanceGroup getScope() {
        return scope;
    }

    public void setScope(MaintenanceGroup scope) {
        this.scope = scope;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityURI, taskExecution, start, end);
    }

    /**
     *
     * @return id from wp, scope, shiftGroup, taskType.type, start and end
     */
    public String form0(){
        return String.join(",", scope.getAbbreviation(), shiftGroup, taskExecution.entityURI.toString(), DateParserSerializer.formatDate(start), DateParserSerializer.formatDate(end));
    }

    /**
     *
     * @return id from wp, scope, taskType.type, start and end
     */
    public String form1(){
        return String.join(",", scope.getAbbreviation(), taskExecution.entityURI.toString(), DateParserSerializer.formatDate(start), DateParserSerializer.formatDate(end));
    }

    /**
     *
     * @return id from wp, scope, taskType.type and start
     */
    public String form2(){
        return String.join(",", scope.getAbbreviation(), taskExecution.entityURI.toString(), DateParserSerializer.formatDate(start));
    }

    private LongIntervalImpl asDateInterval(){
        return new LongIntervalImpl(start, end);
    }


    /**
     *
     * @param s1
     * @param s2
     * @return > 0 the length of the overlap interval, 0 if they touch and their bounds and
     * < 0 the shortest distance between the two intervals' bounds
     */
    public static long overlap(Result s1, Result s2){
        return Math.min(s1.getEnd(), s2.getEnd()) - Math.max(s1.getStart(), s2.getStart());
    }

    /**
     *
     * @param sessions assumes the sessions are ordered by start
     * @return
     */
    public static List<LongIntervalImpl> mergeOverlaps(List<WorkSession> sessions){
        List<LongIntervalImpl> intervals = new ArrayList<>(sessions.size());
        for(WorkSession s: sessions){
            intervals.add(s.asDateInterval());
        }
        return LongIntervalImpl.mergeOverlaps(intervals);
    }


//    public static void normalizeTaskTypes(List<Result> results){
//        normalizeTaskTypeLabels(results);
//        normalizeScopes(results);
//    }

//    /**
//     * Set the scope of  each Result (work session) to the main scope of the task card using the heuristic that the main
//     * scope is the one which is used the most.
//     * @param results
//     */
//    public static void normalizeScopes(List<Result> results){
//        results.stream().collect(Collectors.groupingBy(r -> r.taskType)).entrySet().stream()
//                .forEach(e ->
//                        e.getKey().scope = e.getValue().stream().collect(Collectors.groupingBy(r -> r.scope)).entrySet().stream()
//                                .max(Comparator.comparing(ee -> ee.getValue().size()))
//                                .map(ee -> ee.getKey()).orElseGet(null)
//                );
//    }

    /**
     * Create a normalized set of unique task types with normalized codes, labels and scopes (main scopes) using the
     * method TaskType.normalizeTaskTypes. Replace references to task types in results with the normalized the task types.
     *
     * @param results
     */
    public static void normalizeTaskTypeLabels(List<Result> results){
        Map<String, TaskType> taskTypeMap = TaskType.normalizeTaskTypes(
                results.stream()
                        .map(r -> r.taskType)
                        .filter(t -> t != null).collect(Collectors.toList())
        );

        // fix records using the same taskTypeInstance
        results.forEach(r -> r.taskType = taskTypeMap.get(r.taskType.getCode()));
    }

    public static Function<Result, Long> startTimeMilSec = r -> r.start.getTime();
    public static ToLongFunction<Result> startTimeSec = r -> r.start.getTime()/1000;
    public static Comparator<Result> startComparator = Comparator.comparing(startTimeMilSec);
    public static Function<Result, String> key_WP_TaskTypeCode_ScopeCode = r -> r.wp + "," + r.taskType.getCode() + "," + r.scope;
    public static Predicate<Result> isMainScopeSession =
            r -> r.taskType != null && r.taskType.getScope() != null && r.taskType.getCode() != null && r.scope.equals(r.taskType.getScope());
    public static Function<Result, Object> key_TaskTypeCode_ScopeCode = r -> r.taskType.getCode() + r.scope;
    public static Function<Result, Object> mainScopeKey_TaskTypeCode_ScopeCode = r -> isMainScopeSession.test(r) ? key_TaskTypeCode_ScopeCode.apply(r) : null;
    public static ToLongFunction<Result> durationMilSec = r -> (r.end.getTime() - r.start.getTime());
    public static ToLongFunction<Result> durationSec = r -> (r.end.getTime() - r.start.getTime())/1000;

}
