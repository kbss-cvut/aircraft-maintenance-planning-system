package cz.cvut.kbss.amaplas.exp.graphml.model;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.*;

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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    public String getFill() {
        return fill;
    }

    public void setFill(String fill) {
        this.fill = fill;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getShapeType() {
        return shapeType;
    }

    public void setShapeType(String shapeType) {
        this.shapeType = shapeType;
    }
}
