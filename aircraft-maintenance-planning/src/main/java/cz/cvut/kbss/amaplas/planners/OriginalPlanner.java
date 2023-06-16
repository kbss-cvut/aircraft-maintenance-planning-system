package cz.cvut.kbss.amaplas.planners;

import cz.cvut.kbss.amaplas.model.Result;
import cz.cvut.kbss.amaplas.model.TaskExecution;

import java.util.*;
import java.util.stream.Collectors;

public class OriginalPlanner {

    public static final OriginalPlanner planner = new OriginalPlanner();

    public List<SequencePattern> plan(List<TaskExecution> wp){
//        List<Result> sequence = new ArrayList<>();
//        wp.sort(Comparator.comparing(r -> r.start.getTime()));
//        Set<TaskType> usedTaskTypes = new HashSet<>();
//        for(Result r : wp){
//            if(usedTaskTypes.contains(r.taskType))
//                continue;
//            sequence.add(r);
//            usedTaskTypes.add(r.taskType);
//        }

        List<TaskExecution> sequence = wp;
        if(sequence.isEmpty())
            return Collections.emptyList();

        List<SequencePattern> plan = new ArrayList<>();
        Iterator<TaskExecution> i = sequence.iterator();
        TaskExecution last = i.next();
        while (i.hasNext()) {
            TaskExecution next = i.next();
            SequencePattern pat = createPattern(last,next);
            plan.add(pat);
            last = next;
        }
        SequencePattern.calculateSupportClasses(plan);
        return plan;
    }

    public void validatePlan(){

    }

    /**
     * It does not order the task types which start at the same time
     * @param wp
     * @return
     */
    public List<SequencePattern> planSameTime(List<TaskExecution> wp){

        List<List<TaskExecution>> sequence = wp.stream()
                .collect(Collectors.groupingBy(r -> r.getStart().getTime()))
                .values().stream().sorted(Comparator.comparing(l -> l.get(0).getStart().getTime()))
                .collect(Collectors.toList());

        if(sequence.isEmpty())
            return Collections.emptyList();

        List<SequencePattern> plan = new ArrayList<>();
        Iterator<List<TaskExecution>> i = sequence.iterator();
        List<TaskExecution> lastl = i.next();


        while (i.hasNext()) {
            List<TaskExecution> nextl = i.next();
            for(TaskExecution last : lastl) {
                for (TaskExecution next : nextl){
                    SequencePattern pat = createPattern(last,next);
                    plan.add(pat);
                }
            }
            lastl = nextl;
        }

        SequencePattern.calculateSupportClasses(plan);
        return plan;
    }

    protected SequencePattern createPattern(TaskExecution r1, TaskExecution r2){
        SequencePattern pat = new SequencePattern();
        pat.pattern = Arrays.asList(r1.getTaskType(), r2.getTaskType());
        pat.instances.add(Arrays.asList(r1, r2));
        return pat;
    }

}
