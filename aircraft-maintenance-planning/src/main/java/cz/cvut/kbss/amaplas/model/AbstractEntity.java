package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.*;

import java.io.Serializable;
import java.net.URI;
import java.util.Set;

@MappedSuperclass
public class AbstractEntity<ID> implements Serializable {

    // contains all types of the
    @Types
    protected Set<URI> types;

    // property that specifies what is the main type of the entity. It also corresponds to a specific class from the object model

    @OWLDataProperty(iri = Vocabulary.s_p_application_type)
    protected String applicationType = this.getClass().getSimpleName();

    @Id(generated = true)
    protected URI entityURI;

    @OWLDataProperty(iri = Vocabulary.s_p_id)
    private Object id;

    @OWLDataProperty(iri = Vocabulary.s_p_label)
    protected String title;

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

    public ID getId() {
        return (ID)id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int hashCode() {
        if(getEntityURI() == null)
            return super.hashCode();
        return getEntityURI().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if(obj == null)
            return false;

        if(!(obj instanceof AbstractEntity))
            return false;

        if(getEntityURI() == null)
            return super.equals(obj);

        return getEntityURI().equals(((AbstractEntity)obj).getEntityURI());
    }

    @Override
    public String toString() {

        return applicationType + "{" +
                "types=" + types +
                ", entityURI=" + entityURI +
                ", id=" + id +
                ", title='" + title + '\'' +
                '}';
    }
}
