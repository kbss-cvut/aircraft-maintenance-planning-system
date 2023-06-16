package cz.cvut.kbss.amaplas.planners;

import cz.cvut.kbss.amaplas.algs.SimilarityUtils;
import cz.cvut.kbss.amaplas.model.Result;
import cz.cvut.kbss.amaplas.model.TaskExecution;
import cz.cvut.kbss.amaplas.model.TaskType;
import cz.cvut.kbss.amaplas.model.Workpackage;
import cz.cvut.kbss.amaplas.utils.GraphmlUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReuseBasedPlanner {

    public static final ReuseBasedPlanner planner = new ReuseBasedPlanner();

    public List<SequencePattern> flattenPartialOrderBreadthFirst1(List<SequencePattern> partialOrder){
        DefaultDirectedGraph<TaskType, SequencePattern> g = GraphmlUtils.toGraph(partialOrder);
        Set<TaskType> tts = g.vertexSet();

        Iterator<Map.Entry<Integer, List<TaskType>>> taskTypesByDegree = tts.stream().collect(Collectors.groupingBy(g::inDegreeOf))
                .entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey()))
                .iterator();


        Set<TaskType> visitedTaskTypes = new HashSet<>();
        List<TaskType> result = new ArrayList();
        while(tts.size() > visitedTaskTypes.size() && taskTypesByDegree.hasNext()){
            List<TaskType> startingNodes = taskTypesByDegree.next().getValue();
            if(startingNodes == null)
                continue;
            BreadthFirstIterator<TaskType, SequencePattern> iter = new BreadthFirstIterator<>(g, startingNodes);
            while(iter.hasNext()){
                TaskType tt = iter.next();
                if(visitedTaskTypes.add(tt))
                    result.add(tt);
            }
        }

        return null;
    }


    public List<TaskType> flattenPartialOrderBreadthFirst(List<SequencePattern> partialOrder) {
        PlanGraph g = toGraph(partialOrder);
        return flattenPartialOrderBreadthFirst(g);
    }
    public List<TaskType> flattenPartialOrderBreadthFirst(PlanGraph planGraph){
        Set<TaskPattern> tts = planGraph.vertexSet();

        Iterator<Map.Entry<Integer, List<TaskPattern>>> taskTypesByDegree = tts.stream().collect(Collectors.groupingBy(planGraph::inDegreeOf))
                .entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey()))
                .iterator();


        Set<TaskPattern> visitedTaskTypes = new HashSet<>();
        List<TaskType> result = new ArrayList();
        while(tts.size() > visitedTaskTypes.size() && taskTypesByDegree.hasNext()){
            List<TaskPattern> startingNodes = taskTypesByDegree.next().getValue();
            if(startingNodes == null)
                continue;
            BreadthFirstIterator<TaskPattern, SequencePattern> iter = new BreadthFirstIterator<>(planGraph, startingNodes);
            while(iter.hasNext()){
                TaskPattern tt = iter.next();
                if(visitedTaskTypes.add(tt))
                    result.add(tt.getTaskType());
            }
        }

        return result;
    }

    public void traverse(PlanGraph planGraph, NodeVisitor nodeVisitor){
        Set<TaskPattern> taskPatterns = planGraph.vertexSet();
        Iterator<Map.Entry<Integer, List<TaskPattern>>> taskTypesByDegree = taskPatterns.stream()
                .collect(Collectors.groupingBy(planGraph::inDegreeOf))
                .entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey()))
                .iterator();


        Set<TaskPattern> visitedTaskTypes = new HashSet<>();

        while(taskPatterns.size() > visitedTaskTypes.size() && taskTypesByDegree.hasNext()){
            LinkedList<Pair<SequencePattern, TaskPattern>> queue = new LinkedList<>();
            taskTypesByDegree.next().getValue().forEach(n -> queue.add(Pair.of(null, n)));

            while(!queue.isEmpty()){
                Pair<SequencePattern, TaskPattern> visitation = queue.poll();
                SequencePattern edge = visitation.getLeft();
                TaskPattern target = visitation.getRight();
                if(visitedTaskTypes.add(target)) {
                    nodeVisitor.visit(
                            planGraph,
                            target,
                            edge,
                            edge != null ? planGraph.getEdgeSource(edge) : null
                    );
                }

                Set<SequencePattern> edges = planGraph.outgoingEdgesOf(target);
                for(SequencePattern e : edges)
                    queue.add(Pair.of(e, planGraph.getEdgeTarget(e)));
            }
        }
    }

    // TODO - consider method ToGraphml.toGraph
    public static PlanGraph toGraph(Collection<SequencePattern> patterns){
        PlanGraph g = new PlanGraph();
        DefaultEdge de;

        // add nodes and edges
        int edgeId = 0;
        for (SequencePattern pattern : patterns){
            TaskType s = pattern.pattern.get(0);
            TaskType t = pattern.pattern.get(1);
            TaskPattern sourceNode = g.addVertex(s, null, true);
            TaskPattern targetNode = g.addVertex(t, null, true);
            g.addEdge(sourceNode, targetNode, pattern);
            g.setEdgeWeight(pattern, pattern.instances.size());
        }
        return g;
    }

    public static DefaultDirectedGraph<TaskType, SequencePattern> toPatternGraph(Collection<SequencePattern> patterns){
        DefaultDirectedGraph<TaskType, SequencePattern> g = new DefaultDirectedGraph<>(null, () -> new SequencePattern(), true );
        for (SequencePattern pattern : patterns){
            TaskType s = pattern.pattern.get(0);
            TaskType t = pattern.pattern.get(1);
            g.addVertex(s);
            g.addVertex(t);
            g.addEdge(s, t, pattern);
            g.setEdgeWeight(pattern, pattern.instances.size());
        }
        return g;
    }

    public PlanGraph planDisconnected(List<Pair<Supplier<Workpackage>, Double>> similarWPs, Set<TaskType> taskTypes, Predicate<String> ignoreHistoryEntry) {
        return plan(similarWPs, taskTypes, DISCONNECTED_ORDER);
    }
    public PlanGraph planConnected(List<Pair<Supplier<Workpackage>, Double>> similarWPs, Set<TaskType> taskTypes){
        return plan(similarWPs, taskTypes, CONNECTED_ORDER);
    }

    public PlanGraph plan(List<Pair<Supplier<Workpackage>, Double>> similarWPs, Set<TaskType> taskTypes,
                          UnacceptableOrder isUnacceptableOrder // TODO currently not used, revise use-case of parametrized order, interface and implementation

                                      ){
        PlanGraph planGraph = new PlanGraph();
        // add nodes to graph
        Map<TaskType, TaskPattern> taskPatternMap = new HashMap<>();
        for(TaskType taskType : taskTypes){
            TaskPattern taskPattern = new TaskPattern(taskType, true);
            planGraph.addVertex(taskPattern);
            taskPatternMap.put(taskType, taskPattern);
        }

        // add edges to graph
        int k = 0; // index in the similarPlans list
        Set<TaskType> remainder = new HashSet<>(taskTypes);
        Set<TaskType> plannedByThisWP = new HashSet<>();
        Set<TaskType> plannedByOtherWP = new HashSet<>();
        similarWPs.sort(Comparator.comparing((Pair<Supplier<Workpackage>, Double> p) -> p.getRight()).reversed());
        while(!remainder.isEmpty() && k < similarWPs.size()){ // revise condition
            Pair<Supplier<Workpackage>, Double> pair = similarWPs.get(k);
            k ++;
            Workpackage similarWP = pair.getKey().get();
//            if(ignoreHistoryEntry != null && ignoreHistoryEntry.test(similarPlan.getKey().getId()))
//                continue;

//            String key = similarPlan.getKey().getId();

            List<TaskExecution> taskExecutions = new ArrayList<>(similarWP.getTaskExecutions());

            if(taskExecutions.isEmpty())
                continue;

            taskExecutions.sort(Comparator.comparing(te -> te.getStart()));

            int i = 0; // index of the source node (taskType)
            TaskType source = null;
            TaskExecution sourceHistory = null;

            // find first reused TaskType
            for(; i < taskExecutions.size(); i ++){
                sourceHistory = taskExecutions.get(i);
                source = sourceHistory.getTaskType();
                if(taskTypes.contains(source)) {
                    break;
                }
            }

            // find the target node, add edge, repeat until all TCs from current history plan are reused
            for(int j = i + 1; j < taskExecutions.size(); j ++){ // j is the index of the target node (taskType)
                TaskType target = taskExecutions.get(j).getTaskType();
                if(!taskTypes.contains(target))
                    continue;
                // one of the task types should be from the reminder, i.e., do not plan edges that are already planned
                if(!(remainder.contains(source) || remainder.contains(target)))
                    continue;

                TaskExecution targetHistory = taskExecutions.get(j);

                // create edge
                SequencePattern seqpat = new SequencePattern();

                seqpat.pattern = Arrays.asList(source, target);

                List<TaskExecution> sequenceInstances = Arrays.asList(sourceHistory, targetHistory);
                seqpat.instances.add(sequenceInstances);

                TaskPattern sourceNode = taskPatternMap.get(source);
                TaskPattern targetNode = taskPatternMap.get(target);

                BiFunction<TaskPattern, TaskExecution, String> getReusedWorkPackage = (p, te) ->
                        p.getInstances() != null && !p.getInstances().isEmpty() ?
                                p.getInstances().get(0).getWorkpackage().getId() :
                                te.getWorkpackage().getId();

                if(Objects.equals(
                        getReusedWorkPackage.apply(sourceNode, sequenceInstances.get(0)),
                        getReusedWorkPackage.apply(targetNode, sequenceInstances.get(1))) &&
                   sequenceInstances.get(0).getStart().getTime() == sequenceInstances.get(1).getStart().getTime()){

                    seqpat.patternType = PatternType.EQUALITY;
                }else {
                    seqpat.patternType = (i - j == 1)?
                            PatternType.STRICT_DIRECT_ORDER :
                            PatternType.STRICT_INDIRECT_ORDER
                    ;
                }

                planGraph.addEdge(sourceNode, targetNode, seqpat);

                // add instances to graph nodes
                for(TaskExecution te: Stream.of(i, j).map(taskExecutions::get).collect(Collectors.toList())){
                    if(plannedByThisWP.contains(te.getTaskType())) // add support instances from each work package only once
                        continue;
                    TaskPattern node = taskPatternMap.get(te.getTaskType());
                    if(node.getInstances() == null){
                        node.setInstances(new ArrayList<>());
                    }
                    node.getInstances().add(te);
                }

                // finish iteration
                plannedByThisWP.add(source);
                plannedByThisWP.add(target);
                remainder.remove(source);
                remainder.remove(target);

                i = j;
                source = target;
                sourceHistory = targetHistory;
            }

            // prepare to plan for the next wp
            plannedByOtherWP.addAll(plannedByThisWP);
            plannedByThisWP = new HashSet<>();
        }

        SequencePattern.calculateSupportClasses(planGraph.edgeSet());
        return planGraph;
    }

    /**
     * Groups sessions by TaskTypes. The returned list preserves start order of the input argument {@code sessions}
     * @param sessions
     * @return
     */
    protected List<Pair<TaskType, List<Result>>> groupAsTaskPlans(List<Result> sessions){

        LinkedHashMap<TaskType, List<Result>> sessionsByTC = sessions.stream().collect(Collectors.groupingBy(r -> r.taskType, LinkedHashMap::new, Collectors.toList()));

        List<Pair<TaskType, List<Result>>> tcPlans = new ArrayList<>();
        sessionsByTC.entrySet().forEach(e -> tcPlans.add(Pair.of(e.getKey(), e.getValue())));
        return tcPlans;
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
            ret[i] = SimilarityUtils.calculateSetSimilarity(pkgs.get(i).getValue(), taskTypes);
        }
        return ret;
    }

    // TODO - revise interface, usage and implementation
    public interface UnacceptableOrder {
        boolean isOrderAcceptable(List<Result> pat, Set<TaskType> orderedTasks, DefaultDirectedGraph<TaskPattern, SequencePattern> planGraph);
    }

    public static interface NodeVisitor{
        /**
         * Visits nodes in the graph and gives context of the last node in the visited in the graph. If the edge
         * @param graph the traversed graph
         * @param node the node that is visited
         * @param edge the edge that leads to the visited node.
         * @param source Source node of edge
         */
        void visit(PlanGraph graph, TaskPattern node, SequencePattern edge, TaskPattern source);

    }

    public static final UnacceptableOrder DISCONNECTED_ORDER = (pat, orderedTasks, planGraph) -> pat.stream().map(r -> r.taskType).allMatch(tt -> !orderedTasks.contains(tt));
    public static final UnacceptableOrder CONNECTED_ORDER = (pat, orderedTasks, planGraph) -> pat.stream().map(r -> r.taskType).anyMatch(tt -> !orderedTasks.contains(tt));

}
