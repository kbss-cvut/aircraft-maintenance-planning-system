package cz.cvut.kbss.amaplas.io;

import cz.cvut.kbss.amaplas.model.Mechanic;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.amaplas.utils.ResourceUtils;
import cz.cvut.kbss.amaplas.model.AircraftType;
import cz.cvut.kbss.amaplas.model.Result;
import cz.cvut.kbss.amaplas.model.TaskType;
import cz.cvut.kbss.amaplas.utils.RepositoryUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SparqlDataReaderRDF4J {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlDataReaderRDF4J.class);

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

        LOG.debug("converting time log instances ...");
        List<String> names = rs.getBindingNames();
        List<String> ret = rs.stream().map(bs ->
                names.stream().map(n -> bs.getValue(n).stringValue()).collect(Collectors.joining(", "))
                ).collect(Collectors.toList());
        // close connection
        c.close();
        return ret;
    }

    public static <T> List<T> executeNamedQuery(String queryName, Map<String, Value> bindings, String endpoint, String username, String password, SparqlDataReaderRDF4J.Converter converter) {
        LOG.debug("executing query \"{}\" at endpoint <{}> with bindings {}", queryName, endpoint, bindings);
        String query = ResourceUtils.loadResource(queryName);
        List<T> results = executeQuery(query, bindings, endpoint, username, password, converter);
        return results;
    }

    public static <T> List<T> executeQuery(String query, Map<String, Value> bindings, String endpoint, String username, String password, SparqlDataReaderRDF4J.Converter converter) {
        Repository r = RepositoryUtils.createRepo(endpoint, username, password);
        List<T> result = executeQuery(query, bindings, r, converter);
        r.shutDown();
        return result;
    }

    public static <T> List<T> executeQuery(String query, Map<String, Value> bindings, Repository r, SparqlDataReaderRDF4J.Converter converter){
        long time = System.currentTimeMillis();
        RepositoryConnection c = r.getConnection();
        TupleQuery q = c.prepareTupleQuery(query);
        if(bindings != null)
            bindings.entrySet().stream().forEach(b -> q.setBinding(b.getKey(), b.getValue()));
        TupleQueryResult rs = q.evaluate();

        LOG.debug("converting query results ...");
        List<T> ret = convert(rs, converter);
        c.close();
        LOG.info("query executed in {} seconds", ((double)(System.currentTimeMillis() - time)/1000.0));
        return ret;
    }

    public static void persistStatements(Collection<Statement> statements, String graphURL, String endpoint, String username, String password){
        LOG.info("persisting {} statements  to graph <{}> in endpoint <{}>", statements.size(), graphURL, endpoint);
        long time = System.currentTimeMillis();

        Repository r = RepositoryUtils.createRepo(endpoint, username, password);
        RepositoryConnection c = r.getConnection();
        c.begin();
        if (graphURL != null) {
            Resource graph = r.getValueFactory().createIRI(graphURL);
            c.add(statements, graph);
        } else {
            c.add(statements);
        }
        c.commit();
        c.close();
        r.shutDown();
        LOG.info("statements persisted in {} seconds", ((double)(System.currentTimeMillis() - time)/1000.0));
    }

    public static List<Statement> convertTaskCardAsStatement(Map<String, List<TaskType>> map){
        ValueFactory f = SimpleValueFactory.getInstance();
        IRI hasSpecificTaskWithId = f.createIRI(Vocabulary.s_p_has_specific_task_with_id);
        return map.entrySet().stream().
                filter(e -> !e.getValue().isEmpty()).
                map(e -> f.createStatement(
                        f.createIRI(e.getValue().get(0).getEntityURI().toString()),
                        hasSpecificTaskWithId,
                        f.createLiteral(e.getKey())
                        )
                ).collect(Collectors.toList());
    }

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
    public List<TaskType> readTaskTypes(String queryName, Map<String, Value> bindings, String endpoint, String username, String password, SparqlDataReaderRDF4J.Converter<TaskType> converter){
        LOG.debug("executing query \"{}\" at endpoint <{}> with bindings {}", queryName, endpoint, bindings);
        String query = ResourceUtils.loadResource(queryName);
        List<TaskType> results = executeQuery(query, bindings, endpoint, username, password, converter);
        return results;
    }

    public List<TaskType> readTaskTypes(String queryName, Map<String, Value> bindings, Repository r, SparqlDataReaderRDF4J.Converter<TaskType> converter){
        LOG.debug("executing query \"{}\" at endpoint <{}> with bindings {}", queryName, r.getDataDir(), bindings);
        String query = ResourceUtils.loadResource(queryName);
        List<TaskType> results = executeQuery(query, bindings, r, converter);
        return results;
    }

    public List<TaskType> readTaskDefinitions(String queryName, Map<String, Value> bindings, String endpoint, String graph, String username, String password, SparqlDataReaderRDF4J.Converter<TaskType> converter){
        LOG.debug("executing query \"{}\" at endpoint <{}> with bindings {}", queryName, endpoint, bindings);
        String query = ResourceUtils.loadResource(queryName);
        // Set graph parameter in query
        query = query.replaceAll("\\?taskTypeDefinitionGraph", String.format("<%s>",graph));
        List<TaskType> results = executeQuery(query, bindings, endpoint, username, password, converter);
        return results;
    }

    public List<TaskType> readTaskDefinitions(String queryName, Map<String, Value> bindings, Repository r, String graph, SparqlDataReaderRDF4J.Converter<TaskType> converter){
        LOG.debug("executing query \"{}\" at endpoint <{}> with bindings {}", queryName, r.getDataDir(), bindings);
        String query = ResourceUtils.loadResource(queryName);
        // Set graph parameter in query
        query = query.replaceAll("\\?taskTypeDefinitionGraph", String.format("<%s>", graph));
        List<TaskType> results = executeQuery(query, bindings, r, converter);
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

        mandatory(bs, "taskCard", s -> taskType.setEntityURI(URI.create(s)));
        optional(bs, "MPDTASK", taskType::setMpdtask);
        optional(bs, "team", taskType::setScope);
        optional(bs, "phase", taskType::setPhase);
        optional(bs, "taskType", taskType::setTaskType);
        optional(bs, "area", taskType::setArea);
        optional(bs, "elPower", taskType::setElPowerRestrictions);//?elPower ?hydPower ?jacks
        optional(bs, "hydPower", taskType::setHydPowerRestrictions);
        optional(bs, "jacks", taskType::setJackRestrictions);
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



    public List<Result> readSessionLogsWithNamedQuery(String queryName, Map<String, Value> bindings, String endpoint, String username, String password){
        LOG.info("executing query \"{}\" at endpoint <{}> with bindings {}", queryName, endpoint, bindings);
        String query = ResourceUtils.loadResource(queryName);
        List<Result> results = executeQuery(query, bindings, endpoint, username, password, SparqlDataReaderRDF4J::convertToTimeLog);

        return results.stream().collect(Collectors.groupingBy(Result::form0))
                .entrySet().stream()
                .map(e -> e.getValue().get(0))
                .collect(Collectors.toList());
    }

    protected static HashMap<String, String > taskCategories = new HashMap<>(){
        {
            put("TC", "task-card");
            put("M", "maintenance-work-order");
            put("S", "scheduled-work-order");
        }
    };
    protected static Pattern taskTypeIRIPattern = Pattern.compile("task-type--([^-]+)--(.+)");
    protected static Pattern mechanicIRI_IDPattern = Pattern.compile("^.+/mechanic--(.+)$");
    public static Result convertToTimeLog(BindingSet bs) throws ParseException {
        // TODO - do not create task type with null type string.
        String def = "unknown";
        String wp = bs.getValue("wp").stringValue();
        String tt = optValue(bs, "tt", null);
        String tType = optValue(bs, "tType", null);
        String defaultTaskType = def;
        String defaultTaskCategory = def;
        if(tType != null){
            Matcher m = taskTypeIRIPattern.matcher(tType);
            if(m.find()) {
                defaultTaskCategory = taskCategories.getOrDefault(m.group(1), def);
                defaultTaskType = m.group(2);
            }
        }
        TaskType taskType = new TaskType(
                optValue(bs,"type", defaultTaskType),
                optValue(bs, "typeLabel", defaultTaskType),
                optValue(bs, "taskcat", defaultTaskCategory),
                optValue(bs,"acmodel", def)
        );

        // create mechanic
        String mechanicIRI = optValue(bs, "w", null);
        String mechanicID = optValue(bs, "wId", null);
        String mechanicLabel = optValue(bs, "wLabel", null);
        Mechanic mechanic = null;
        if(mechanicIRI != null){
            mechanic = new Mechanic();
            mechanic.setEntityURI(URI.create(mechanicIRI));
            if(mechanicID == null){
                Matcher m = mechanicIRI_IDPattern.matcher(mechanicIRI);
                if(m.matches()){
                    mechanicID = m.group(1);
                }
            }
            mechanic.setId(mechanicID);
            if(mechanicID != null){
                mechanic.setTitle(mechanicID);
            }
            mechanic.setTitle(mechanicLabel != null ? mechanicLabel : mechanicID);
        }

        // create a work session record
        Result t = new Result();

        t.wp = wp;
        t.acmodel = taskType.getAcmodel();
        t.acType = AircraftType.getTypeLabelForModel(t.acmodel);
        t.taskType = taskType;
        t.setMechanic(mechanic);

        t.scope = optValue(bs, "scope", def);
        t.date = optValue(bs, "date", def);

        String start = optValue(bs, "start", null);
        String end = optValue(bs, "end", null);
        if(start != null )
            t.start = SparqlDataReader.parseDate(SparqlDataReader.df, start.substring(0, start.length() -1) + "+0200");
        if(end != null)
            t.end = SparqlDataReader.parseDate(SparqlDataReader.df, end.substring(0, end.length() -1) + "+0200");

        t.dur = Optional.ofNullable(bs.getValue("dur")).map(v -> ((Literal)v).longValue()).orElse(null);
        return t;
    }

    public static Pair<String, String> convertToPair(BindingSet bs) {
        return Pair.of(manValue(bs, "tcId"), manValue(bs, "tcdId"));
    }

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

        public QueryResultConsumer() {}

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
                null,
                url,
                graph,
                username, password,
                SparqlDataReaderRDF4J::convertToTaskTypeDefinition
        );
        return taskTypes;
    }
}