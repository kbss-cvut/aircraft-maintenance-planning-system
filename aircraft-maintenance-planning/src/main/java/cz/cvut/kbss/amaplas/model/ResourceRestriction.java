package cz.cvut.kbss.amaplas.model;


import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;

import java.net.URI;
import java.util.List;

@OWLClass(iri = Vocabulary.s_c_restriction)
public class ResourceRestriction extends AbstractEntity<String>{
    @OWLObjectProperty(iri = Vocabulary.s_p_requirement_on)
    protected Resource resource;
    @OWLObjectProperty(iri = Vocabulary.s_p_has_requirement)
    protected List<URI> requiredResourceState;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public List<URI> getRequiredResourceState() {
        return requiredResourceState;
    }

    public void setRequiredResourceState(List<URI> requiredResourceState) {
        this.requiredResourceState = requiredResourceState;
    }
}
