package cz.cvut.kbss.amaplas.exp.graphml;

import cz.cvut.kbss.amaplas.exp.common.JGraphTUtils;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class JGraphT2GraphMlExp {


    // does not work
//    @Test
//    public void testGroupNodesAsGraphNestedInNode(){
//        String out = "out.graphml";
//
//        DefaultDirectedGraph<Object, String> g = new DefaultDirectedGraph<>(String.class);
//        Tmp n1 = create();
//        Tmp n2 = create();
//        Tmp n3 = create();
//        DefaultDirectedGraph<Tmp, String> gn1 = new DefaultDirectedGraph<>(String.class);
//        DefaultDirectedGraph<Tmp, String> gn2 = new DefaultDirectedGraph<>(String.class);
//        g.addVertex(gn1);
//        g.addVertex(gn2);
//        gn1.addVertex(n1);
//        gn1.addVertex(n2);
//        gn1.addEdge(n1, n2, "1");
//        gn2.addVertex(n3);
//        g.addVertex(n1);
//        g.addVertex(n2);
//        g.addVertex(n3);
//        g.addEdge(n2, n3, "2");
//
//        GraphMLExporter<Object, String> e = new GraphMLExporter<>();
//        e.setExportEdgeLabels(true);
//        e.setExportVertexLabels(true);
////        e.setVertexIdProvider(t -> if(t instance Tmp) t.id);
//        e.setVertexLabelAttributeName("label");
//        // register attributes
//        e.registerAttribute("scope", GraphMLExporter.AttributeCategory.NODE, AttributeType.STRING);
////        e.registerAttribute("label", GraphMLExporter.AttributeCategory.NODE, AttributeType.STRING);
//        e.registerAttribute("id", GraphMLExporter.AttributeCategory.NODE, AttributeType.STRING);
//        e.setVertexAttributeProvider(JGraphTUtils::toMap);
//        e.exportGraph(g, new File(out));
//    }

//    @Test
    public void experimentJGraphT2Graphml() {
        String out = "out.graphml";

        DefaultDirectedGraph<Tmp, String> g = new DefaultDirectedGraph<>(String.class);
        Tmp n1 = create();
        Tmp n2 = create();
        Tmp n3 = create();
        g.addVertex(n1);
        g.addVertex(n2);
        g.addVertex(n3);
        g.addEdge(n1, n2, "4");
        g.addEdge(n2, n3, "5");
//        org.jgrapht.nio.graphml.GraphMLExporter g;
        GraphMLExporter<Tmp, String> e = new GraphMLExporter<>();
        e.setExportEdgeLabels(true);
        e.setExportVertexLabels(true);
        e.setVertexIdProvider(t -> t.id);
        e.setVertexLabelAttributeName("label");
        // register attributes
        e.registerAttribute("scope", GraphMLExporter.AttributeCategory.NODE, AttributeType.STRING);
//        e.registerAttribute("label", GraphMLExporter.AttributeCategory.NODE, AttributeType.STRING);
        e.registerAttribute("id", GraphMLExporter.AttributeCategory.NODE, AttributeType.STRING);
        e.setVertexAttributeProvider(JGraphTUtils::toMap);
        e.exportGraph(g, new File(out));

    }


    public static class Tmp {
        public String id;
        public String label;
        public String scope;

        public Tmp(String id, String label, String scope) {
            this.id = id;
            this.label = label;
            this.scope = scope;
        }

        public Map<String, Attribute> toMap() {
            Map<String, Attribute> ret = new HashMap<>();
            ret.put("id", new DefaultAttribute<>(id, AttributeType.STRING));
            ret.put("label", new DefaultAttribute<>(label, AttributeType.STRING));
            ret.put("scope", new DefaultAttribute<>(scope, AttributeType.STRING));
            return ret;
        }
    }


    public static int i = 0;

    public Tmp create() {
        i++;
        return new Tmp("" + i, "node " + i, "scope " + i % 2);
    }
}
