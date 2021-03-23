package cz.cvut.kbss.amaplas.exp.graphml.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class Node implements IGraphElement {

    @XmlAttribute(name = "id")
    public String id;


    @XmlPath("data/y:ShapeNode/y:NodeLabel/y:tmptxt/text()")
    public String text;
    @XmlPath("data/y:ShapeNode/y:Fill/@color")
    public String fill;
    @XmlPath("data/y:ShapeNode/y:Geometry/@width")
    public String width;
    @XmlPath("data/y:ShapeNode/y:Geometry/@height")
    public String height;
    @XmlPath("data/y:ShapeNode/y:Shape/@type")
    public String shapeType;
}
