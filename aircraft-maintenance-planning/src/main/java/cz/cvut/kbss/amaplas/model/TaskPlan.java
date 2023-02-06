package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;

@OWLClass(iri = Vocabulary.s_c_task_plan)
public class TaskPlan extends AbstractComplexPlan<SessionPlan>{
    public static long counter = 0;

//    private long id;
//    @JsonProperty("task-type")
    @OWLObjectProperty(iri = Vocabulary.s_p_has_task_type)
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
        this.setId(counter ++);
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
