package cz.cvut.kbss.amaplas.exp.graphml.model;


import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "graphml", namespace = "http://graphml.graphdrawing.org/xmlns")
@XmlType(name = "graphml", namespace = "http://graphml.graphdrawing.org/xmlns")
@XmlAccessorType(XmlAccessType.FIELD)
public class Graph {

    @XmlPath("graph/node")
    public List<Node> nodes;
    @XmlPath("graph/edge")
    public List<Edge> edges;
}
