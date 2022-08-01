package cz.cvut.kbss.amaplas.io;

import cz.cvut.kbss.amaplas.utils.ResourceUtils;
import cz.cvut.kbss.amaplas.model.AircraftType;
import cz.cvut.kbss.amaplas.model.Result;
import cz.cvut.kbss.amaplas.model.TaskType;
import cz.cvut.kbss.amaplas.utils.RepositoryUtils;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SparqlDataReaderRDF4J {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlDataReaderRDF4J.class);
//    public static HTTPRepository createRepo(String endpoint, String username, String password){
//
//        HTTPRepository r = new HTTPRepository(endpoint);
//        if(username != null && password != null && !username.isEmpty() && !password.isEmpty());
//            r.setUsernameAndPassword(username, password);
//        return r;
//    }

    public List<String> readRowsAsStrings(String queryName, String endpoint, String username, String password) {
        Repository r = RepositoryUtils.createRepo(endpoint, username, password);


        List<String> results = readRowsAsStrings(queryName, r);
        // shutdown the repository
        r.shutDown();
        return results;
    }

    public List<String> readRowsAsStrings(String queryName, Repository r){
        String query = ResourceUtils.loadResource(queryName);
        RepositoryConnection c = r.getConnection();
        TupleQuery q = c.prepareTupleQuery(query);
        TupleQueryResult rs = q.evaluate();
//        res.
        LOG.debug("converting time log instances ...");
        List<String> names = rs.getBindingNames();
        List<String> ret = rs.stream().map(bs ->
                names.stream().map(n -> bs.getValue(n).stringValue()).collect(Collectors.joining(", "))
                ).collect(Collectors.toList());
        // close connection
        c.close();
        return ret;
    }

    public static <T> List<T> executeQuery(String query, String endpoint, String username, String password, SparqlDataReaderRDF4J.Converter converter) {
        Repository r = RepositoryUtils.createRepo(endpoint, username, password);
        List<T> result = executeQuery(query, r, converter);
        r.shutDown();
        return result;
    }

    public static <T> List<T> executeQuery(String query, Repository r, SparqlDataReaderRDF4J.Converter converter){
        RepositoryConnection c = r.getConnection();
        TupleQuery q = c.prepareTupleQuery(query);
        TupleQueryResult rs = q.evaluate();

        LOG.debug("converting query results ...");
        List<T> ret = convert(rs, converter);
        c.close();
        return ret;
    }


//    public List<Result> readData(String query, String endpoint){
//        HTTPRepository r = new HTTPRepository(endpoint);
//
//        RepositoryConnection c = r.getConnection();
//        TupleQuery q = c.prepareTupleQuery(query);
//        TupleQueryResult rs = q.evaluate();
////        res.
//        LOG.debug("converting time log instances ...");
//        List<Result> ret = convertToTimeLog(rs);
//        // close connection
//        c.close();
//        r.shutDown();
//        return ret;
//    }

//
//    private List<Result> convertToTimeLog(TupleQueryResult rs){
//
//        List<Result> results = convert(rs, SparqlDataReaderRDF4J::convertToTimeLog);
////        Result.normalizeTaskTypeLabels(results);
//        Result.normalizeTaskTypes(results);
//        return results.stream().collect(Collectors.groupingBy(Result::form0))
//                .entrySet().stream()
//                .map(e -> e.getValue().get(0))
//                .collect(Collectors.toList());
//    }
//

    public static <T> List<T> convert(TupleQueryResult rs, SparqlDataReaderRDF4J.Converter converter){
        List<T> results = new ArrayList<>();
        long numberOfIgnoredRecords = 0;
        while(rs.hasNext()){
            BindingSet bs = rs.next();
            try {
                results.add(((Converter<T>)converter).convert(bs));
            } catch (Exception e) {
                numberOfIgnoredRecords ++;
                LOG.trace("record is missing mandatory fields",e);
            }
        }
        if(numberOfIgnoredRecords > 0)
            LOG.warn("{} records where ignored due to missing mandatory fields");
        return results;
    }

    //////////////////////////
    // read and map to POJO //
    //////////////////////////

    /**
     *
     * @param queryName
     * @param endpoint
     * @return
     */
    public List<TaskType> readTaskTypes(String queryName, String endpoint, String username, String password, SparqlDataReaderRDF4J.Converter<TaskType> converter){
        LOG.debug("executing query \"{}\" at endpoint <{}>...", queryName, endpoint);
        String query = ResourceUtils.loadResource(queryName);
        List<TaskType> results = executeQuery(query, endpoint, username, password, converter);
        return results;
    }

    public List<TaskType> readTaskTypes(String queryName, Repository r, SparqlDataReaderRDF4J.Converter<TaskType> converter){
        LOG.debug("executing query \"{}\" on repository with datadir \"{}\"", queryName, r.getDataDir());
        String query = ResourceUtils.loadResource(queryName);
        List<TaskType> results = executeQuery(query, r, converter);
        return results;
    }

    public List<TaskType> readTaskDefinitions(String queryName, String endpoint, String graph, String username, String password, SparqlDataReaderRDF4J.Converter<TaskType> converter){
        LOG.debug("executing query \"{}\" at endpoint <{}>...", queryName, endpoint);
        String query = ResourceUtils.loadResource(queryName);
        // Set graph parameter in query
        query = query.replaceAll("\\?taskTypeDefinitionGraph", String.format("<%s>",graph));
        List<TaskType> results = executeQuery(query, endpoint, username, password, converter);
        return results;
    }

    public List<TaskType> readTaskDefinitions(String queryName, Repository r, String graph, SparqlDataReaderRDF4J.Converter<TaskType> converter){
        LOG.debug("executing query \"{}\"  on repository with datadir \"{}\"", queryName, r.getDataDir());
        String query = ResourceUtils.loadResource(queryName);
        // Set graph parameter in query
        query = query.replaceAll("\\?taskTypeDefinitionGraph", String.format("<%s>", graph));
        List<TaskType> results = executeQuery(query, r, converter);
        return results;
    }

    public static TaskType convertToTaskType(BindingSet bs) throws ParseException {
        // ?acmodel ?type ?description ?wpuri
        TaskType taskType = new TaskType(
                manValue(bs,"type"),
                optValue(bs, "typeLabel"),
                manValue(bs, "taskcat"),
                manValue(bs, "acmodel")
        );

        taskType.setCode(taskType.getCode().replaceFirst("[A-Z]+-]", ""));
        taskType.setCode(taskType.getCode().replaceFirst("[A-Z]+-]", ""));
        taskType.setAcmodel(taskType.getCode().replaceFirst("[A-Z]+-]", ""));

        return taskType;
    }

    /**
     * For Query SparqlDataReader.TASK_TYPES_DEFINITIONS
     * @param bs
     * @return
     */
    public static TaskType convertToTaskTypeDefinition(BindingSet bs){
        TaskType taskType = new TaskType(
                manValue(bs,"taskCardCode"),
                optValue(bs, "title"),
                "task_card",
                optValue(bs,"aircraftModel")
        );
        optional(bs, "MPDTASK", taskType::setMpdtask);
        optional(bs, "team", taskType::setScope);
        optional(bs, "phase", taskType::setPhase);
        optional(bs, "taskType", taskType::setTaskType);
        optional(bs, "area", taskType::setArea);
        return taskType;
    }

    public static <T> void optional(BindingSet bs, String name, Consumer<String> s){
        Optional.ofNullable(bs.getValue(name))
                .map(Value::stringValue)
                .ifPresent(s);
    }

    public static void mandatory(BindingSet bs, String name, Consumer<String> s){
        s.accept(bs.getValue(name).stringValue());
    }

    public static String manValue(BindingSet bs, String name){
        return bs.getValue(name).stringValue();
    }

    public static String optValue(BindingSet bs, String name){
        return optValue(bs, name, "");
    }

    public static String optValue(BindingSet bs, String name, String def){
        return Optional.ofNullable(bs.getValue(name)).map(Value::stringValue).orElse(def);
    }

    public List<Result> readSessionLogsWithNamedQuery(String queryName, String endpoint, String username, String password){
        LOG.info("executing query \"{}\" at endpoint <{}>...", queryName, endpoint);
        String query = ResourceUtils.loadResource(queryName);
        List<Result> results = executeQuery(query, endpoint, username, password, SparqlDataReaderRDF4J::convertToTimeLog);

        // fix data alignment
        Result.normalizeTaskTypes(results);
        return results.stream().collect(Collectors.groupingBy(Result::form0))
                .entrySet().stream()
                .map(e -> e.getValue().get(0))
                .collect(Collectors.toList());
    }

    public static Result convertToTimeLog(BindingSet bs) throws ParseException {
        TaskType taskType = new TaskType(
                manValue(bs,"type"),
                optValue(bs, "typeLabel"),
                optValue(bs, "taskcat"),
                optValue(bs,"acmodel")
        );

        Result t = new Result();

        t.wp = bs.getValue("wp").stringValue();
        t.acmodel = taskType.getAcmodel();
        t.acType = AircraftType.getTypeLabelForModel(t.acmodel);
        t.taskType = taskType;


        t.scope = bs.getValue("scope").stringValue();
        t.date = bs.getValue("date").stringValue();

        String start = bs.getValue("start").stringValue();
//        start = start.substring(0, start.length()-1);
        String end = bs.getValue("end").stringValue();
//        end = end.substring(0, end.length()-1);
        t.start = SparqlDataReader.df.parse(start.substring(0, start.length() -1) + "+0200");
        t.end = SparqlDataReader.df.parse(end.substring(0, end.length() -1) + "+0200");
//        Value v = new
        t.dur = ((Literal)bs.getValue("dur")).longValue();
        return t;
    }

//    public static <T> List<T> process(TupleQueryResult rs, QueryResultConsumer<T> queryResultConsumer) throws ParseException {
//        List<String> names = rs.getBindingNames();
//        while(rs.hasNext()){
//            BindingSet bs = rs.next();
//            queryResultConsumer.consume();
//            process(bs, names, consumers);
//        }
//    }
//
//    public static void process(BindingSet bs, List<String> names, ValueConsumer... consumers) throws ParseException {
//
//        int l = names.size();
//        if(l > consumers.length)
//            l = consumers.length;
//
//        for(int i = 0; i < l; i++){
//            String name = names.get(i);
//            Value value = bs.getValue(name);
//            ValueConsumer c = consumers[i];
//            process(value, name, c);
//        }
//    }
//
//    public static void process(Value val, String name, ValueConsumer c) throws ParseException{
//        if(c == null)
//            return;
//        String strVal = val.stringValue();
//        c.consume(strVal, name);
//    }


    public interface Converter<T>{
        T convert(BindingSet bs) throws Exception;
    }

    public static interface ValueConsumer{
        void consume(String value, String name) throws ParseException;
    }

    public static List<ValueConsumer> valueConsumers(Consumer<String> ... consumer){
        return Arrays.stream(consumer)
                .map(SparqlDataReaderRDF4J::valueConsumer)
                .collect(Collectors.toList());
    }

    public static ValueConsumer valueConsumer(Consumer<String> consumer){
        return new ValueConsumer() {
            @Override
            public void consume(String value, String name) throws ParseException {
                consumer.accept(value);
            }
        };
    }

    public static class QueryResultConsumer<T>{
        private Supplier<T> generator;
        private Function<T, List<ValueConsumer>> consumersGenerator;


        public QueryResultConsumer() {
//            new QueryResultConsumer<TaskType>(() -> new TaskType(), t ->
//                    valueConsumers(
//                            t::setType, t::setMpdtask, t::setLabel,
//                            t::setScope, t::setPhase, t::setTaskType,
//                            t::setAcmodel, t::setArea
//                    )
//            );
        }

        public QueryResultConsumer(Supplier<T> generator, Function<T, List<ValueConsumer>> consumersGenerator) {
            this.generator = generator;
            this.consumersGenerator = consumersGenerator;
        }

        public List<T> consume(TupleQueryResult rs) throws ParseException{
            List<T> ret = new ArrayList<>();
            List<String> names = rs.getBindingNames();
            while(rs.hasNext()){
                BindingSet bs = rs.next();
                T t = generator.get();
                List<ValueConsumer> consumers = consumersGenerator.apply(t);

                consume(bs, names, consumers);
            }
            return ret;
        }

        public void consume(BindingSet bs, List<String> names, List<ValueConsumer> consumers) throws ParseException{
            int l = names.size();
            if(l > consumers.size())
                l = consumers.size();

            for(int i = 0; i < l; i++){
                String name = names.get(i);
                Value value = bs.getValue(name);
                ValueConsumer c = consumers.get(i);
                consume(value, name, c);
            }
        }

        public void consume(Value val, String name, ValueConsumer c) throws ParseException{
            if(c == null)
                return;
            String strVal = val.stringValue();
            c.consume(strVal, name);
        }
    }

    public static List<TaskType> __loadTCDefinitions(String url, String graph, String username, String password){
        SparqlDataReaderRDF4J reader = new SparqlDataReaderRDF4J();
        List<TaskType> taskTypes = reader.readTaskDefinitions(
                SparqlDataReader.TASK_TYPES_DEFINITIONS,
                url,
                graph,
                username, password,
                SparqlDataReaderRDF4J::convertToTaskTypeDefinition
        );
//        taskTypes.forEach(t -> t.type = t.type.replaceFirst("/1.0",""));
//        taskTypes.forEach(t -> System.out.println(Stream.of(t.type, t.mpdtask, t.label, t.acmodel, t.area, t.phase, t.scope).collect(Collectors.joining(", "))));
        return taskTypes;
    }
}