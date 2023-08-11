package cz.cvut.kbss.amaplas.model.scheduler;

import cz.cvut.kbss.amaplas.model.RevisionPlan;
import cz.cvut.kbss.amaplas.model.Workpackage;

public interface PlanScheduler {
    void schedule(RevisionPlan revisionPlan, Workpackage wp);
}
