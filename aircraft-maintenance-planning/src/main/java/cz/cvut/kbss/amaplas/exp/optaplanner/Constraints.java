package cz.cvut.kbss.amaplas.exp.optaplanner;

import cz.cvut.kbss.amaplas.exp.optaplanner.model.MaintenanceTaskAssignment;
import cz.cvut.kbss.amaplas.exp.optaplanner.model.TimeSlot;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;

public class Constraints implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                eachTaskMustBeAssignedTimeSlots(constraintFactory),
                eachTaskShouldBeAssignedCorrectNumberOfTimeSlots(constraintFactory),
                tasksMustNotOverlap(constraintFactory),

        };
    }

    public Constraint eachTaskMustBeAssignedTimeSlots(ConstraintFactory constraintFactory){
        Constraint c = constraintFactory.fromUnfiltered(MaintenanceTaskAssignment.class)
                .filter(a -> a.getTimeSlot() == null)
                .penalize("assignment should be have a specific time slot", HardSoftScore.ofHard(1));
        return c;
    }

    public Constraint eachTaskShouldBeAssignedCorrectNumberOfTimeSlots(ConstraintFactory constraintFactory){
        Constraint cc = constraintFactory.fromUnfiltered(MaintenanceTaskAssignment.class)
                .groupBy(MaintenanceTaskAssignment::getMaintenanceTask, ConstraintCollectors.count())
                .filter((t, c) -> !t.isCorrectlyAssigned(c))
                .penalize("each task should be assigned correct number of timeslots", HardSoftScore.ofHard(1));
        return cc;
    }

    public Constraint tasksMustNotOverlap(ConstraintFactory constraintFactory){
        Constraint c = constraintFactory.fromUnfiltered(MaintenanceTaskAssignment.class)
                .filter(a -> a.getTimeSlot() != null)
                .join(MaintenanceTaskAssignment.class,
                        Joiners.equal(MaintenanceTaskAssignment::getTimeSlot),
                        Joiners.greaterThan(MaintenanceTaskAssignment::getId),
                        Joiners.filtering(( a1, a2) -> a1.getMaintenanceTask() != a2.getMaintenanceTask())
                )
//                .filter((a1,a2) -> {
//                    return true;
//                        }
//                )
                .penalize("slots assigned to tasks should not overlap", HardSoftScore.ofHard(1));
        return c;
    }


}
