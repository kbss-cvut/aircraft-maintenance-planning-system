package cz.cvut.kbss.amaplas.exp.graphml;

import cz.cvut.kbss.amaplas.exp.common.ResourceUtils;
import cz.cvut.kbss.amaplas.exp.dataanalysis.model.Task;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GraphToSchema {

    public static final String CLASSES_FROM_GRAPH_QP = "/queries/schema/classes-from-graph.sparql";
    public static final String CLASS_SUBSUMPTION_HIERARCHY_FROM_GRAPH_QP = "/queries/schema/class-subsumption-hierarchy-from-graph.sparql";
    public static final String RELATIONS_FROM_GRAPH_QP = "/queries/schema/relations-from-graph.sparql";
    public static final String CLASS_REFERENCES_GRAPH_QP = "/queries/schema/class-references.sparql";


    public Model constructSchemaFromGraph(Model graph){
        Model ret = constructSchemaFromGraph(graph, null);
        replaceReferences(graph, ret);
        return ret;
    }


    public Model constructSchemaFromGraph(Model graph, Model target){
        if(target == null) {
            target = ModelFactory.createDefaultModel();
            target.setNsPrefixes(graph.getNsPrefixMap());
            target.setNsPrefix("ufo", "http://onto.fel.cvut.cz/ontologies/ufo/");
        }
        for( String q : Arrays.asList(
                CLASSES_FROM_GRAPH_QP,
                CLASS_SUBSUMPTION_HIERARCHY_FROM_GRAPH_QP,
                RELATIONS_FROM_GRAPH_QP
                )) {
            Model res = construct(graph, q);
            target.add(res);
        }
        return target;
    }

    public void replaceReferences(Model graph, Model m){
        ResultSet rs = getSelect(graph, CLASS_REFERENCES_GRAPH_QP);
        replaceReferences(m, rs);
    }

    protected void replaceReferences(Model m, ResultSet rs){
        rs.forEachRemaining(b -> {
            Resource l1 = b.getResource("el1");
            Resource l2 = b.getResource("el2");
            l2 = m.getResource(l2.getURI());
            org.apache.jena.util.ResourceUtils.renameResource(l2, l1.getURI());
        });
    }

    public Model construct(Model graph, String queryName) {
        String query = ResourceUtils.loadResource(queryName);
        return QueryExecutionFactory.create(query, graph).execConstruct();
    }

    public ResultSet getSelect(Model m, String queryName) {
        String query = ResourceUtils.loadResource(queryName);
        return QueryExecutionFactory.create(query, m).execSelect();
    }

}
