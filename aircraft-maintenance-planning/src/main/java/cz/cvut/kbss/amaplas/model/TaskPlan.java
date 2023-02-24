package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;

@OWLClass(iri = Vocabulary.s_c_task_plan)
public class TaskPlan extends AbstractComplexPlan<SessionPlan>{

//    private long id;
//    @JsonProperty("task-type")
    @OWLObjectProperty(iri = Vocabulary.s_p_has_task_type)// has task type
    private TaskType taskType;


    @OWLDataProperty(iri = Vocabulary.s_p_time_estimate_in_hours)
    private Double estMin;


    public TaskPlan(TaskType taskType) {
        this.taskType = taskType;
    }

    public TaskPlan() {
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public Double getEstMin() {
        return estMin;
    }

    public void setEstMin(Double estMin) {
        this.estMin = estMin;
    }
}
