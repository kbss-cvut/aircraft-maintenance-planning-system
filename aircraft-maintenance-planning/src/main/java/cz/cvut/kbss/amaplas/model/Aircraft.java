package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;

import java.net.URI;

@OWLClass(iri = Vocabulary.s_c_aircraft)
public class Aircraft extends Resource{

    @OWLDataProperty(iri = Vocabulary.s_p_model)
    protected String model;

    @OWLDataProperty(iri = Vocabulary.s_p_registration)
    protected String registration;

    @OWLDataProperty(iri = Vocabulary.s_p_age_A)
    protected String age;

    public Aircraft() {
    }

    public Aircraft(URI entityURI, String model) {
        this.entityURI = entityURI;
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Aircraft{" +
                ", types=" + types +
                "model='" + model +
                "registration='" + registration +
                "age='" + age +
                ", applicationType='" + applicationType + '\'' +
                ", entityURI=" + entityURI +
                ", title='" + title + '\'' +
                '}';
    }
}
