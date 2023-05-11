package cz.cvut.kbss.amaplas.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparqlDataReader {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlDataReader.class);

    public static final String TASK_CARD_MAPPINGS = "/queries/analysis/task-card-mappings.sparql";
    public static final String DA_TASK_SO_WITH_WP_SCOPE = "/queries/analysis/task-start-order-with-wp-and-scope.sparql";
    public static final String DA_TASK_SO_WITH_WP_SCOPE_OLD = "/queries/analysis/task-start-order-with-wp-and-scope-old.sparql";
    public static final String WP = "/queries/analysis/wp.sparql";
    public static final String WP_HEADER = "/queries/analysis/wp-header.sparql";
    public static final String WP_IDS = "/queries/analysis/wp-ids.sparql";
    public static final String TASK_TYPES = "/queries/task-types.sparql";
    public static final String ALL_TASKS = "/queries/analysis/all-tasks-strat-order.sparql";
    public static final String TASK_CARDS_FROM_HISTORY = "/queries/analysis/task-cards-from-history.sparql";




    public static final String SIMILAR_WPS = "/queries/analysis/similar-wps.sparql";
    public static final String TASK_EXECUTION_STATISTICS_FROM_PARTS = "/queries/analysis/task-execution-statistics-from-parts.sparql";


    // TODO - refactoring in progress - delete
//    public List<TaskType> readTaskTypes(String endpoint){
//        String queryName = TASK_TYPES;
//        LOG.debug("executing query \"{}\" at endpoint <{}>...", queryName, endpoint);
//        String query = ResourceUtils.loadResource(queryName);
//        ResultSet rs = QueryExecutionFactory
//                .sparqlService(endpoint, query)
//                .execSelect();
//
//        return convertToTaskType(rs);
//    }

    // TODO - refactoring in progress - delete
//    public List<Result> readDataNamedQuery(String queryName, String endpoint){
//        LOG.debug("executing query \"{}\" at endpoint <{}>...", queryName, endpoint);
//        String query = ResourceUtils.loadResource(queryName);
//        return readData(query, endpoint);
//    }

    // TODO - refactoring in progress - delete
//    public List<Result> readData(String query, String endpoint){
//        ResultSet rs = QueryExecutionFactory
//                .sparqlService(endpoint, query)
//                .execSelect();
//        LOG.debug("converting time log instances ...");
//        return convertToTimeLog(rs);
//    }

    // TODO - refactoring in progress - delete
//    public static List<TaskType> convertToTaskType(ResultSet rs){
//        List<TaskType> results = convert(rs, SparqlDataReader::convertToTaskType);
//        return new ArrayList<>(TaskType.normalizeTaskTypes(results).values());
//    }

    // TODO - refactoring in progress - delete
//    public static TaskType convertToTaskType(QuerySolution qs){
//        TaskType taskType = new TaskType(
//                qs.get("typeId").toString(),
//                qs.get("typeLabel").toString()
//        );
//        taskType.setCode(qs.get("id").toString());
//        if(qs.contains("taskcat")){
//            taskType.setTaskcat(qs.get("taskcat").toString());
//        }
//        return taskType;
//    }

    // TODO - refactoring in progress - delete
//    public static List<Result> convertToTimeLog(ResultSet rs){
//        List<Result> results = convert(rs, SparqlDataReader::convertToTimeLog);
//        Result.normalizeTaskTypes(results);
//        return results.stream().collect(Collectors.groupingBy(Result::form0))
//                .entrySet().stream()
//                .map(e -> e.getValue().get(0))
//                .collect(Collectors.toList());
//    }

    // TODO - refactoring in progress
//    public static Result convertToTimeLog(QuerySolution qs) throws ParseException {
//        TaskType taskType = new TaskType(
//                qs.get("type").toString(),
//                qs.get("typeLabel").toString(),
//                qs.get("taskcat").toString(),
//                qs.get("acmodel").toString()
//        );
//
//        Result t = new Result();
//
//        t.wp = qs.get("wp").toString();
//        t.acmodel = taskType.getAcmodel();
//        t.acType = AircraftType.getTypeLabelForModel(t.acmodel);
//        t.taskType = taskType;
//
//
//        t.scope = qs.get("scope").toString();
//        t.date = qs.get("date").toString();
//
//        String start = qs.get("start").toString();
//        start = start.substring(0,start.length()-1);
//        String end = qs.get("end").toString();
//        end = end.substring(0,end.length()-1);
//        t.start = df.get().parse(start + "+0200");
//        t.end = df.get().parse(end + "+0200");
//
//        t.dur = qs.get("dur").asLiteral().getLong();
//        return t;
//    }

    // TODO - refactoring in progress - delete
//    public static <T> List<T> convert(ResultSet rs, Converter<T> converter){
//        List<T> results = new ArrayList<>();
//        while(rs.hasNext()){
//            QuerySolution qs = rs.next();
//            try {
//                results.add(converter.convert(qs));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        return results;
//    }

    // TODO - refactoring in progress - delete
//    public static interface Converter<T>{
//        T convert(QuerySolution qs) throws Exception;
//    }
}
