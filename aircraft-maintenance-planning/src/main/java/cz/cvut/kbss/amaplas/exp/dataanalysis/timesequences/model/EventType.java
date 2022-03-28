package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

//public abstract class EventType {
public abstract class EventType<ID> extends AbstractEntity<ID>{

    public abstract String typeLabel();
    public abstract String getCode();
}
