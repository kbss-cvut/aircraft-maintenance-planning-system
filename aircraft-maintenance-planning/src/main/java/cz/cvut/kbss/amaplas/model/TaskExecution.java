package cz.cvut.kbss.amaplas.model;


import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.Transient;

import java.net.URI;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@OWLClass(iri = Vocabulary.s_c_maintenance_task)
public class TaskExecution extends Event{
    @OWLObjectProperty(iri = Vocabulary.s_p_is_part_of_workpackage)
    protected Workpackage workpackage;

    @Transient
    protected TaskType taskType;

    public TaskExecution() {
    }

    public TaskExecution(URI entityURI) {
        this.entityURI = entityURI;
    }


    @OWLDataProperty(iri = Vocabulary.s_p_time_estimate_in_hours)
    protected Double estMin;

    @OWLDataProperty(iri = Vocabulary.s_p_end_time)
    protected LocalDate endTime;

    @OWLDataProperty(iri = Vocabulary.s_p_issue_time)
    protected LocalDate issueTime;

    @OWLObjectProperty(iri = Vocabulary.s_p_references_task)
    protected Set<TaskExecution> referencedTasks;
    @OWLObjectProperty(iri = Vocabulary.s_p_has_part)
    protected Set<WorkSession> workSessions;


    public Workpackage getWorkpackage() {
        return workpackage;
    }

    public void setWorkpackage(Workpackage workpackage) {
        this.workpackage = workpackage;
    }

    public LocalDate getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDate endTime) {
        this.endTime = endTime;
    }

    public LocalDate getIssueTime() {
        return issueTime;
    }

    public void setIssueTime(LocalDate issueTime) {
        this.issueTime = issueTime;
    }

    public Double getEstMin() {
        return estMin;
    }

    public void setEstMin(Double estMin) {
        this.estMin = estMin;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public Set<WorkSession> getWorkSessions() {
        return workSessions;
    }

    public void setWorkSessions(Set<WorkSession> workSessions) {
        this.workSessions = workSessions;
    }

    public Set<TaskExecution> getReferencedTasks() {
        return referencedTasks;
    }

    public void setReferencedTasks(Set<TaskExecution> referencedTasks) {
        this.referencedTasks = referencedTasks;
    }
}
