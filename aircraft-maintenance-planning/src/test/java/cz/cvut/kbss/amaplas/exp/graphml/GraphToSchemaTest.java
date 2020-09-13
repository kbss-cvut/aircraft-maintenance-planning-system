package cz.cvut.kbss.amaplas.exp.graphml;


import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

public class GraphToSchemaTest {

    public static final String NS = "http://example.com/";

    @Test
    public void replaceReferencesInResultSetTest(){
        // set up
        Model m = ModelFactory.createDefaultModel();
        Property p = createProperty("p", m);
        createResource("a", m).addProperty(RDF.type, "t1");
        Resource r = createResource("b", m).addProperty(RDF.type, "t2");
        createResource("c", m).addProperty(p, r);

        // expected result
        Model expectedResultModel = ModelFactory.createDefaultModel();
        p = createProperty("p", expectedResultModel);
        r = createResource("a", expectedResultModel)
                .addProperty(RDF.type, "t1")
                .addProperty(RDF.type, "t2");
        createResource("c", expectedResultModel).addProperty(p, r);


        Model workingModel = ModelFactory.createDefaultModel().add(m);
        Query q = QueryFactory.create("PREFIX : <" + NS +"> SELECT ?el1 ?el2 {} VALUES (?el1 ?el2){ (:a :b)}");

//        System.out.println(q.toString());
        ResultSet rs = QueryExecutionFactory.create(q, m).execSelect();
        // apply function
        GraphToSchema sut = new GraphToSchema();
        sut.replaceReferences(workingModel, rs);

        // test result
        Model onlyInOld = m.difference(expectedResultModel);
        Model onlyInNew = expectedResultModel.difference(m);

//        Assert.assertTrue(expectedResultModel.equals(workingModel));
        for(Statement s : onlyInOld.listStatements().toList()){
            Assert.assertFalse(workingModel.contains(s));
        }

        for(Statement s : onlyInNew.listStatements().toList()){
            Assert.assertTrue(workingModel.contains(s));
        }
    }


    protected Resource createResource(String name, Model m){
        return m.createResource(NS + name);
    }

    protected Property createProperty(String name, Model m){
        return m.createProperty(NS + name);
    }
}
