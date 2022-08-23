package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;

@OWLClass(iri = Vocabulary.s_c_aircraft)
public class Aircraft extends Resource{
    @OWLDataProperty(iri = Vocabulary.s_p_model)
    protected String model;

}
