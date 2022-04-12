package cz.cvut.kbss.amaplas.exp.graphml.model;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Edge implements IGraphElement {
    @XmlAttribute
    public String id;
    @XmlAttribute
    public String source;
    @XmlAttribute
    public String target;

    @XmlPath("data/y:PolyLineEdge/y:EdgeLabel/y:tmptxt/text()")
    public String text;

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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    public String getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(String labelColor) {
        this.labelColor = labelColor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSourceArrow() {
        return sourceArrow;
    }

    public void setSourceArrow(String sourceArrow) {
        this.sourceArrow = sourceArrow;
    }

    public String getTargetArrow() {
        return targetArrow;
    }

    public void setTargetArrow(String targetArrow) {
        this.targetArrow = targetArrow;
    }
}
