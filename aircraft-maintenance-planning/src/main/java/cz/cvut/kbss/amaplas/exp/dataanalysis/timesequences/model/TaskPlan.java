package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class TaskPlan extends AbstractComplexPlan<SessionPlan>{
    public static long counter = 0;

//    private long id;
    @JsonProperty("task-type")
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
}
