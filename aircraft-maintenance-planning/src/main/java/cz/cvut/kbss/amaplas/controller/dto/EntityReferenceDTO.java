package cz.cvut.kbss.amaplas.controller.dto;

import java.net.URI;

public class EntityReferenceDTO {
    private URI entityURI;

    public EntityReferenceDTO() {
    }

    public EntityReferenceDTO(URI entityURI) {
        this.entityURI = entityURI;
    }

    public URI getEntityURI() {
        return entityURI;
    }

    public void setEntityURI(URI entityURI) {
        this.entityURI = entityURI;
    }
}
