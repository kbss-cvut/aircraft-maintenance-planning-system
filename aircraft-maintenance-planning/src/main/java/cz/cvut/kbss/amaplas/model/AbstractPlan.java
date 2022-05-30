package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.model.base.LongInterval;
import cz.cvut.kbss.amaplas.model.base.LongIntervalWrapper;
import cz.cvut.kbss.amaplas.model.visit.PlanVisitor;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;

import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

@OWLClass(iri = Vocabulary.s_c_event_plan)
public abstract class AbstractPlan extends AbstractEntity<Long>{
    @OWLObjectProperty(iri=Vocabulary.s_p_has_participant)
    protected Resource resource;

    // planned temporal properties
    @OWLDataProperty(iri = Vocabulary.s_p_planned_start_time)
    protected Date plannedStartTime;
    @OWLDataProperty(iri = Vocabulary.s_p_planned_end_time)
    protected Date plannedEndTime;
    @OWLDataProperty(iri = Vocabulary.s_p_planned_duration)
    protected Long plannedDuration;

    @OWLDataProperty(iri = Vocabulary.s_p_planned_work_time)
    protected Long plannedWorkTime;

    // actual temporal properties
    @OWLDataProperty(iri = Vocabulary.s_p_start_time)
    protected Date startTime;
    @OWLDataProperty(iri = Vocabulary.s_p_end_time)
    protected Date endTime;
    @OWLDataProperty(iri = Vocabulary.s_p_duration)
    protected Long duration;

    @OWLDataProperty(iri = Vocabulary.s_p_work_time)
    protected Long workTime;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Date getPlannedStartTime() {
        return plannedStartTime;
    }

    public void setPlannedStartTime(Date plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
    }

    public Date getPlannedEndTime() {
        return plannedEndTime;
    }

    public void setPlannedEndTime(Date plannedEndTime) {
        this.plannedEndTime = plannedEndTime;
    }

    public Long getPlannedDuration() {
        return plannedDuration;
    }

    public void setPlannedDuration(Long plannedDuration) {
        this.plannedDuration = plannedDuration;
    }

    public Long getPlannedWorkTime() {
        return plannedWorkTime;
    }

    public void setPlannedWorkTime(Long plannedWorkTime) {
        this.plannedWorkTime = plannedWorkTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
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

    public void updateTemporalAttributes(Date toDate){
        // do nothing, used for complex tasks to calculate their temporal properties from their parts.
    }

    public void accept(PlanVisitor visitor){
        visitor.visit(this);
    }

    public LongInterval convertPlannedTimeInterval(Long startMissing, Long endMissing){
        return convertToInterval(
                this,
                getTimeFunctionSafe(AbstractPlan::getPlannedStartTime, startMissing),
                getTimeFunctionSafe(AbstractPlan::getPlannedEndTime, endMissing)
        );
    }



    public LongInterval convertActualTimeInterval(Date fromDate, Date toDate){
        return convertToInterval(
                this,
                getTimeFunctionSafe(AbstractPlan::getStartTime, fromDate != null ? fromDate.getTime() : null),
                getTimeFunctionSafe(AbstractPlan::getEndTime, toDate != null ? toDate.getTime() : null)
        );
    }


    public static Function<AbstractPlan, Long> getTimeFunction(Function<AbstractPlan, Date> dateFunction){
        return dateFunction.andThen(Date::getTime);
    }

    public static Function<AbstractPlan, Long> getTimeFunctionSafe(Function<AbstractPlan, Date> dateFunction, Long orElse){
        return d -> Optional
                .ofNullable(dateFunction.apply(d))
                .map(Date::getTime)
                .orElse(orElse);
    }
//
//    public static Function<AbstractPlan, Long> getTimeFunction(Function<AbstractPlan, Date> date){
//        return date.andThen(Date::getTime);
//    }

    public static LongInterval convertToInterval(AbstractPlan p, Function<AbstractPlan, Long> start, Function<AbstractPlan, Long> end){
        return new LongIntervalWrapper() {
            @Override
            public Object getWrapped() {
                return p;
            }

            @Override
            public Long getStart() {
                return start.apply(p);
            }

            @Override
            public Long getEnd() {
                return end.apply(p);
            }
        };
    }
}
