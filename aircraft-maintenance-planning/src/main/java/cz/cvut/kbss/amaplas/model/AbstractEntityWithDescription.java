package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.*;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import java.util.Set;

@MappedSuperclass
public class AbstractEntityWithDescription extends AbstractEntity {

    // property that specifies what is the main type of the entity. It also corresponds to a specific class from the object model

    @OWLDataProperty(iri = Vocabulary.s_p_id)
    protected String id;

    @OWLDataProperty(iri = Vocabulary.s_p_label)
    protected String title;

    @OWLDataProperty(iri = Vocabulary.s_p_description)
    protected String description;
//    // TODO - check if deleting/setting-lazy will make reading objects faster
//    @Properties(fetchType = FetchType.LAZY)
//    protected Map<URI, Set<Object>> properties;

//    public Map<URI, Set<Object>> getProperties() {
//        return properties;
//    }
//
//    public void setProperties(Map<URI, Set<Object>> properties) {
//        this.properties = properties;
//    }


    public String getId(){
        return id;
    }

    /**
     * Utility method to convert id to string when it is passed as numeric value.
     * @param id
     */
    public void setId(Object id){
        this.id = id == null ? null : id.toString();
    }
    public void setId(String id){
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return applicationType + "{" +
                "types=" + types +
                ", entityURI=" + entityURI +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
//                ", properties=" + properties +
                '}';
    }
}
