package cz.cvut.kbss.amaplas.model.builders;

import cz.cvut.kbss.amaplas.model.Workpackage;

public class PlanBuilderInput<T> {
    protected T input;

    protected Workpackage workpackage;

    public PlanBuilderInput() {
    }

    public PlanBuilderInput(T input) {
        this(input, null);
    }

    public PlanBuilderInput(T input, Workpackage workpackage) {
        this.input = input;
        this.workpackage = workpackage;
    }

    public T getInput() {
        return input;
    }

    public void setInput(T input) {
        this.input = input;
    }

    public Workpackage getWorkpackage() {
        return workpackage;
    }

    public void setWorkpackage(Workpackage workpackage) {
        this.workpackage = workpackage;
    }
}
