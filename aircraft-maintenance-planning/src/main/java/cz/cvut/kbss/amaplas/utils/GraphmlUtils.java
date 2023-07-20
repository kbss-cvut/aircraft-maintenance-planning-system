package cz.cvut.kbss.amaplas.utils;

import cz.cvut.kbss.amaplas.model.TaskType;
import cz.cvut.kbss.amaplas.planners.SequencePattern;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.GraphExporter;
import org.jgrapht.nio.gml.GmlExporter;
import org.jgrapht.nio.graphml.GraphMLExporter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GraphmlUtils {
    public static void write(Collection<SequencePattern> patterns, String file){
//        DefaultDirectedGraph<String, String> g = toStringGraph(patterns);
        DefaultDirectedGraph<TaskType, SequencePattern> g = toGraph(patterns);
        write(g, file);
    }

    public static DefaultDirectedGraph<TaskType, SequencePattern> toGraph(Collection<SequencePattern> patterns){
        DefaultDirectedGraph<TaskType, SequencePattern> g = new DefaultDirectedGraph<>(() -> new TaskType(), () -> new SequencePattern(), true );

        DefaultEdge de;

        // add nodes and edges
        int edgeId = 0;
        for (SequencePattern pattern : patterns){
            TaskType s = pattern.pattern.get(0);
            TaskType t = pattern.pattern.get(1);
            g.addVertex(s);
            g.addVertex(t);
//            String edge = "" + edgeId++;
            g.addEdge(s, t, pattern);
            g.setEdgeWeight(pattern, pattern.instances.size());
        }
        return g;
    }

    public static DefaultDirectedGraph<String, String> toStringGraph(Collection<SequencePattern> patterns){
        DefaultDirectedGraph<String, String> g = new DefaultDirectedGraph<>(() -> new String(), () -> new String(), true );

        DefaultEdge de;
        // add nodes and edges
        int edgeId = 0;
        for (SequencePattern pattern : patterns){
            TaskType s = pattern.pattern.get(0);
            TaskType t = pattern.pattern.get(1);
            g.addVertex(s.getCode());
            g.addVertex(t.getCode());
            String edge = "" + edgeId++;
            g.addEdge(s.getCode(), t.getCode(), edge);
            g.setEdgeWeight(edge, pattern.instances.size());
        }
        return g;
    }
//
//    public static DefaultDirectedGraph<Vertex, DefaultEdge> toVertexGraph(Collection<SequencePattern> patterns){
//
//    }

    public static <V, E> GraphMLExporter<V, E> createExporter(Graph<V, E> g){
        GraphMLExporter<V, E> e = new GraphMLExporter<>();
        e.setExportEdgeLabels(true);
        e.setExportVertexLabels(true);
        e.setExportEdgeWeights(true);
        return e;
    }

    public static  GmlExporter<TaskType, SequencePattern> createGMLExporter(Graph<TaskType, SequencePattern> g){
        Function<TaskType, Map<String, Attribute>> vertexAttributeProvider = createDefaultAttributeProvider(TaskType.class, TaskType::getTitle);
        return createGMLExporter(g, vertexAttributeProvider);
    }

    public static <V,E> GmlExporter<V, E> createGMLExporter(Graph<V, E> g, Function<V, Map<String, Attribute>> vertexAttributeProvider){
        GmlExporter<V, E> e = new GmlExporter<>();
        e.setVertexAttributeProvider(vertexAttributeProvider);
        e.setParameter(GmlExporter.Parameter.EXPORT_VERTEX_LABELS,true);
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


    public static <V, E> void write(Graph<V, E> g, GraphExporter<V,E> graphExporter, String file){
        File f = new File(file);
        try {
            System.out.println(f.getCanonicalPath());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        graphExporter.exportGraph(g, new File(file));
    }

    public static <V, E> void write(Graph<V, E> g, Function<V, Map<String,Attribute>> vertexAttributeProvider , String file){
        GraphExporter<V, E> e = createGMLExporter(g, vertexAttributeProvider);
        File f = new File(file);
        try {
            System.out.println(f.getCanonicalPath());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        e.exportGraph(g, new File(file));
    }

    public static void write(Graph<TaskType, SequencePattern> g, String file){
        GraphExporter<TaskType, SequencePattern> e = createGMLExporter(g);
        File f = new File(file);
        try {
            System.out.println(f.getCanonicalPath());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        e.exportGraph(g, new File(file));
    }

    public static <V> Function<V, Map<String,Attribute>> createDefaultAttributeProvider(Class<V> vertexType, Function<V, String> label){
        return tt ->
                new HashMap<>()
                {{
                    this.put("label", new DefaultAttribute<>(label.apply(tt), AttributeType.STRING));
                }};
    }

    static class Vertex{
        public String id;
        public String label;

        public Vertex() {
        }

        public Vertex(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }
}
