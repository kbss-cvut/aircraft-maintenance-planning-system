package cz.cvut.kbss.amaplas.model.builders;

import cz.cvut.kbss.amaplas.model.Workpackage;

public class PlanBuilderInput {

    protected Workpackage workpackage;

    public PlanBuilderInput() {
    }

    public PlanBuilderInput(Workpackage workpackage) {
        this.workpackage = workpackage;
    }

    public Workpackage getWorkpackage() {
        return workpackage;
    }

    public void setWorkpackage(Workpackage workpackage) {
        this.workpackage = workpackage;
    }
}
