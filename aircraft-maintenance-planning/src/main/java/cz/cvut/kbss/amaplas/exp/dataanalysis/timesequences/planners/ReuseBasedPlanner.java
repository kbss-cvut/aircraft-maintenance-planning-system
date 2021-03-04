package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.planners;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.ExtractData;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.PatternType;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.SequencePattern;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.seqalg.FilterTransitiveEdgesAlg;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ReuseBasedPlanner {

    public static final ReuseBasedPlanner planner = new ReuseBasedPlanner();



    public List<SequencePattern> planDisconnected(Map<String, List<Result>> history, Set<TaskType> taskTypes, SimilarityMeasure similarityMeasure) {
        return plan(history, taskTypes, DISCONNECTED_ORDER, similarityMeasure);
    }
    public List<SequencePattern> planConnected(Map<String, List<Result>> history, Set<TaskType> taskTypes, SimilarityMeasure similarityMeasure){
        return plan(history, taskTypes, CONNECTED_ORDER, similarityMeasure);
    }

    public List<SequencePattern> plan(Map<String, List<Result>> history, Set<TaskType> taskTypes,
                                                  UnacceptableOrder isUnacceptableOrder,
                                                  SimilarityMeasure similarityMeasure){
        // order history wps according to similarity with taskTypes
        List<Pair<String, Set<TaskType>>> similarPlans = history.entrySet().stream().map(e -> Pair.of(
                e.getKey(),
                e.getValue().stream().map(r -> r.taskType).collect(Collectors.toSet()))
        ).sorted(Comparator.comparing(
                (Pair<String, Set<TaskType>> p) -> similarityMeasure.similarity(p.getValue(), taskTypes)
                ).reversed()// biggest number at the beginning
        ).collect(Collectors.toList());

        int k = 0;
        Set<TaskType> remainder = new HashSet<>(taskTypes);
        Set<TaskType> plannedByThisWP = new HashSet<>();
        Set<TaskType> plannedByOtherWP = new HashSet<>();
        Result t1 = null;
        Result t2 = null;

        List<SequencePattern> plan = new ArrayList<>();
        Set<TaskType> tasks = new HashSet<>();

        while(!remainder.isEmpty() && k < similarPlans.size()){ // revise condition

            String key = similarPlans.get(k).getKey();
//            Set<TaskType> sim = similarPlans.get(k).getValue(); // Error - tasks type set of the similar plan in history, may contain task types which are not part of the set of tasks to be planned

//            Set<TaskType> sim = new HashSet<>(similarPlans.get(k).getValue());
//            sim.retainAll(remainder); // work only on non
            t1 = null;// if null the alg will calculate try to connect the remaining tasks to all of the tasks, if t1 is not reset to null the alg will try to order from the end of the last planned task
            List<Result> p = history.get(key);

            // induce the orders between tasks common with the selected plan
            for(int i = 0; i < p.size() - 1; i ++ ){
                if(!taskTypes.contains(p.get(i).taskType))
                    continue;
                // select the first task
                if(t1 == null) {
                    t1 = p.get(i);
                    continue;
                }


                t2 = p.get(i);
//                if(t1.taskType.equals(t2.taskType) && t1.scope.equals(t2.scope)) // hack - this condition should be given as input parameter
                // filter irrelevant orderings
                if(t1.taskType.equals(t2.taskType) || // do not plan the same task
                        !(remainder.contains(t1.taskType) || remainder.contains(t2.taskType)) // one of the task types should be from the reminder, i.e., do not plan edges that are already planned using previous WPs
                ) // hack - this condition should be given as input parameter
                    continue;
                List<TaskType> pat = Arrays.asList(t1.taskType, t2.taskType);
                List<Result> instances = Arrays.asList(t1, t2);

                boolean planned = isUnacceptableOrder.isOrderAcceptable(instances, plannedByOtherWP, plan);
                if(planned)
                    continue;

                // add ordered pair to plan
                SequencePattern seqpat = new SequencePattern();
                seqpat.pattern = pat;
                seqpat.instances.add(instances);
                if(t1.start.getTime() == t2.start.getTime()){
                    seqpat.patternType = PatternType.EQUALITY;
                }
                plan.add(seqpat);

                // finish iteration
                plannedByThisWP.add(t1.taskType);
                plannedByThisWP.add(t2.taskType);
                remainder.remove(t1.taskType);
                remainder.remove(t2.taskType);
                t1 = t2;
            }
            // prepare to plan for the next wp
            plannedByOtherWP.addAll(plannedByThisWP);
            plannedByThisWP = new HashSet<>();
            k ++;
        }

        // combine tasks which start at the same time

        SequencePattern.calculateSupportClasses(plan);
        return plan;
    }

    public List<SequencePattern> plan_algorithm_version_001(Map<String, List<Result>> history, Set<TaskType> taskTypes,
                                      UnacceptableOrder isUnacceptableOrder,
                                      SimilarityMeasure similarityMeasure){
        // order history wps according to similarity with taskTypes
        List<Pair<String, Set<TaskType>>> similarPlans = history.entrySet().stream().map(e -> Pair.of(
                e.getKey(),
                e.getValue().stream().map(r -> r.taskType).collect(Collectors.toSet()))
        ).sorted(Comparator.comparing(
                        (Pair<String, Set<TaskType>> p) -> similarityMeasure.similarity(p.getValue(), taskTypes)
                ).reversed()// biggest number at the beginning
        ).collect(Collectors.toList());
//        Map<String, Set<TaskType>> commonTasks = new HashMap<>();
//        history.entrySet().stream()
//                .forEach(e -> {
//                            Set<TaskType> set = e.getValue().stream().map(r -> r.taskType).collect(Collectors.toSet());
//                            set.retainAll(taskTypes);
//                            commonTasks.put(e.getKey(), set);
//                        }
//                );
//        List<Pair<String, Set<TaskType>>> similarPlans = commonTasks.entrySet().stream()
//                .map(e -> Pair.of(e.getKey(), e.getValue()))
//                .sorted(Comparator.comparing((Pair<String, Set<TaskType>> p) -> p.getValue().size()).reversed())
//                .collect(Collectors.toList());

        int k = 0;
        Set<TaskType> remainder = new HashSet<>(taskTypes);
        Result t1 = null;
        Result t2 = null;

        List<SequencePattern> plan = new ArrayList<>();
        Set<Set<TaskType>> orderedTaskSets = new HashSet<>();
        Set<TaskType> usedTasks = new HashSet<>();
        while(!remainder.isEmpty() && k < similarPlans.size()){ // revise condition

            String key = similarPlans.get(k).getKey();
            Set<TaskType> sim = similarPlans.get(k).getValue(); // Error - tasks type set of the similar plan in history, may contain task types which are not part of the set of tasks to be planned

//            Set<TaskType> sim = new HashSet<>(similarPlans.get(k).getValue());
//            sim.retainAll(remainder); // work only on non
            List<Result> p = history.get(key);
            Set<TaskType> plannedTasks = new HashSet<>();
            for(int i = 0; i < p.size() - 1; i ++ ){
                if(!taskTypes.contains(p.get(i).taskType))
                    continue;
                if(t1 == null) {
                    t1 = p.get(i);
                    plannedTasks.add(t1.taskType);
                    continue;
                }
                t2 = p.get(i);
                if(t1.taskType.equals(t2.taskType) && t1.scope.equals(t2.scope)) // hack - this condition should given as input parameter
                    continue;
                List<TaskType> pat = Arrays.asList(t1.taskType, t2.taskType);
                List<Result> instances = Arrays.asList(t1, t2);

//                boolean planned = isUnacceptableOrder.isOrderAcceptable(instances, plannedTasks, plan);
                boolean planned = false;
                for(Set<TaskType> orderedTasks : orderedTaskSets){
                    if(isUnacceptableOrder.isOrderAcceptable(instances, orderedTasks, plan)){
//                    if(orderedTasks.containsAll(pat)) {
//                    if(orderedTasks.contains(t1.taskType) || orderedTasks.contains(t2.taskType)) {
                        planned = true;
                        break;
                    }
                }
                if(planned)
                    continue;
                // add ordered pair to plan
                SequencePattern seqpat = new SequencePattern();
                seqpat.pattern = pat;
                seqpat.instances.add(instances);
                if(t1.start.getTime() == t2.start.getTime()){
                    seqpat.patternType = PatternType.EQUALITY;
                }
                plan.add(seqpat);

                // finish iteration
                t1 = t2;
                plannedTasks.add(t2.taskType);
//                FilterTransitiveEdgesAlg filterTransitiveEdges = FilterTransitiveEdgesAlg.filterTransitiveEdges(plan);
//                plan =
            }
            if(plannedTasks.size() > 1) {
                orderedTaskSets.add(plannedTasks);
                usedTasks.addAll(plannedTasks);
            }

            remainder.removeAll(sim);
            k ++;
        }

        // combine tasks which start at the same time

        SequencePattern.calculateSupportClasses(plan);
        return plan;
    }

    public interface SimilarityOrder{
        double[] similarity(List<Pair<String, Set<TaskType>>> pkgs, Set<TaskType> taskTypes);
    }

    public interface SimilarityMeasure{
        double similarity(Set<TaskType> p1, Set<TaskType> p2);
    }

    public static final SimilarityOrder SIMILARITY_ORDER = ReuseBasedPlanner::similarityOrder;

    public static double[] similarityOrder(List<Pair<String, Set<TaskType>>> pkgs, Set<TaskType> taskTypes){
        double[] ret = new double[pkgs.size()];
        for( int i = 0; i < pkgs.size(); i++ ){
            ret[i] = ExtractData.calculateSetSimilarity(pkgs.get(i).getValue(), taskTypes);
        }
        return ret;
    }

    public interface UnacceptableOrder {
        boolean isOrderAcceptable(List<Result> pat, Set<TaskType> orderedTasks, List<SequencePattern> plan);
    }

    public static final UnacceptableOrder DISCONNECTED_ORDER = (pat, orderedTasks, plan) -> pat.stream().map(r -> r.taskType).anyMatch(orderedTasks::contains);
    public static final UnacceptableOrder CONNECTED_ORDER = (pat, orderedTasks, plan) -> pat.stream().map(r -> r.taskType).allMatch(orderedTasks::contains);

}
