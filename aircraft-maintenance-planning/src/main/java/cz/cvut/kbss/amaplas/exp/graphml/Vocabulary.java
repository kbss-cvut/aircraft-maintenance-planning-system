    package cz.cvut.kbss.amaplas.exp.graphml;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class Vocabulary {

    public final static String	NS = "http://onto.fel.cvut.cz/ontologies/graph";
    public final static String	P = "gr";

    public final static String	dp_sourceArrow = "http://onto.fel.cvut.cz/ontologies/graph#sourceArrow";
    public final static String	dp_targetArrow = "http://onto.fel.cvut.cz/ontologies/graph#targetArrow";
    public final static String	dp_fillColor = "http://onto.fel.cvut.cz/ontologies/graph#fillColor";
    public final static String	dp_height = "http://onto.fel.cvut.cz/ontologies/graph#height";
    public final static String	dp_id = "http://onto.fel.cvut.cz/ontologies/graph#id";
    public final static String	dp_label = "http://onto.fel.cvut.cz/ontologies/graph#label";
    public final static String	dp_linetype = "http://onto.fel.cvut.cz/ontologies/graph#linetype";
    public final static String	dp_linewidth = "http://onto.fel.cvut.cz/ontologies/graph#linewidth";
    public final static String	dp_shapeType = "http://onto.fel.cvut.cz/ontologies/graph#shapeType";
    public final static String	dp_textColor = "http://onto.fel.cvut.cz/ontologies/graph#textColor";
    public final static String	dp_lineColor = "http://onto.fel.cvut.cz/ontologies/graph#lineColor";
    public final static String	dp_width = "http://onto.fel.cvut.cz/ontologies/graph#width";
    public final static String	op_hasEdge = "http://onto.fel.cvut.cz/ontologies/graph#hasEdge";
    public final static String	op_hasNode = "http://onto.fel.cvut.cz/ontologies/graph#hasNode";
    public final static String	op_source = "http://onto.fel.cvut.cz/ontologies/graph#source";
    public final static String	op_target = "http://onto.fel.cvut.cz/ontologies/graph#target";
    public final static String	c_edge = "http://onto.fel.cvut.cz/ontologies/graph#edge";
    public final static String	c_graph = "http://onto.fel.cvut.cz/ontologies/graph#graph";
    public final static String	c_node = "http://onto.fel.cvut.cz/ontologies/graph#node";


    public final static Property p_dp_sourceArrow = ResourceFactory.createProperty(dp_sourceArrow);
    public final static Property p_dp_targetArrow = ResourceFactory.createProperty(dp_targetArrow);
    public final static Property p_dp_fillColor = ResourceFactory.createProperty(dp_fillColor);
    public final static Property p_dp_height = ResourceFactory.createProperty(dp_height);
    public final static Property p_dp_id = ResourceFactory.createProperty(dp_id);
    public final static Property p_dp_label = ResourceFactory.createProperty(dp_label);
    public final static Property p_dp_linetype = ResourceFactory.createProperty(dp_linetype);
    public final static Property p_dp_linewidth = ResourceFactory.createProperty(dp_linewidth);
    public final static Property p_dp_shapeType = ResourceFactory.createProperty(dp_shapeType);
    public final static Property p_dp_textColor = ResourceFactory.createProperty(dp_textColor);
    public final static Property p_dp_lineColor = ResourceFactory.createProperty(dp_lineColor);
    public final static Property p_dp_width = ResourceFactory.createProperty(dp_width);
    public final static Property p_op_hasEdge = ResourceFactory.createProperty(op_hasEdge);
    public final static Property p_op_hasNode = ResourceFactory.createProperty(op_hasNode);
    public final static Property p_op_source = ResourceFactory.createProperty(op_source);
    public final static Property p_op_target = ResourceFactory.createProperty(op_target);
    public final static Resource r_c_edge = ResourceFactory.createResource(c_edge);
    public final static Resource r_c_graph = ResourceFactory.createResource(c_graph);
    public final static Resource r_c_node = ResourceFactory.createResource(c_node);


}
