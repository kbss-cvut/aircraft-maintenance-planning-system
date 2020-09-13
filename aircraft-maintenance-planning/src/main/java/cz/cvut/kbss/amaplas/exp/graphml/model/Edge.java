package cz.cvut.kbss.amaplas.exp.graphml.model;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Edge {
    @XmlAttribute
    public String id;
    @XmlAttribute
    public String source;
    @XmlAttribute
    public String target;

    @XmlPath("data/y:PolyLineEdge/y:EdgeLabel/y:tmptxt/text()")
    public String label;

    @XmlPath("data/y:PolyLineEdge/y:EdgeLabel/@textColor")
    public String labelColor;

    @XmlPath("data/y:PolyLineEdge/y:LineStyle/@type")
    public String type;

    @XmlPath("data/y:PolyLineEdge/y:LineStyle/@width")
    public String width;

    @XmlPath("data/y:PolyLineEdge/y:LineStyle/@color")
    public String color;

    @XmlPath("data/y:PolyLineEdge/y:Arrows/@source")
    public String sourceArrow;

    @XmlPath("data/y:PolyLineEdge/y:Arrows/@target")
    public String targetArrow;

}
