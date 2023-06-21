package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.Transient;

import java.util.Date;

@OWLClass(iri = Vocabulary.s_c_event)
public class Event extends AbstractEntity {
    @Transient
    protected String date;
    @Transient
    protected Date start;
    @Transient
    protected Date end;
    @Transient
    protected Long dur;

    @Transient
    protected Long workTime;


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Long getDur() {
        return dur;
    }

    public void setDur(Long dur) {
        this.dur = dur;
    }

    public Long getWorkTime() {
        return workTime;
    }

    public void setWorkTime(Long workTime) {
        this.workTime = workTime;
    }
}
