package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import cz.cvut.kbss.amaplas.exp.dataanalysis.io.SparqlDataReader;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Result{
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
        return Stream.of(wp, acmodel, taskType.type, taskType.label, taskType.taskcat, scope, date, start.toString(), end.toString(), dur.toString()).collect(Collectors.joining(sep));
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
        return String.join(",", wp, scope, shiftGroup, taskType.type, SparqlDataReader.df.format(start), SparqlDataReader.df.format(end));
    }

    /**
     *
     * @return id from wp, scope, taskType.type, start and end
     */
    public String form1(){
        return String.join(",", wp, scope, taskType.type, SparqlDataReader.df.format(start), SparqlDataReader.df.format(end));
    }

    /**
     *
     * @return id from wp, scope, taskType.type and start
     */
    public String form2(){
        return String.join(",", wp, scope, taskType.type, SparqlDataReader.df.format(start));
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
        results.forEach(r -> r.taskType = taskTypeMap.get(r.taskType.type));
        // DEBUG
        List<Result> resultsNoTaskTypes2 = results.stream().filter(r -> r.taskType == null).collect(Collectors.toList());
        if(resultsNoTaskTypes2.size() != resultsNoTaskTypes.size()){
            throw new RuntimeException("error in normalizeTaskTypeLabels!");
        }

    }
}
