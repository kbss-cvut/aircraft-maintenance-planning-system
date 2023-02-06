package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@OWLClass(iri = Vocabulary.s_c_workpackage)
public class Workpackage extends AbstractEntity{

    @OWLObjectProperty(iri = Vocabulary.s_c_aircraft, fetch = FetchType.EAGER)
    protected Aircraft aircraft;

    @OWLObjectProperty(iri = Vocabulary.s_c_client, fetch = FetchType.EAGER)
    protected Client client;
//    OffsetDateTime
    @OWLDataProperty(iri = Vocabulary.s_p_workpackage_start_time)
    protected LocalDate startTime;
    @OWLDataProperty(iri = Vocabulary.s_p_workpackage_end_time)
    protected LocalDate endTime;
    @OWLDataProperty(iri = Vocabulary.s_p_workpackage_scheduled_start_time)
    protected LocalDate plannedStartTime;
    @OWLDataProperty(iri = Vocabulary.s_p_workpackage_scheduled_end_time)
    protected LocalDate plannedEndTime;

    public Workpackage() {
    }

    public Workpackage(String id) {
        setId(id);
    }


    //
    public Aircraft getAircraft() {
        return aircraft;
    }

    public void setAircraft(Aircraft aircraft) {
        this.aircraft = aircraft;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public LocalDate getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDate startTime) {
        this.startTime = startTime;
    }

    public LocalDate getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDate endTime) {
        this.endTime = endTime;
    }

    public LocalDate getPlannedStartTime() {
        return plannedStartTime;
    }

    public void setPlannedStartTime(LocalDate plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
    }

    public LocalDate getPlannedEndTime() {
        return plannedEndTime;
    }

    public void setPlannedEndTime(LocalDate plannedEndTime) {
        this.plannedEndTime = plannedEndTime;
    }

    @Override
    public String toString() {
        return "WorkPackage{" +
                "entityURI=" + entityURI +
                ", id='" + id + '\'' +
                ", aircraft=" + aircraft +
                ", client=" + client +
                ", endTime=" + endTime +
                ", plannedStartTime=" + plannedStartTime +
                ", plannedEndTime=" + plannedEndTime +
                '}';
    }

    public static void main(String[] args) {
        OffsetDateTime d;


    }
}