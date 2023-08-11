package cz.cvut.kbss.amaplas.model.scheduler;

import cz.cvut.kbss.amaplas.model.RevisionPlan;
import cz.cvut.kbss.amaplas.model.SessionPlan;
import cz.cvut.kbss.amaplas.model.Workpackage;

public class NaivePlanScheduler implements PlanScheduler {

    public void schedule(RevisionPlan revisionPlan, Workpackage wp){
        // deduce schedule from revision work sessions
        // 1.a Creat session schedules - from session logs
        revisionPlan.streamPlanParts()
                .filter(p -> p instanceof SessionPlan)
                .forEach(sp -> {

                    if(sp.getStartTime() != null && sp.getEndTime() != null){
                        long duration = sp.getEndTime().getTime() - sp.getStartTime().getTime();
                        sp.setDuration(duration);
                        sp.setWorkTime(duration);
                    }
                });

        // 2. update plan parts bottom up
        revisionPlan.applyOperationBottomUp( p -> p.updateTemporalAttributes());
    }
}
