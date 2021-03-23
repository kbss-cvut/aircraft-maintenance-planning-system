package cz.cvut.kbss.amaplas.exp.graphml;

import cz.cvut.kbss.amaplas.exp.common.JAXBUtils;
import cz.cvut.kbss.amaplas.exp.graphml.model.Edge;
import cz.cvut.kbss.amaplas.exp.graphml.model.Graph;
import cz.cvut.kbss.amaplas.exp.graphml.model.IGraphElement;
import cz.cvut.kbss.amaplas.exp.graphml.model.Node;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String nsp = ns;
        if(!nsp.endsWith("/") && !nsp.endsWith("#"))
            nsp += "/";
        m.setNsPrefix(prefix, nsp);
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
        if(n.text != null){// && n.label.text != null) {
            node.addLiteral(Vocabulary.p_dp_text, n.text);
            addStereotypes(n, node);
        }
        if(n.shapeType != null)
            node.addLiteral(Vocabulary.p_dp_shapeType, n.shapeType);
    }

//    public void addStereotypeAndProperties(Node n, Resource node){
//        Pattern stereotypePattern = Pattern.compile("<<([^>]+)>>");
//        Matcher m = stereotypePattern.matcher(n.text);
//    }

    public void addStereotypes(Node n, Resource node){
        Pattern stereotypePattern = Pattern.compile("<<([^>]+)>>");
        Matcher m = stereotypePattern.matcher(n.text);
//        String rest = n.text;
//        List<MatchResult> results = new ArrayList<>();
        while(m.find()){
            String s = m.group(1);
            s = s.trim().toLowerCase().replaceAll("\\s+", "-");
            node.addProperty(Vocabulary.p_dp_stereotype, s);
//            m.repl
//            results.add(m.toMatchResult());
//            // remove match from rest
//            setCharBetween(rest, m.start(), m.end(), ' ');
        }

        m.reset();
//        results.forEach(m -> StringUtils.);
        String l = m.replaceAll("");
        l = l.trim().replaceAll(" +", " ");
        l = handleNodeProperties(n, l, node);
        l = l.replaceAll("\\s+", " ").trim();

        l = normalizeLabel(l);

        node.addProperty(Vocabulary.p_dp_label, l);
    }

    /**
     * normalize label to space separated terms. processes camelcase, replace "_+" to " " or "-+" to " ".
     * @param l
     * @return
     */
    public String normalizeLabel(String l){
        if(!l.matches(".+\\s.+")){
            String[] terms = StringUtils.splitByCharacterTypeCamelCase(l);
            if(terms.length > 1){
                l = String.join(" ", terms);
            }else if(l.matches(".*_.*")){
                l = l.replaceAll("_+", " ");
            }else{
                l = l.replaceAll("-+", " ");
            }
        }
        return l;
    }
//
//    protected String setCharBetween(String str, int from, int to, char c){
//        StringBuffer sb = new StringBuffer();
//        sb.append(str.substring(0, from));
//        for(int i = from; i < to; i ++){
//            sb.append(c);
//        }
//        if(to < str.length())
//            sb.append(str.substring(to));
//        return sb.toString();
//    }

    public String handleNodeProperties(Node n, String in, Resource node){
        String[] lines = in.split("\n");
        String typeAndStereotype = "";
        Pattern propertyType = Pattern.compile("^-\\s*(.*)\\s*$");
        StringBuffer sb = new StringBuffer();
        for(String line : lines){
            Matcher m = propertyType.matcher(line);
            if(m.matches()){
                String prop = m.group(1);
                Resource p = asres(n.id + "-" + asres(n.id + "-" + prop.replaceAll("\\s+", "-")));
                node.addProperty(Vocabulary.p_dp_property, p);
                node.getModel().add(p, Vocabulary.p_dp_label, prop);
            }else {
                sb.append(line);
                sb.append("\n");
            }
        }
        return sb.toString();
    }


    public String urlEncode(String str){
        try {
            return str == null ? null :
                    URLEncoder.encode(str.replaceAll("\\s", "_"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String localName(IGraphElement el){
        String localName = el.getId();
//        String localName =
//                el.getId() + "-" +
//                Optional.ofNullable(el.getText()).map(this::urlEncode).orElse("");
        return localName;
    }

//    public Resource asres(Node n){
//        return asres(n.id);
//    }

//    public Resource asres(Edge e){
//        return asres(e.id);
//    }

    public Resource asres(IGraphElement el){
        return asres(localName(el));
    }
    public Resource asres(String id){
        return ResourceFactory.createResource(namespace + id);
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
        if (e.text != null){
            edge.addLiteral(Vocabulary.p_dp_label, e.text);
            edge.addLiteral(RDFS.label, e.text);
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
