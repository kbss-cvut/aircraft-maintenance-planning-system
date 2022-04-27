package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import java.util.Objects;

public class TaskScopeType extends EventType<String> {
    public TaskType taskType;
    public String scope;
    public String typeLabel;

    public TaskScopeType(TaskType taskType, String scope) {
        this.taskType = taskType;
        this.scope = scope;
        this.setId(taskType.getCode() + "-" + scope);
        this.typeLabel = taskType.typeLabel() + "-" + scope;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }


//    @Override
    public String typeLabel() {
        return typeLabel;
    }

//    @Override

    public String getCode() {
        return getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskScopeType)) return false;
        TaskScopeType that = (TaskScopeType) o;
        return taskType.equals(that.taskType) && scope.equals(that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskType, scope);
    }


    @Override
    public String toString() {
        return typeLabel;
    }
}
