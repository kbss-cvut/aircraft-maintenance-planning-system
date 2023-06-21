package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.*;

import java.net.URI;

@OWLClass(iri = "http://www.w3.org/2002/07/owl#NamedIndividual")
public class TaskStepPlan extends AbstractEntity {
    @Transient
    protected URI parentTask;

    @OWLDataProperty(iri = Vocabulary.s_p_step_index)
    protected String stepIndex;

    @OWLDataProperty(iri = Vocabulary.s_p_work_order_action_text)
    protected String actionDescription;

    @OWLDataProperty(iri = Vocabulary.s_p_work_order_text)
    protected String description;

    @OWLDataProperty(iri = Vocabulary.s_p_time_estimate_in_hours)
    private Double estMin;

    @Sparql(query = "SELECT ?failureAnnotation {BIND(?this as ?failureAnnotation)}")
    protected FailureAnnotation failureAnnotation;
    public FailureAnnotation getFailureAnnotation() {
        return failureAnnotation;
    }
    public void setFailureAnnotation(FailureAnnotation failureAnnotation) {
        this.failureAnnotation = failureAnnotation;
    }
    public String getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(String stepIndex) {
        this.stepIndex = stepIndex;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    public Double getEstMin() {
        return estMin;
    }

    public void setEstMin(Double estMin) {
        this.estMin = estMin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public URI getParentTask() {
        return parentTask;
    }

    public void setParentTask(URI parentTask) {
        this.parentTask = parentTask;
    }
}
