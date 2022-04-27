 package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.base.LongInterval;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.base.LongIntervalImpl;
import cz.cvut.kbss.amplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.MappedSuperclass;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;


import javax.servlet.Filter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


 @MappedSuperclass
public abstract class AbstractComplexPlan<T extends AbstractPlan> extends AbstractPlan {

    @OWLObjectProperty(iri = Vocabulary.s_p_has_part)
    private Set<AbstractPlan> planParts = new HashSet<>();

    public Set<T> getPlanParts() {
        return (Set<T>)planParts;
    }

    public void setPlanParts(Set<T> planParts) {
        this.planParts = (Set<AbstractPlan>)planParts;
    }

    // calculate attributes from plan parts
    public void updateTemporalAttributes() {
        updateTemporalAttributes(null, null);
    }
    public void updateTemporalAttributes(Date fromDate, Date toDate) {
        // update task

//        planParts.stream()
////                .filter(p -> p instanceof AbstractComplexPlan)
//                .forEach(p -> p.updateTemporalAttributes(toDate));


        updatePlannedTemporalAttributesFromPlanParts();
        updateActualTemporalAttributesFromPlanParts(fromDate, toDate);
    }

    public void updatePlannedTemporalAttributesFromPlanParts(){
        // calculate planned start, end time and duration based on plan parts
        updatePlannedTimeIntervalFromPlanParts();
        // calculate planned work time based on children based on plan parts
        updatePlannedWorkTimeFromPlanParts();
    }

    public void updateActualTemporalAttributesFromPlanParts(Date fromDate, Date toDate){
        // calculate start time, end time and duration based on plan parts
        updateActualTimeIntervalFromPlanParts(fromDate, toDate);
        // calculate work time based on plan parts
        updateActualWorkTimeFromPlanParts();
    }

    public void updatePlannedTimeIntervalFromPlanParts() {
        // start time is the min of all start times
        List<LongInterval> intervals = planParts.stream()
                .map(p -> p.convertPlannedTimeInterval(null, null))
                .filter(DEFINED_INTERVAL)
                .collect(Collectors.toList());
        LongInterval unionInterval = LongIntervalImpl.union(intervals);

        if(unionInterval != null) {
            setPlannedStartTime(new Date(unionInterval.getStart()));
            setPlannedEndTime(new Date(unionInterval.getEnd()));
            setPlannedDuration(unionInterval.getLength());
        }
    }

    public void updatePlannedWorkTimeFromPlanParts(){
        long plannedWorkTime = planParts.stream().mapToLong(AbstractPlan::getPlannedWorkTime).sum();
        setPlannedWorkTime(plannedWorkTime);
    }

    /**
     * Calculating the actual time spent on a plan requires a toDate for plans which are not closed, e.i. their end time
     * is not specified.
     *
     * @param toDate - the current time to use to calculate the work done for started but unfinished plans.
     */
    public void updateActualTimeIntervalFromPlanParts(Date fromDate, Date toDate){

        List<LongInterval> intervals = planParts.stream()
                .map(p -> p.convertActualTimeInterval(fromDate, toDate))
                .filter(DEFINED_INTERVAL)
                .collect(Collectors.toList());

        LongInterval unionInterval = LongIntervalImpl.union(intervals);
        if(unionInterval != null) {
            setStartTime(new Date(unionInterval.getStart()));
//            setEndTime(new Date(unionInterval.getEnd()));
            setEndTime(new Date(unionInterval.getEnd()));
            setDuration(unionInterval.getLength());
        }
    }

    public void updateActualWorkTimeFromPlanParts(){
        long plannedWorkTime = planParts.stream().mapToLong(AbstractPlan::getWorkTime).sum();
        setWorkTime(plannedWorkTime);
    }

    public Stream<AbstractPlan> streamPlanParts(){
        return Stream.concat(
                Stream.of(this),
                getPlanParts().stream().flatMap(
                        p -> p instanceof AbstractComplexPlan
                                ? ((AbstractComplexPlan<?>) p).streamPlanParts()
                                : Stream.of(p)
                )
        );
    }

    public Stream<AbstractPlan> streamPlanPartsBottomUp(){
        return Stream.concat(
                getPlanParts().stream().flatMap(
                        p -> p instanceof AbstractComplexPlan
                                ? ((AbstractComplexPlan<?>) p).streamPlanPartsBottomUp()
                                : Stream.of(p)
                ),
                Stream.of(this)
        );
    }

    public void applyOperationBottomUp(Consumer<AbstractComplexPlan> op){
        Set<? extends AbstractPlan> planParts = getPlanParts();
        // first apply operation to plan parts
        for(AbstractPlan planPart : planParts){
            if(planPart instanceof AbstractComplexPlan){
                ((AbstractComplexPlan<?>) planPart).applyOperationBottomUp(op);
            }
        }

        // apply to this plan
        op.accept(this);
    }


    public static Predicate<LongInterval> DEFINED_INTERVAL = i -> i.getStart() != null && i.getEnd() != null;
}
