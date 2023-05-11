package cz.cvut.kbss.amaplas.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TaskExecutionStatistics extends AbstractEntity{

    protected URI workpackageURI;
    protected TaskType taskType;

    protected Date issueTime;
    protected Date endTime;
    protected Date start;
    protected Date end;
    protected Long duration;
    protected Long workTime;

    protected List<ScopedTaskExecutionStatistics> scopedTaskExecutionStatistics = new ArrayList<>();

    public TaskExecutionStatistics() {
    }

    public Date getIssueTime() {
        return issueTime;
    }

    public void setIssueTime(Date issueTime) {
        this.issueTime = issueTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
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

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getWorkTime() {
        return workTime;
    }

    public void setWorkTime(Long workTime) {
        this.workTime = workTime;
    }

    public List<ScopedTaskExecutionStatistics> getScopedTaskExecutionStatistics() {
        return scopedTaskExecutionStatistics;
    }

    public void setScopedTaskExecutionStatistics(List<ScopedTaskExecutionStatistics> scopedTaskExecutionStatistics) {
        this.scopedTaskExecutionStatistics = scopedTaskExecutionStatistics;
    }

    public URI getWorkpackageURI() {
        return workpackageURI;
    }

    public void setWorkpackageURI(URI workpackageURI) {
        this.workpackageURI = workpackageURI;
    }
}
