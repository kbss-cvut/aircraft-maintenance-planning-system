package cz.cvut.kbss.amaplas.controller.dto;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.AbstractPlan;

import java.net.URI;

public class RelationDTO {

    public static RelationDTO asDTO(AbstractPlan planWhole, AbstractPlan planPart){
        return new RelationDTO(planWhole.getEntityURI(), planPart.getEntityURI());
    }

    private URI left;
    private URI right;

    public RelationDTO() {
    }

    public RelationDTO(URI left, URI right) {
        this.left = left;
        this.right = right;
    }

    public URI getLeft() {
        return left;
    }

    public void setLeft(URI left) {
        this.left = left;
    }

    public URI getRight() {
        return right;
    }

    public void setRight(URI right) {
        this.right = right;
    }
}
