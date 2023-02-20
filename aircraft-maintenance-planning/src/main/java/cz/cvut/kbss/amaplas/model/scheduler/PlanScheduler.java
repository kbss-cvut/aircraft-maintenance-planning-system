package cz.cvut.kbss.amaplas.model.scheduler;

import cz.cvut.kbss.amaplas.model.RevisionPlan;

public interface PlanScheduler {
    void schedule(RevisionPlan revisionPlan);
}
