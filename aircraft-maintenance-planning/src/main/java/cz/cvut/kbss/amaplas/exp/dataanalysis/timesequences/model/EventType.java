package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import cz.cvut.kbss.jopa.model.annotations.MappedSuperclass;

//public abstract class EventType {
@MappedSuperclass//(iri = Vocabulary.s_c_event_type)
public abstract class EventType<ID> extends AbstractEntity<ID>{

}
