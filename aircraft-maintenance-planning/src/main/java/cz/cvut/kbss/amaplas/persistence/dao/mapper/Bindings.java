package cz.cvut.kbss.amaplas.persistence.dao.mapper;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.*;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

    public String toQueryTerm(Value v ){
        if(v == null)
            return null;

        if(v.isIRI()){
            return toQueryTerm((IRI)v);
        }
        if(v.isLiteral()){
            if(v instanceof NumericLiteral){
                return toQueryTerm((NumericLiteral)v);
            }
            if(v instanceof DecimalLiteral){
                return toQueryTerm((DecimalLiteral)v);
            }
            if(v instanceof IntegerLiteral){
                return toQueryTerm((IntegerLiteral)v);
            }
            if(v instanceof BooleanLiteral){
                return toQueryTerm((BooleanLiteral)v);
            }
            return toQueryTerm((Literal) v);
        }
        if(v.isBNode()){
            return String.format("_:%s",((BNode)v).getID());
        }

        throw new UnsupportedOperationException(String.format(
                "Cannot convert value \"%s\" of class %s to query term.",
                v.stringValue(),
                v.getClass().getCanonicalName())
        );
    }

    public String toQueryTerm(IRI iri){
        return String.format("<%s>", iri.stringValue());
    }
    public String toQueryTerm(Literal lit){
        return String.format("%s", lit.stringValue());
    }
    public String toQueryTerm(DecimalLiteral lit){
        return String.format("%s", lit.stringValue());
    }
    public String toQueryTerm(BooleanLiteral lit){
        return String.format("%s", lit.stringValue());
    }
    public String toQueryTerm(IntegerLiteral lit){
        return String.format("%s", lit.stringValue());
    }

    public static String constructValuesClause(Collection<Bindings> values){
        List<String> varNames = values.stream().flatMap(b -> b.stream().map(e -> e.getKey())).distinct().sorted().collect(Collectors.toList());
        String valuesVars = varNames.stream().map(s -> "?" + s).collect(Collectors.joining(" "));

        String rows = values.stream().map(b -> varNames.stream()
                        .map(n -> b.toQueryTerm(b.getBindings().get(n)))
                        .collect(Collectors.joining(" "))
                ).map(r -> String.format("(%s)", r))
                .collect(Collectors.joining("\n"));

        return String.format("VALUES (%s)\n{%s}", valuesVars, rows);
    }

    public static void main(String[] args) {
        List<Bindings> values = IntStream.range(0,10)
                .mapToObj(i -> Bindings.newBindings().add("taskType", URI.create("http://" + i)))
                .collect(Collectors.toList());

        String valuesClause = Bindings.constructValuesClause(values);
        System.out.println(valuesClause);
    }
}
