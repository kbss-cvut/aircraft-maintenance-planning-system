package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences;

import cz.cvut.kbss.amaplas.exp.common.JGraphTUtils;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.SequencePattern;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.graphml.GraphMLExporter;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public class ToGraphml {
    public static void write(Collection<SequencePattern> patterns, String file){
        DefaultDirectedGraph<String, String> g = toGraph(patterns);
        write(g, file);
    }

    public static DefaultDirectedGraph<String, String> toGraph(Collection<SequencePattern> patterns){
        DefaultDirectedGraph<String, String> g = new DefaultDirectedGraph<>(() -> new String(), () -> new String(), true );

        DefaultEdge de;

        // add nodes and edges
        int edgeId = 0;
        for (SequencePattern pattern : patterns){
            TaskType s = pattern.pattern.get(0);
            TaskType t = pattern.pattern.get(1);
            g.addVertex(s.type);
            g.addVertex(t.type);
            String edge = "" + edgeId++;
            g.addEdge(s.type, t.type, edge);
            g.setEdgeWeight(edge, pattern.instances.size());
        }
        return g;
    }

    public static <V, E> GraphMLExporter<V, E> createExporter(Graph<V, E> g){
        GraphMLExporter<V, E> e = new GraphMLExporter<>();
        e.setExportEdgeLabels(true);
        e.setExportVertexLabels(true);
        e.setExportEdgeWeights(true);
        return e;
    }

    public static Map<String,Attribute> patternAttributes(SequencePattern pattern){
        Map<String, Attribute> attrs = JGraphTUtils.toMap(pattern);
        attrs.remove("supportClass");
        return attrs;
    }

    public static Map<String, Attribute> taskTypeAttributes(TaskType t){
        Map<String, Attribute> attrs = JGraphTUtils.toMap(t);
        return attrs;
    }

    public static void writePatterns(Graph<TaskType, SequencePattern> g, String file){
        GraphMLExporter<TaskType, SequencePattern> exporter = createExporter(g);

        exporter.setVertexAttributeProvider(v -> taskTypeAttributes(v));
        // register node attributes
        JGraphTUtils.attributeDefinitions(TaskType.class).entrySet().forEach(e ->{
                    exporter.registerAttribute(e.getKey(), GraphMLExporter.AttributeCategory.NODE, e.getValue());
                }
        );

        exporter.setEdgeAttributeProvider(e -> patternAttributes(e));
        // register edge attributes
        JGraphTUtils.attributeDefinitions(SequencePattern.class).entrySet().stream()
//                .filter(e -> !e.getKey().equals("supportClass"))
                .forEach(e ->{
                    exporter.registerAttribute(e.getKey(), GraphMLExporter.AttributeCategory.EDGE, e.getValue());
                }
        );
        exporter.exportGraph(g, new File(file));
    }

    public static <V, E> void write(Graph<V, E> g, String file,
                                    Function<V, Map<String, Attribute>> vertexAttributeProvider,
                                    Function<E, Map<String, Attribute>> edgeAttributeProvider){
        GraphMLExporter<V, E> e = createExporter(g);
        e.setVertexAttributeProvider(vertexAttributeProvider);
        e.setEdgeAttributeProvider(edgeAttributeProvider);
        e.exportGraph(g, new File(file));
    }


    public static <V, E> void write(Graph<V, E> g, String file){
        GraphMLExporter<V, E> e = createExporter(g);
        e.exportGraph(g, new File(file));
    }

}
