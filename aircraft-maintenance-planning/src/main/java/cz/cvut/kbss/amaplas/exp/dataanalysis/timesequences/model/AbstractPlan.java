package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.base.LongInterval;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.base.LongIntervalWrapper;
import cz.cvut.kbss.amplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.MappedSuperclass;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;

import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

@MappedSuperclass
public abstract class AbstractPlan<ID> extends AbstractEntity<ID>{
//    protected EventType eventType;
//    protected String description;
    protected Resource resource;

    // planned temporal properties
    @JsonProperty("planned-start-time")
    protected Date plannedStartTime;
    @JsonProperty("planned-end-time")
    protected Date plannedEndTime;
    @JsonProperty("planned-duration")
    protected Long plannedDuration;

    @JsonProperty("planned-work-time")
    protected Long plannedWorkTime;

    // actual temporal properties
    @JsonProperty("start-time")
    protected Date startTime;
    @JsonProperty("end-time")
    protected Date endTime;
    protected Long duration;

    @JsonProperty("work-time")
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

//    public static void main(String[] args) {
//        Function<AbstractPlan, Long> safeStartTimeFunction = getTimeFunctionSafe(AbstractPlan::getStartTime, null);
//        Function<AbstractPlan, Long> startTimeFunction = getTimeFunction(AbstractPlan::getStartTime);
//        SessionPlan p = new SessionPlan();
//        Long l1 = safeStartTimeFunction.apply(p);
//        System.out.println(l1);
//        Long l2 = startTimeFunction.apply(p);
//        System.out.println(l2);
//    }
}
