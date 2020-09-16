package cz.cvut.kbss.amaplas.exp.graphml;

import cz.cvut.kbss.amaplas.exp.common.ResourceUtils;
import cz.cvut.kbss.amaplas.exp.common.SPINInferenceUtils;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.rules.Rule;
import org.topbraid.shacl.rules.RuleEngine;

import java.util.Arrays;

public class GraphToSchema {

    public static final String CLASSES_FROM_GRAPH_QP = "/queries/schema/classes-from-graph.sparql";
    public static final String CLASS_SUBSUMPTION_HIERARCHY_FROM_GRAPH_QP = "/queries/schema/class-subsumption-hierarchy-from-graph.sparql";
    public static final String RELATIONS_FROM_GRAPH_QP = "/queries/schema/relations-from-graph.sparql";
    public static final String PART_OF_FROM_GRAPH_GRAPH_QP = "/queries/schema/part-of-from-graph.sparql";
    public static final String CLASS_REFERENCES_GRAPH_QP = "/queries/schema/class-references.sparql";
    public static final String CLASS_PROPERTIES_FROM_GRAPH_QP = "/queries/schema/class-properties-from-graph.sparql";
    public static final String INFER_STEREOTYPE_FROM_SUBCLASSOF = "/queries/schema/infer-stereotypes-from-subclassof.sparql";
    public static final String INFER_STEREOTYPE_FROM_HASPART = "/queries/schema/infer-stereotypes-from-haspart.sparql";


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

        // construct schema based on graph
        for( String q : Arrays.asList(
                CLASSES_FROM_GRAPH_QP,
                CLASS_SUBSUMPTION_HIERARCHY_FROM_GRAPH_QP,
                RELATIONS_FROM_GRAPH_QP,
                PART_OF_FROM_GRAPH_GRAPH_QP,
                CLASS_PROPERTIES_FROM_GRAPH_QP
                )) {
            Model res = construct(graph, q);
            target.add(res);
        }

        // propagate stereotypes along subClassOf and part-of relations
        Model newTriples = SPINInferenceUtils.infer(
                Arrays.asList(INFER_STEREOTYPE_FROM_SUBCLASSOF, INFER_STEREOTYPE_FROM_HASPART),
                target
        );

        target.add(newTriples);
//        Model lastModel = null;
//        while(true){
//            Model res = construct(graph, INFER_STEREOTYPE_FROM_SUBCLASSOF);
//            if(res.isIsomorphicWith(lastModel))
//                break;
//            target.add(res);
//        }

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
