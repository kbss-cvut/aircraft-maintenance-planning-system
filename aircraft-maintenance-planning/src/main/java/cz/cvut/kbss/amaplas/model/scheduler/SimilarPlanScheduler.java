package cz.cvut.kbss.amaplas.model.scheduler;

import cz.cvut.kbss.amaplas.model.*;
import cz.cvut.kbss.amaplas.planners.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Schedules a revision plan using a set of partial orders of task types found in the revision. The schedule sets the
 * start of the revision to the provided start date. Note that each partial order is represented as a graph where nodes
 * are represented by the class TaskPattern and edges by the class SequencePatterns. Nodes and edges should contain
 * references to supporting events, e.g. work sessions.
 */
public class SimilarPlanScheduler implements PlanScheduler{

    private static final Logger LOG = LoggerFactory.getLogger(SimilarPlanScheduler.class);

    protected Map<String, PlanGraph> groupedPartialTaskOrder;
    protected Date planStartTime;

    public SimilarPlanScheduler(Date planStartTime, Map<String, PlanGraph> groupedPartialTaskOrder) {
        this.planStartTime = planStartTime;
        this.groupedPartialTaskOrder = groupedPartialTaskOrder;
    }

    @Override
    public void schedule(RevisionPlan revisionPlan) {
        Map<TaskType, TaskPlan> taskPlanMap = new HashMap<>();
        revisionPlan.streamPlanParts()
                .filter(p -> p instanceof TaskPlan)
                .map(p -> (TaskPlan)p)
                .forEach(p -> taskPlanMap.put(p.getTaskType(), p));



        for(Map.Entry<String, PlanGraph> e : groupedPartialTaskOrder.entrySet()){
            ReuseBasedPlanner.planner.traverse(e.getValue(), (planGraph, node, edge, source) -> {
                // schedule the time of the task plan
                TaskPlan taskPlan = taskPlanMap.get(node.getTaskType());
                Date plannedStartTime = null;

                if(node.getInstances() == null || node.getInstances().isEmpty())
                    return;

                String edgeWP = getEdgeWP(node, edge, source);

                TaskExecution taskExecution = node.getInstance(te -> Objects.equals(edgeWP, te.getWorkpackage().getId()));

                Long end = Optional.ofNullable(taskExecution.getEnd()).map(Date::getTime).orElse(-1L);
                if(end < 0)
                    end = taskExecution.getStart().getTime() + 3600000;
                long duration = end - taskExecution.getStart().getTime();

                long workTime = taskExecution.getWorkTime();
                if(edge == null || edge.patternType == null){
                    plannedStartTime = planStartTime;

                }else if(edge.patternType == PatternType.EQUALITY){
                    TaskPlan previousTaskPlan = taskPlanMap.get(source.getTaskType());
                    plannedStartTime = previousTaskPlan.getPlannedStartTime();
                }else if(edge.patternType == PatternType.STRICT_DIRECT_ORDER){
                    TaskPlan previousTaskPlan = taskPlanMap.get(source.getTaskType());
                    TaskExecution previousTaskExecution = source.getInstance(taskExecution, te -> te.getWorkpackage().getId()); //source.instances.stream()

                    long startTimeDistance = taskExecution.getStart().getTime() - previousTaskExecution.getStart().getTime();
                    plannedStartTime = new Date(previousTaskPlan.getStartTime().getTime() + startTimeDistance);
                } else if(edge.patternType == PatternType.STRICT_INDIRECT_ORDER){
                    TaskPlan previousTaskPlan = taskPlanMap.get(source.getTaskType());
                    TaskExecution previousTaskExecution = source.getInstance(te -> Objects.equals(edgeWP, te.getWorkpackage().getId())); //source.getInstance(sessions, r -> r.wp); //source.instances.stream()

                    long endToStart = taskExecution.getStart().getTime() - previousTaskExecution.getEnd().getTime();
                    if(endToStart > 0)
                        endToStart = 0;

                    plannedStartTime =  new Date(previousTaskPlan.getPlannedEndTime().getTime() + endToStart);
                }

                taskPlan.setPlannedStartTime(plannedStartTime);
                taskPlan.setPlannedEndTime(new Date(plannedStartTime.getTime() + duration));

                taskPlan.setPlannedWorkTime(workTime);

                revisionPlan.applyOperationBottomUp( p -> {
                    if(!(p instanceof TaskPlan) )
                        p.updatePlannedTemporalAttributesFromPlanParts();
                });
            });
        }
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
