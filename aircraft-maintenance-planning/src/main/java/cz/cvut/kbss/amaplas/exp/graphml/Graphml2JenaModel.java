package cz.cvut.kbss.amaplas.exp.graphml;

import cz.cvut.kbss.amaplas.exp.common.JAXBUtils;
import cz.cvut.kbss.amaplas.exp.graphml.model.Edge;
import cz.cvut.kbss.amaplas.exp.graphml.model.Graph;
import cz.cvut.kbss.amaplas.exp.graphml.model.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class Graphml2JenaModel {

    protected String namespace;

    public void execute(String file, String out, String ns, String prefix) throws IOException {
        Model m = execute(file, ns, prefix);
        try(Writer w = new FileWriter(out)) {
            m.write(w, "ttl");
        }
    }

    public Model execute(String file, String ns, String prefix) throws IOException {

        this.namespace = ns;
        Graph g = JAXBUtils.loadData(file, Graph.class, null, new FixLabels());
        Model m = toModel(g, null);
//        m.setNsPrefix(, ns);
        m.setNsPrefix(prefix, ns);
        m.setNsPrefix(Vocabulary.P, Vocabulary.NS + "#");

        return m;

    }

    public Model toModel(Graph g, Model m){
        if(m == null) {
            m = ModelFactory.createDefaultModel();
            m.setNsPrefixes(PrefixMapping.Standard);
        }
        Resource graph = m.createResource(namespace)
                .addProperty(RDF.type, Vocabulary.c_graph);

        g.nodes.forEach(n -> addToModel(n, graph));
        Map<String, Resource> reg = new HashMap<>();
        g.nodes.forEach(n -> reg.put(n.id, asres(n)));
        g.edges.forEach(e -> addToModel(e, graph, reg));
        return m;
    }


    public void addToModel(Node n, Resource graph){
        Resource node = asres(n);
        graph.addProperty(Vocabulary.p_op_hasNode, node);
        Model m = graph.getModel();

        node = m.createResource(node.getURI());

        node.addProperty(RDF.type, Vocabulary.r_c_node);
        node.addLiteral(Vocabulary.p_dp_id, n.id);
        if(n.fill != null)
            node.addLiteral(Vocabulary.p_dp_fillColor, n.fill);
        if(n.height != null)
            node.addLiteral(Vocabulary.p_dp_height, n.height);
        if(n.width != null)
            node.addLiteral(Vocabulary.p_dp_width, n.width);
        if(n.label != null){// && n.label.text != null) {
            node.addLiteral(Vocabulary.p_dp_label, n.label);
            node.addLiteral(RDFS.label, n.label);
        }
        if(n.shapeType != null)
            node.addLiteral(Vocabulary.p_dp_shapeType, n.shapeType);
    }

    public Resource asres(Node n){
        return ResourceFactory.createResource(namespace + n.id);
    }

    public Resource asres(Edge e){
        return ResourceFactory.createResource(namespace + e.id);
    }

    public void addToModel(Edge e, Resource graph, Map<String, Resource> reg) {

        Resource edge = asres(e);
        graph.addProperty(Vocabulary.p_op_hasEdge, edge);
        Model m = graph.getModel();

        edge = m.createResource(edge.getURI())
                .addProperty(RDF.type, Vocabulary.r_c_edge)
                .addProperty(Vocabulary.p_op_source, reg.get(e.source))
                .addProperty(Vocabulary.p_op_target, reg.get(e.target))
                .addLiteral(Vocabulary.p_dp_id, e.id);
        if (e.label != null){
            edge.addLiteral(Vocabulary.p_dp_label, e.label);
            edge.addLiteral(RDFS.label, e.label);
        }
        if(e.labelColor != null)
            edge.addLiteral(Vocabulary.p_dp_textColor, e.labelColor);

        if(e.type != null)
            edge.addLiteral(Vocabulary.p_dp_linetype, e.type);

        if(e.color != null)
            edge.addLiteral(Vocabulary.p_dp_lineColor, e.color);
        if(e.width != null)
            edge.addLiteral(Vocabulary.p_dp_linewidth, e.width);
        if(e.sourceArrow != null)
            edge.addLiteral(Vocabulary.p_dp_sourceArrow, e.sourceArrow);
        if(e.targetArrow != null)
            edge.addLiteral(Vocabulary.p_dp_targetArrow, e.targetArrow);
    }



    public static void main(String[] args) throws IOException {

////        GenericXMLWriter
////        GMLWriter w = new GMLWriter(null);
//        TinkerGraph g = TinkerGraphFactory.createTinkerGraph();
//        GraphMLReader.inputGraph(g, "c:\\Users\\kostobog\\Documents\\skola\\projects\\2019-CSAT-doprava-2020\\code\\aircraft-maintenance-planning-system\\aircraft-maintenance-planning-model\\drafts\\maintenance-planning-model-B.graphml");
//        g.getEdges();
////        GMLReader r = G
////        GMLWriter    gmlWriter = new GenericXMLWriter(null);

        String input = "c:\\Users\\kostobog\\Documents\\skola\\projects\\2019-CSAT-doprava-2020\\code\\aircraft-maintenance-planning-system\\aircraft-maintenance-planning-model\\drafts\\maintenance-planning-model-B.graphml";

        String output = input.substring(0, input.lastIndexOf(".")) + ".ttl";
        new Graphml2JenaModel().execute(input, output, CSAT.PLANNING_NS, CSAT.PLANNING_PREF);
    }
}
