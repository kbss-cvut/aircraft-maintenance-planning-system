package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;

import java.net.URI;

@OWLClass(iri = Vocabulary.s_c_aircraft)
public class Aircraft extends Resource{
    @OWLDataProperty(iri = Vocabulary.s_p_model)
    protected String model;

    public Aircraft() {
    }

    public Aircraft(URI entityURI, String model) {
        this.entityURI = entityURI;
        this.model = model;
    }

    @Override
    public String toString() {
        return "Aircraft{" +
                "model='" + model + '\'' +
                ", types=" + types +
                ", applicationType='" + applicationType + '\'' +
                ", entityURI=" + entityURI +
                ", title='" + title + '\'' +
                '}';
    }
}
