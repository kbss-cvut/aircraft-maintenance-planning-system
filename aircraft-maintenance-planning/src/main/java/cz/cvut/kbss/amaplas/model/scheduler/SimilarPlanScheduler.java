package cz.cvut.kbss.amaplas.model.scheduler;

import cz.cvut.kbss.amaplas.model.*;
import cz.cvut.kbss.amaplas.model.values.DateUtils;
import cz.cvut.kbss.amaplas.planners.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Schedules a revision plan using a set of partial orders of task types found in the revision. The schedule sets the
 * start of the revision to the provided start date. Note that each partial order is represented as a graph where nodes
 * are represented by the class TaskPattern and edges by the class SequencePatterns. Nodes and edges should contain
 * references to supporting events, e.g. work sessions.
 */
public class SimilarPlanScheduler implements PlanScheduler{

    private static final Logger LOG = LoggerFactory.getLogger(SimilarPlanScheduler.class);

    protected Map<String, PlanGraph> groupedPartialTaskOrder;
    protected PlanGraph findingOrder;
    protected long planStartTime;

    public SimilarPlanScheduler(Date planStartTime, Map<String, PlanGraph> groupedPartialTaskOrder, PlanGraph findingOrder) {
        this.planStartTime = planStartTime.getTime();
        this.groupedPartialTaskOrder = groupedPartialTaskOrder;
        this.findingOrder = findingOrder;
    }

    @Override
    public void schedule(RevisionPlan revisionPlan, Workpackage wp) {
        long defaultBufferBetweenSchedules = 15*60*1000;
        Map<TaskType, TaskPlan> taskPlanMap = new HashMap<>();
        revisionPlan.streamPlanParts()
                .filter(p -> p instanceof TaskPlan)
                .map(p -> (TaskPlan)p)
                .forEach(p -> taskPlanMap.put(p.getTaskType(), p));

        BiFunction<Optional<LocalDate>, Function<LocalDate, Date>, Long> toMillis = (oLD, toDate) -> oLD
                .map(toDate)
                .map(Date::getTime)
                .orElse(null);

        Function<Optional<LocalDate>, Long> startTime = oLD -> toMillis.apply(
                oLD,
                ld -> DateUtils.toDateAtTime(ld, 7, 0)
        );

        Function<Optional<LocalDate>, Long> endTime = oLD -> toMillis.apply(
                oLD,
                ld -> DateUtils.toDateAtTime(ld, 19, 0)
        );

        Long wpStart = Optional.ofNullable(startTime.apply(Optional.ofNullable(wp)
                .map(p -> p.getPlannedStartTime() != null ? p.getPlannedStartTime() : p.getStartTime())
        )).orElse(wp.getStart() != null ? wp.getStart().getTime() : new Date().getTime() );

        Long wpEnd = Optional.ofNullable(endTime.apply(Optional.ofNullable(wp)
                .map(p -> p.getPlannedEndTime() != null ? p.getPlannedEndTime() : p.getEndTime())
        )).orElse(wp.getEnd() != null ? wp.getEnd().getTime() : wpStart + 7 * 3600 * 24 * 1000);

        for(Map.Entry<String, PlanGraph> e : groupedPartialTaskOrder.entrySet()){
            ReuseBasedPlanner.planner.traverse(e.getValue(), (planGraph, node, edge, source) -> {
                // schedule the time of the task plan
                TaskPlan taskPlan = taskPlanMap.get(node.getTaskType());
                Long plannedStartTime = null;

                if(node.getInstances() == null || node.getInstances().isEmpty())
                    return;
                // Do not schedule tasks (e.g. maintenance wo) which reference other tasks.
                if(edge == null && source == null && findingOrder.containsVertex(node) && !findingOrder.incomingEdgesOf(node).isEmpty())
                    return;
                // TODO - there should be a default duration or task should not be scheduled?
                Double dDuration = node.getTaskType().getAverageTime();
                long duration = (long)( (dDuration != null ? dDuration : 4.) * 3600000); //end - taskExecution.getStart().getTime();

                Long workTime = null; // TODO - replace with node.getTaskType().getAverageWorkTime()
                if(edge == null || edge.patternType == null){
                    plannedStartTime = planStartTime;
                    workTime = node.getInstances().get(0).getWorkTime(); // TODO - replace with node.getTaskType().getAverageWorkTime()
                }else {
                    TaskExecution sourceHistory = edge.instances.get(0).get(0);
                    TaskExecution targetHistory = edge.instances.get(0).get(1);
                    workTime = targetHistory.getWorkTime(); // TODO - replace with node.getTaskType().getAverageWorkTime()

                    TaskPlan sourceTaskPlan = taskPlanMap.get(source.getTaskType());
                    if(sourceTaskPlan.getPlannedStartTime() == null) // do not schedule if the source plan is not scheduled
                        return;

                    if(edge.patternType == PatternType.EQUALITY){
                        plannedStartTime = sourceTaskPlan.getPlannedStartTime().getTime();
                    } else if(edge.patternType == PatternType.STRICT_DIRECT_ORDER){
                        long sourceStart = sourceHistory.getStart().getTime();
                        long sourceEnd = sourceHistory.getEnd().getTime();
                        long targetStart = targetHistory.getStart().getTime();

                        if(targetStart < sourceEnd)
                            plannedStartTime = sourceTaskPlan.getPlannedStartTime().getTime() + (long)(
                                    (sourceTaskPlan.getPlannedEndTime().getTime() - sourceTaskPlan.getPlannedStartTime().getTime())*( targetStart - sourceStart)*1./(sourceEnd - sourceStart)
                            );
                        else
                            plannedStartTime = sourceTaskPlan.getPlannedStartTime().getTime() + targetStart - sourceStart;
//                            plannedStartTime = sourceTaskPlan.getPlannedEndTime().getTime() + defaultBufferBetweenSchedules;// TODO sourceTaskPlan.getPlannedEndTime() == null ????

                    } else if(edge.patternType == PatternType.STRICT_INDIRECT_ORDER) {
                        long sourceStart = sourceHistory.getStart().getTime();
                        long sourceEnd = sourceHistory.getEnd().getTime();
                        long targetStart = targetHistory.getStart().getTime();

                        if (targetStart < sourceEnd) {
                            plannedStartTime = sourceTaskPlan.getPlannedStartTime().getTime() + (long) (
                                    (sourceTaskPlan.getPlannedEndTime().getTime() - sourceTaskPlan.getPlannedStartTime().getTime()) * (targetStart - sourceStart) * 1. / (sourceEnd - sourceStart)
                            );
                        } else {

                            Workpackage reusedWP = sourceHistory.getWorkpackage();


                            Long reusedWPStart = startTime.apply(Optional.ofNullable(reusedWP)
                                        .map(p -> p.getStartTime() != null ? p.getStartTime() : p.getPlannedStartTime())
                            );

                            Long reusedWPEnd = endTime.apply(Optional.ofNullable(reusedWP)
                                    .map(p -> p.getEndTime() != null ? p.getEndTime() : p.getPlannedEndTime())
                            );

                            if(Stream.of(reusedWPStart, reusedWPEnd).anyMatch(l -> l == null))
                                return;

                            plannedStartTime = sourceTaskPlan.getPlannedStartTime().getTime() +
                                    (long) ((wpEnd - wpStart) * ((targetStart - sourceStart) * (2./5.) / (reusedWPEnd - reusedWPStart)));
                        }
                    }
                }
                if(taskPlan.getPlannedStartTime() == null || (plannedStartTime != null && taskPlan.getPlannedStartTime().getTime() < plannedStartTime)) {
                    taskPlan.setPlannedStartTime(new Date(plannedStartTime));
                    taskPlan.setPlannedEndTime(new Date(plannedStartTime + duration));
                    taskPlan.setPlannedWorkTime(workTime);
                }
            });
        }
        // schedule WOs
        ReuseBasedPlanner.planner.traverse(findingOrder, (planGraph, node, edge, source) -> {
            if(source == null || edge == null || node == null) // do not schedule the root nodes, they should be scheduled in the first scheduling phase
                return;
            TaskPlan sourcePlan = taskPlanMap.get(source.getTaskType());
            TaskPlan targetPlan = taskPlanMap.get(node.getTaskType());
            if(sourcePlan == null || targetPlan == null || sourcePlan.getPlannedStartTime() == null || sourcePlan.getPlannedEndTime() == null)
                return;
            Double duration = targetPlan.getTaskType().getAverageTime();
            if(duration == null)
                duration = 4.;

            targetPlan.setPlannedStartTime(new Date(sourcePlan.getPlannedEndTime().getTime() + defaultBufferBetweenSchedules));
            targetPlan.setPlannedEndTime(new Date(targetPlan.getPlannedStartTime().getTime() + (long)(duration*1000*3600)));
        });

        revisionPlan.applyOperationBottomUp( p -> {
            if(!(p instanceof TaskPlan) )
                p.updatePlannedTemporalAttributesFromPlanParts();
        });
    }

    protected String getEdgeWP(TaskPattern node, SequencePattern edge, TaskPattern source){
        if(edge == null ) {
            if(node.getInstances() != null && !node.getInstances().isEmpty()) {
                return node.getInstances().get(0).getWorkpackage().getId();
            }
        }else{
            if(!Objects.equals(
                    edge.instances.get(0).get(0).getWorkpackage().getId(),
                    edge.instances.get(0).get(1).getWorkpackage().getId())) { // DEBUG
                LOG.warn("Support instance {} of edge has two different is from two different workpackages", edge.instances.get(0));
            }
            return edge.instances.get(0).get(0).getWorkpackage().getId();
        }
        return null;
    }
}
