package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;

import java.util.*;

@OWLClass(iri = Vocabulary.s_c_task_plan)
public class TaskPlan extends AbstractComplexPlan<SessionPlan>{

    @OWLObjectProperty(iri = Vocabulary.s_p_has_task_type)// has task type
    private TaskType taskType;

    @OWLDataProperty(iri = Vocabulary.s_p_time_estimate_in_hours)
    private Double estMin;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_work_order_step)
    private Set<TaskStepPlan> taskStepPlans = new HashSet<>();

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

    public Set<TaskStepPlan> getTaskStepPlans() {
        return taskStepPlans;
    }

    public void setTaskStepPlans(Set<TaskStepPlan> taskStepPlans) {
        this.taskStepPlans = taskStepPlans;
    }


    public void addStep(TaskStepPlan taskStepPlan){
        if(taskStepPlan == null)
            return;
        if(taskStepPlans == null)
            taskStepPlans = new HashSet<>();
        taskStepPlans.add(taskStepPlan);
    }

    public boolean removeStep(TaskStepPlan taskStepPlan){
        if(taskStepPlan == null || taskStepPlans == null || taskStepPlans.isEmpty())
            return false;

        return taskStepPlans.remove(taskStepPlan);
    }
}
