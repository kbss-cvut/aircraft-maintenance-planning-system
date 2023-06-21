package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.model.base.LongIntervalImpl;
import cz.cvut.kbss.amaplas.model.values.DateParserSerializer;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.Transient;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@OWLClass(iri = Vocabulary.s_c_work_session)
public class WorkSession extends Event {
    /** The id of the work session log */

    @OWLDataProperty(iri = Vocabulary.s_p_id)
    protected String id;

    @OWLDataProperty(iri = Vocabulary.s_p_task_description)
    protected String description;


    @OWLDataProperty(iri = Vocabulary.s_p_has_shift)
    protected String shiftGroup;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_scope)
    protected MaintenanceGroup scope;

    @OWLObjectProperty(iri = Vocabulary.s_p_performed_by)
    protected Mechanic mechanic;

    @OWLObjectProperty(iri = Vocabulary.s_p_is_part_of_maintenance_task)
    protected TaskExecution taskExecution;

    public WorkSession() {
    }

    public WorkSession(WorkSession r) {
        this.scope = r.scope;
        this.date = r.date;
        this.start = r.start;
        this.end = r.end;
        this.dur = r.dur;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Mechanic getMechanic() {
        return mechanic;
    }

    public void setMechanic(Mechanic mechanic) {
        this.mechanic = mechanic;
    }

    public TaskExecution getTaskExecution() {
        return taskExecution;
    }

    public void setTaskExecution(TaskExecution taskExecution) {
        this.taskExecution = taskExecution;
    }

    @Override
    public String toString() {
        return toString(",");
    }

    public String toString(String sep) {
        return Stream.of(entityURI.toString(), description, scope.applicationType,
                        date, start.toString(), end.toString(), dur.toString())
                .collect(Collectors.joining(sep));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkSession)) return false;
        WorkSession workSession = (WorkSession) o;
        return Objects.equals(entityURI, workSession.entityURI) &&
                taskExecution.equals(workSession.taskExecution) &&
                start.equals(workSession.start) &&
                end.equals(workSession.end);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShiftGroup() {
        return shiftGroup;
    }

    public void setShiftGroup(String shiftGroup) {
        this.shiftGroup = shiftGroup;
    }

    public MaintenanceGroup getScope() {
        return scope;
    }

    public void setScope(MaintenanceGroup scope) {
        this.scope = scope;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityURI, taskExecution, start, end);
    }
}
