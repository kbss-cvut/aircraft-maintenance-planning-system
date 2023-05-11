package cz.cvut.kbss.amaplas.model;

public class ScopedTaskExecutionStatistics extends AbstractEntity{

    protected TaskExecutionStatistics parent;

    protected Scope scope;

    public TaskExecutionStatistics getParent() {
        return parent;
    }

    public void setParent(TaskExecutionStatistics parent) {
        this.parent = parent;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}
