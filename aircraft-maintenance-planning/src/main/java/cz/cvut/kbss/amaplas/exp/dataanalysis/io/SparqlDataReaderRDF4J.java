package cz.cvut.kbss.amaplas.exp.dataanalysis.io;

import cz.cvut.kbss.amaplas.exp.common.ResourceUtils;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.AircraftType;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpRetryException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SparqlDataReaderRDF4J {

    private static final Logger LOG = LoggerFactory.getLogger(SparqlDataReaderRDF4J.class);


    public List<String> readRowsAsStrings(String queryName, String endpoint){
        HTTPRepository r = new HTTPRepository(endpoint);

        RepositoryConnection c = r.getConnection();
        String query = ResourceUtils.loadResource(queryName);
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
        r.shutDown();
        return ret;
    }

    public List<Result> readData(String query, String endpoint){
        HTTPRepository r = new HTTPRepository(endpoint);

        RepositoryConnection c = r.getConnection();
        TupleQuery q = c.prepareTupleQuery(query);
        TupleQueryResult rs = q.evaluate();
//        res.
        LOG.debug("converting time log instances ...");
        List<Result> ret = convertToTimeLog(rs);
        // close connection
        c.close();
        r.shutDown();
        return ret;
    }

    public List<Result> readDataNamedQuery(String queryName, String endpoint){
        LOG.debug("executing query \"{}\" at endpoint <{}>...", queryName, endpoint);
        String query = ResourceUtils.loadResource(queryName);
        return readData(query, endpoint);
    }



    public static List<Result> convertToTimeLog(TupleQueryResult rs){

        List<Result> results = convert(rs, SparqlDataReaderRDF4J::convertToTimeLog);
//        Result.normalizeTaskTypeLabels(results);
        Result.normalizeTaskTypes(results);
        return results.stream().collect(Collectors.groupingBy(Result::form0))
                .entrySet().stream()
                .map(e -> e.getValue().get(0))
                .collect(Collectors.toList());
    }

    public static <T> List<T> convert(TupleQueryResult rs, SparqlDataReaderRDF4J.Converter<T> converter){
        List<T> results = new ArrayList<>();
        while(rs.hasNext()){
            BindingSet bs = rs.next();
            try {
                results.add(converter.convert(bs));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    public static Result convertToTimeLog(BindingSet bs) throws ParseException {
        TaskType taskType = new TaskType(
                bs.getValue("type").stringValue(),
                bs.getValue("typeLabel").stringValue(),
                bs.getValue("taskcat").stringValue()
        );

        Result t = new Result();

        t.wp = bs.getValue("wp").stringValue();
        t.acmodel = bs.getValue("acmodel").stringValue();
        t.acType = AircraftType.getTypeLabelForModel(t.acmodel);
        t.taskType = taskType;


        t.scope = bs.getValue("scope").stringValue();
        t.date = bs.getValue("date").stringValue();

        String start = bs.getValue("start").stringValue();
//        start = start.substring(0, start.length()-1);
        String end = bs.getValue("end").stringValue();
//        end = end.substring(0, end.length()-1);
        t.start = SparqlDataReader.df.parse(start + "+0200");
        t.end = SparqlDataReader.df.parse(end + "+0200");
//        Value v = new
        t.dur = ((Literal)bs.getValue("dur")).longValue();
        return t;
    }

//    public String asString(BindingSet bs, String name){
//        return if(bs.getValue("wp").stringValue()
//    }


    public static interface Converter<T>{
        T convert(BindingSet bs) throws Exception;
    }

    public static void main(String[] args) {
        List<Result> results = new SparqlDataReaderRDF4J().readDataNamedQuery(
                SparqlDataReader.DA_TASK_SO_WITH_WP_SCOPE,
                "http://localhost:7200/repositories/csat-data-02"
        );

    }

}