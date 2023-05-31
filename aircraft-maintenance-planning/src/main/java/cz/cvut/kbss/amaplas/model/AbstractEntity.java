package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.*;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import java.util.Set;
@MappedSuperclass
public class AbstractEntity implements Serializable {
    @Types
    protected Set<URI> types;
    @OWLDataProperty(iri = Vocabulary.s_p_application_type)
    protected String applicationType = this.getClass().getSimpleName();
    @Id(generated = true)
    protected URI entityURI;
    public Set<URI> getTypes() {
        return types;
    }

    public void setTypes(Set<URI> types) {
        this.types = types;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public URI getEntityURI() {
        return entityURI;
    }

    public void setEntityURI(URI entityURI) {
        this.entityURI = entityURI;
    }

    @Override
    public int hashCode() {
        if (getEntityURI() == null)
            return super.hashCode();
        return getEntityURI().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null)
            return false;

        if (!(obj instanceof AbstractEntityWithDescription))
            return false;

        if (getEntityURI() == null)
            return super.equals(obj);

        return getEntityURI().equals(((AbstractEntityWithDescription) obj).getEntityURI());
    }

    @Override
    public String toString() {
        return "AbstractEntity{" +
                "entityURI=" + entityURI +
                '}';
    }
}
