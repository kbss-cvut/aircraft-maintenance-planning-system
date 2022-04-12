package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import cz.cvut.kbss.amplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;

//public abstract class EventType {
@OWLClass(iri = Vocabulary.s_c_event_type)
public abstract class EventType<ID> extends AbstractEntity<ID>{

}
