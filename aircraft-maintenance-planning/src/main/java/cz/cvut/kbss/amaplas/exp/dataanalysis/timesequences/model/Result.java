package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.ExtractData;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Result {
    public String wp;
    public String type;
    public String scope;
    public String taskcat;
    public String date;
    public Date start;
    public Date end;
    public Long dur;


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
        return Stream.of(wp, type, scope, taskcat, date, start.toString(), end.toString(), dur.toString()).collect(Collectors.joining(sep));
    }
}
