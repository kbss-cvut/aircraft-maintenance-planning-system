package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@OWLClass(iri = Vocabulary.s_c_workpackage)
public class Workpackage extends AbstractEntityWithDescription {

    @OWLObjectProperty(iri = Vocabulary.s_p_is_repair_of, fetch = FetchType.EAGER)
    protected Aircraft aircraft;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_client, fetch = FetchType.EAGER)
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

    @Transient
    protected Date start;
    @Transient
    protected Date end;

    @Transient
    protected List<TaskExecutionStatistics> taskExecutionStatistics;// TODO remove if not used?

    @OWLObjectProperty(iri = Vocabulary.s_p_has_part)
    protected Set<TaskExecution> taskExecutions;

    public Workpackage() {
    }

    public Workpackage(URI entityUri) {
        this.entityURI = entityUri;
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

    public List<TaskExecutionStatistics> getTaskExecutionStatistics() {
        return taskExecutionStatistics;
    }

    public void setTaskExecutionStatistics(List<TaskExecutionStatistics> taskExecutionStatistics) {
        this.taskExecutionStatistics = taskExecutionStatistics;
    }

    public Set<TaskExecution> getTaskExecutions() {
        return taskExecutions;
    }

    public void setTaskExecutions(Set<TaskExecution> taskExecutions) {
        this.taskExecutions = taskExecutions;
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

    public Set<TaskType> getTaskTypes(){
        return getTaskExecutions().stream().map(e -> e.getTaskType()).collect(Collectors.toSet());
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