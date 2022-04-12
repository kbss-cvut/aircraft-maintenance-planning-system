package cz.cvut.kbss.amaplas.exp.dataanalysis.io;

import cz.cvut.kbss.amaplas.exp.common.ResourceUtils;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.AircraftType;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SparqlDataReader {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlDataReader.class);

    public static final String DA_TASK_SO_WITH_WP_SCOPE = "/queries/analysis/task-start-order-with-wp-and-scope.sparql";
    public static final String DA_TASK_SO_WITH_WP_SCOPE_OLD = "/queries/analysis/task-start-order-with-wp-and-scope-old.sparql";
    public static final String WP = "/queries/analysis/wp.sparql";
    public static final String TASK_TYPES = "/queries/task-types.sparql";
    public static final String ALL_TASKS = "/queries/analysis/all-tasks-strat-order.sparql";
    public static final String TASK_CARDS_FROM_HISTORY = "/queries/analysis/task-cards-from-history.sparql";
    public static final String TASK_TYPES_DEFINITIONS = "/queries/analysis/task-types-definitions.sparql";
    public static final String dateFormatPattern1 = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String dateFormatPattern2 = "dd.MM.yyyy'T'HH:mm";
    public static final SimpleDateFormat df = new SimpleDateFormat(dateFormatPattern1);
    public static final SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");

    public List<TaskType> readTaskTypes(String endpoint){
        String queryName = TASK_TYPES;
        LOG.debug("executing query \"{}\" at endpoint <{}>...", queryName, endpoint);
        String query = ResourceUtils.loadResource(queryName);
        ResultSet rs = QueryExecutionFactory
                .sparqlService(endpoint, query)
                .execSelect();

        return convertToTaskType(rs);
    }

    public List<Result> readDataNamedQuery(String queryName, String endpoint){
        LOG.debug("executing query \"{}\" at endpoint <{}>...", queryName, endpoint);
        String query = ResourceUtils.loadResource(queryName);
        return readData(query, endpoint);
    }

    public List<Result> readData(String query, String endpoint){
        ResultSet rs = QueryExecutionFactory
                .sparqlService(endpoint, query)
                .execSelect();
        LOG.debug("converting time log instances ...");
        return convertToTimeLog(rs);
    }

    public static List<TaskType> convertToTaskType(ResultSet rs){
        List<TaskType> results = convert(rs, SparqlDataReader::convertToTaskType);
        return new ArrayList<>(TaskType.normalizeTaskTypes(results).values());
    }

    public static TaskType convertToTaskType(QuerySolution qs){
        TaskType taskType = new TaskType(
                qs.get("typeId").toString(),
                qs.get("typeLabel").toString()
        );
        taskType.setCode(qs.get("id").toString());
        if(qs.contains("taskcat")){
            taskType.setTaskcat(qs.get("taskcat").toString());
        }
        return taskType;
    }


    public static List<Result> convertToTimeLog(ResultSet rs){
//        List<Result> results = new ArrayList<>();
//        while(rs.hasNext()){
//            QuerySolution qs = rs.next();
//            try {
//                results.add(convertToTimeLog(qs));
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        }
        List<Result> results = convert(rs, SparqlDataReader::convertToTimeLog);
//        Result.normalizeTaskTypeLabels(results);
        Result.normalizeTaskTypes(results);
        return results.stream().collect(Collectors.groupingBy(Result::form0))
                .entrySet().stream()
                .map(e -> e.getValue().get(0))
                .collect(Collectors.toList());
    }



    public static Result convertToTimeLog(QuerySolution qs) throws ParseException {
        TaskType taskType = new TaskType(
                qs.get("type").toString(),
                qs.get("typeLabel").toString(),
                qs.get("taskcat").toString(),
                qs.get("acmodel").toString()
        );

        Result t = new Result();

        t.wp = qs.get("wp").toString();
        t.acmodel = taskType.getAcmodel();
        t.acType = AircraftType.getTypeLabelForModel(t.acmodel);
        t.taskType = taskType;


        t.scope = qs.get("scope").toString();
        t.date = qs.get("date").toString();

        String start = qs.get("start").toString();
        start = start.substring(0,start.length()-1);
        String end = qs.get("end").toString();
        end = end.substring(0,end.length()-1);
        t.start = df.parse(start + "+0200");
        t.end = df.parse(end + "+0200");

        t.dur = qs.get("dur").asLiteral().getLong();
        return t;
    }

    public static <T> List<T> convert(ResultSet rs, Converter<T> converter){
        List<T> results = new ArrayList<>();
        while(rs.hasNext()){
            QuerySolution qs = rs.next();
            try {
                results.add(converter.convert(qs));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return results;
    }

//    public static TaskType convertToTaskType(QuerySolution qs){
//        TaskType taskType = new TaskType(
//                qs.get("typeId").toString(),
//                qs.get("typeLabel").toString()
//        );
//        taskType.id = qs.get("id").toString();
//        if(qs.contains("taskcat")){
//            taskType.taskcat = qs.get("taskcat").toString();
//        }
//
//        return taskType;
//    }

    public static interface Converter<T>{
        T convert(QuerySolution qs) throws Exception;
    }

    public static void main(String[] args) throws ParseException {
        String date = "08.02.2017";
        String time = "06:47";
//        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy'T'HH:mm");
        Date d = df.parse(date + "T" + time );
        System.out.println(d.toString());
    }
}
