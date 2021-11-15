package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.planners;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.SequencePattern;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;

import java.util.*;
import java.util.stream.Collectors;

public class OriginalPlanner {

    public static final OriginalPlanner planner = new OriginalPlanner();

    public List<SequencePattern> plan(List<Result> wp){
//        List<Result> sequence = new ArrayList<>();
//        wp.sort(Comparator.comparing(r -> r.start.getTime()));
//        Set<TaskType> usedTaskTypes = new HashSet<>();
//        for(Result r : wp){
//            if(usedTaskTypes.contains(r.taskType))
//                continue;
//            sequence.add(r);
//            usedTaskTypes.add(r.taskType);
//        }

        List<Result> sequence = wp;
        if(sequence.isEmpty())
            return Collections.emptyList();

        List<SequencePattern> plan = new ArrayList<>();
        Iterator<Result> i = sequence.iterator();
        Result last = i.next();
        while (i.hasNext()) {
            Result next = i.next();
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
    public List<SequencePattern> planSameTime(List<Result> wp){

        List<List<Result>> sequence = wp.stream()
                .collect(Collectors.groupingBy(r -> r.start.getTime()))
                .values().stream().sorted(Comparator.comparing(l -> l.get(0).start.getTime()))
                .collect(Collectors.toList());

        if(sequence.isEmpty())
            return Collections.emptyList();

        List<SequencePattern> plan = new ArrayList<>();
        Iterator<List<Result>> i = sequence.iterator();
        List<Result> lastl = i.next();


        while (i.hasNext()) {
            List<Result> nextl = i.next();
            for(Result last : lastl) {
                for (Result next : nextl){
                    SequencePattern pat = createPattern(last,next);
                    plan.add(pat);
                }
            }
            lastl = nextl;
        }

        SequencePattern.calculateSupportClasses(plan);
        return plan;
    }

    protected SequencePattern createPattern(Result r1, Result r2){
        SequencePattern pat = new SequencePattern();
        pat.pattern = Arrays.asList(r1.taskType, r2.taskType);
        pat.instances.add(Arrays.asList(r1, r2));
        return pat;
    }

}
