package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class TaskPlan {
    public static long counter = 0;

    public long id;
    @JsonProperty("task-type")
    public TaskType taskType;

    @JsonProperty("start-time")
    public Date startTime;

    public double duration;

    @JsonProperty("work-time")
    public double workTime;

    @JsonProperty("work-done")
    public double workDone;

    public TaskPlan(TaskType taskType, Date startTime, double duration, double workTime, double workDone) {
        this.id = counter ++;
        this.taskType = taskType;
        this.startTime = startTime;
        this.duration = duration;
        this.workTime = workTime;
        this.workDone = workDone;
    }

    public TaskPlan(TaskType taskType) {
        this.id = counter ++;
        this.taskType = taskType;
    }

    public TaskPlan() {
    }
}
