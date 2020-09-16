package cz.cvut.kbss.amaplas.exp.common;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.SPINARQFunction;
import org.topbraid.spin.inference.SPINConstructors;
import org.topbraid.spin.inference.SPINInferences;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.util.SPINUtil;
import org.topbraid.spin.vocabulary.SPIN;

import java.util.Collection;

public class SPINInferenceUtils {

    public static Model infer(Collection<String> queryNames, Model target){
        Model queries = ModelFactory.createDefaultModel();
        Model newTriples = ModelFactory.createDefaultModel();

        for(String name : queryNames){
            String queryStr = ResourceUtils.loadResource(name);
            if(queryStr == null){
                throw new RuntimeException(String.format("Cannot load query resource \"%s\"", name));
            }
            Query spinQuery = ARQ2SPIN.parseQuery(queryStr, queries);
            queries.add(RDFS.Resource, SPIN.rule, spinQuery);
        }

        MultiUnion unionGraph = new MultiUnion(new Graph[]{target.getGraph(), queries.getGraph(), newTriples.getGraph()});
        Model unionModel = ModelFactory.createModelForGraph(unionGraph);
        int interations = SPINInferences.run(unionModel, newTriples, null, null, false, null);
        return newTriples;
    }

//    public void experiment(){
////        ShapesGraph sg = new ShapesGraph()
//////        RuleEngine
////        org.topbraid.spin.inference.SPINInferences si;
////        si.
//////        org.topbraid.spin.util.JenaUtil.spr.inference.
//////                spr.SPRResultSets system.SPINModuleRegistry
//
//        Model d = ModelFactory.createDefaultModel();
//        Resource cls = d.createResource("cls");
//        Property p = d.createProperty("http://p");
//        Resource a = d.createResource("a").addProperty(RDF.type, cls);
//        Resource b = d.createResource("b").addProperty(RDF.type, cls);
//        Resource c = d.createResource("c").addProperty(RDF.type, cls);
//        d.add(a , p, b);
//        d.add(b , p, c);
//
//        Model q = ModelFactory.createDefaultModel();
//        Query spinQuery = ARQ2SPIN.parseQuery("CONSTRUCT {?s1 <http://p> ?s3} WHERE {?s1 <http://p> ?s2. ?s2 <http://p> ?s3.}", q);
//        q.add(RDFS.Resource, SPIN.rule, spinQuery);
//        Model im = ModelFactory.createDefaultModel();
//        MultiUnion unionGraph = new MultiUnion(new Graph[] {
//                d.getGraph(),
//                q.getGraph(),
//                im.getGraph()
//
//        });
//        Model qm = ModelFactory.createModelForGraph(unionGraph);
//
//        q.write(System.out, "ttl");
//        System.out.println("--------------------------------------------\n\n\n");
//        d.write(System.out, "ttl");
//        System.out.println("--------------------------------------------\n\n\n");
//
//
////        MultiUnion unionGraph = new MultiUnion(new Graph[] {
////                q.getGraph(),
////                d.getGraph()
////        });
////        Model qm = ModelFactory.createModelForGraph(unionGraph);
//
//
//
////        SPINConstructors.constructAll(q, d, null);
//        int interations = SPINInferences.run(qm, im, null, null, false, null);
//        System.out.println(interations);
//        System.out.println(im.size());
//        im.write(System.out, "ttl");
//    }
//
//    public static void main(String[] args) {
//        new SPINInferenceUtils().experiment();
//    }
}
