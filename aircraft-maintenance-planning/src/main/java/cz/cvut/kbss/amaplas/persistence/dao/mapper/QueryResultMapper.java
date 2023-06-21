package cz.cvut.kbss.amaplas.persistence.dao.mapper;

import cz.cvut.kbss.amaplas.persistence.dao.BaseDao;
import cz.cvut.kbss.amaplas.utils.ResourceUtils;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class QueryResultMapper<T> {

    protected BaseDao baseDao;
    protected String queryName;
//    protected String query;
    protected BindingSet bs;// not thread safe

    public QueryResultMapper(String queryName) {
        this.queryName = queryName;
    }



    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getQuery() {
        return ResourceUtils.loadResource(queryName);
    }

    public void optional(String name, Consumer<String> s){
        Optional.ofNullable(bs.getValue(name))
                .map(Value::stringValue)
                .ifPresent(s);
    }


    public <A> void optional(String name, Function<String, A> converter, Consumer<A> s){
        Optional.ofNullable(bs.getValue(name))
                .map(Value::stringValue)
                .map(converter)
                .ifPresent(s);
    }

    public <A> void mandatory(String name, Function<String, A> converter, Consumer<A> s){
        s.accept(converter.apply(bs.getValue(name).stringValue()));
    }

    public void mandatory(String name, Consumer<String> s){
        s.accept(bs.getValue(name).stringValue());
    }

    public String manValue(String name){
        return bs.getValue(name).stringValue();
    }

    public <T> T manValue(String name, Function<String,T> converter){
        return converter.apply(bs.getValue(name).stringValue());
    }

    public String optValue(String name){
        return optValue(name, "");
    }

    public String optValue(String name, String def){
        return Optional.ofNullable(bs.getValue(name)).map(Value::stringValue).orElse(def);
    }

    public <T> T optValue(String name, Function<String, T> converter, T def){
        return Optional.ofNullable(bs.getValue(name)).map(Value::stringValue).map(converter).orElse(def);
    }

    public <T> T optValueNonString(String name, Function<Value, T> converter, T def){
        return Optional.ofNullable(bs.getValue(name)).map(converter).orElse(def);
    }

    public List<T> convert(Iterable<BindingSet> bindingSets){
        return convert(bindingSets.iterator());
    }

    public List<T> convert(Iterator<BindingSet> tupleQueryResult){
        List<T> results = new ArrayList<>();
        while(tupleQueryResult.hasNext()){
            bs = tupleQueryResult.next();
            T result = convert();
            if(result != null)
                results.add(result);
        }
        return results;
    }
    public abstract T convert();
}
