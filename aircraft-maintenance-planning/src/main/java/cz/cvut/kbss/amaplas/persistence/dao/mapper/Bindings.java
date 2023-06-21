package cz.cvut.kbss.amaplas.persistence.dao.mapper;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Bindings {


    public static final SimpleValueFactory factory = SimpleValueFactory.getInstance();

    protected Map<String, Value> bindings = new HashMap<>();
    public static Bindings newBindings(){
        return new Bindings();
    }

    public Bindings addIRI(String name, String iri){
        bindings.put(name, factory.createIRI(iri));
        return this;
    }

    public Bindings add(String name, URI uri){
        bindings.put(name, factory.createIRI(uri.toString()));
        return this;
    }

    public Bindings add(String name, String str){
        bindings.put(name, factory.createLiteral(str));
        return this;
    }
    public Bindings add(String name, String str, String lang){
        bindings.put(name, factory.createLiteral(str, lang));
        return this;
    }

    public Bindings add(String name, Integer num){
        bindings.put(name, factory.createLiteral(num));
        return this;
    }

    public Bindings add(String name, double d){
        bindings.put(name, factory.createLiteral(d));
        return this;
    }
    public Bindings add(String name, float f){
        bindings.put(name, factory.createLiteral(f));
        return this;
    }

    public Bindings add(String name, boolean bool){
        bindings.put(name, factory.createLiteral(bool));
        return this;
    }

    public Map<String, Value> getBindings(){
        return bindings;
    }

    public Stream<Map.Entry<String, Value>> stream(){
        return bindings.entrySet().stream();
    }
}
