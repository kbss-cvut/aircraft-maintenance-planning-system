package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.cvut.kbss.amplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;

import java.util.Date;

@OWLClass(iri = Vocabulary.s_c_task_plan)
public class TaskPlan extends AbstractComplexPlan<SessionPlan>{
    public static long counter = 0;

//    private long id;
//    @JsonProperty("task-type")
    @OWLObjectProperty(iri = Vocabulary.s_p_task_type)
    private TaskType taskType;
//
//    @JsonProperty("start-time")
//    private Date startTime;

//    private double duration;

//    @JsonProperty("work-time")
//    private double workTime;
//
//    @JsonProperty("work-done")
//    private double workDone;

//    public TaskPlan(TaskType taskType, Date startTime, long duration, long workTime, long workDone) {
//        this.id = counter ++;
//        this.taskType = taskType;
//        this.startTime = startTime;
//        this.duration = duration;
//        this.workTime = workTime;
////        this.workDone = workDone;
//    }

    public TaskPlan(TaskType taskType) {
        this.id = counter ++;
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
}
