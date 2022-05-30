package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.io.SparqlDataReader;
import cz.cvut.kbss.amaplas.model.base.LongIntervalImpl;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Task based
 */
public class Result{
    /** The id of the work session log */
    public String id;
    public String wp;
    public String acType = "NONE";
    public String acmodel;
    public TaskType taskType;
    public String shiftGroup;
    public String scope;
    public String date;
    public Date start;
    public Date end;
    public Long dur;
    public Mechanic mechanic;
//    public int removed;

    public Result() {
    }
    
    public Result(Result r) {
        this.wp = r.wp;
        this.acmodel = r.acmodel;
        this.taskType = r.taskType;
        this.scope = r.scope;
        this.date = r.date;
        this.start = r.start;
        this.end = r.end;
        this.dur = r.dur;
        AircraftType at = AircraftType.modelMap.get(this.acmodel);
        this.acType = at != null ? at.getType() : "NONE";
    }

    public Result getWrapped(){
        return this;
    }

    public long getStart() {
        return start.getTime();
    }

    public long getEnd(){
        return end.getTime();
    }

    public long getLength(){
        return getEnd() - getStart();
    }

    public Mechanic getMechanic() {
        return mechanic;
    }

    public void setMechanic(Mechanic mechanic) {
        this.mechanic = mechanic;
    }

    public String taskType(){
        return taskType.getCode();
    }

    public boolean isMainScopeSession(){
        return scope.equals(taskType.getScope());
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
        return Stream.of(wp, acmodel, taskType.getCode(), taskType.getTitle(), taskType.getTaskcat(), scope, date, start.toString(), end.toString(), dur.toString()).collect(Collectors.joining(sep));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Result)) return false;
        Result result = (Result) o;
        return wp.equals(result.wp) && taskType.equals(result.taskType) && start.equals(result.start) && end.equals(result.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wp, taskType, start, end);
    }

    /**
     *
     * @return id from wp, scope, shiftGroup, taskType.type, start and end
     */
    public String form0(){
        return String.join(",", wp, scope, shiftGroup, taskType.getCode(), SparqlDataReader.df.format(start), SparqlDataReader.df.format(end));
    }

    /**
     *
     * @return id from wp, scope, taskType.type, start and end
     */
    public String form1(){
        return String.join(",", wp, scope, taskType.getCode(), SparqlDataReader.df.format(start), SparqlDataReader.df.format(end));
    }

    /**
     *
     * @return id from wp, scope, taskType.type and start
     */
    public String form2(){
        return String.join(",", wp, scope, taskType.getCode(), SparqlDataReader.df.format(start));
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
//        return s1.endTime() - s2.startTime();
    }

    /**
     *
     * @param sessions assumes the sessions are ordered by start
     * @return
     */
    public static List<LongIntervalImpl> mergeOverlaps(List<Result> sessions){
        List<LongIntervalImpl> intervals = new ArrayList<>(sessions.size());
        for(Result s: sessions){
            intervals.add(s.asDateInterval());
        }
        return LongIntervalImpl.mergeOverlaps(intervals);
    }


    public static void normalizeTaskTypes(List<Result> results){
        normalizeTaskTypeLabels(results);
        normalizeScopes(results);
    }

    public static void normalizeScopes(List<Result> results){
        results.stream().collect(Collectors.groupingBy(r -> r.taskType)).entrySet().stream()
                .forEach(e ->
                        e.getKey().scope = e.getValue().stream().collect(Collectors.groupingBy(r -> r.scope)).entrySet().stream()
                                .max(Comparator.comparing(ee -> ee.getValue().size()))
                                .map(ee -> ee.getKey()).orElseGet(null)
                );
    }

    public static void normalizeTaskTypeLabels(List<Result> results){
        Map<String, TaskType> taskTypeMap = TaskType.normalizeTaskTypes(
                results.stream()
                        .map(r -> r.taskType)
                        .filter(t -> t != null).collect(Collectors.toList())
        );
//        results.forEach(r ->
//                r.taskType.type = Optional
//                        .of(r.taskType.type)
//                        .map(l -> l.replace("%20", " "))
//                        .orElse(null)
//        );
//
//        results.stream()
//                .map(r -> r.taskType)
//                .collect(Collectors.groupingBy(t -> t.type)).entrySet().stream()
//                .map(e -> taskTypeMap.put(
//                        e.getKey(),
//                        e.getValue().stream().sorted(Comparator.comparing(t -> t.label.length())).findFirst().get()
//                ));

        // DEBUG
        List<Result> resultsNoTaskTypes = results.stream().filter(r -> r.taskType == null).collect(Collectors.toList());
        // fix records using the same taskTypeInstance
        results.forEach(r -> r.taskType = taskTypeMap.get(r.taskType.getCode()));
        // DEBUG
        List<Result> resultsNoTaskTypes2 = results.stream().filter(r -> r.taskType == null).collect(Collectors.toList());
        if(resultsNoTaskTypes2.size() != resultsNoTaskTypes.size()){
            throw new RuntimeException("error in normalizeTaskTypeLabels!");
        }

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


    // is function applicable, e.g. is the input in the domain?
    //
}
