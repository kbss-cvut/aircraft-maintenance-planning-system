package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.graphalgs;

import cz.cvut.kbss.amaplas.exp.common.JGraphTUtils;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class FeedbackEdgeSetAlg {
    private static final Logger LOG = LoggerFactory.getLogger(FeedbackEdgeSetAlg.class);


    /**
     * Finds feedback edge set which when removed break the input graphs strong connectivity. The algorithm does not
     * guarantee to return the smallest feedback edge set. Edges are ordered by the number of cycle in which they participate.
     * The first k edges which break all the cycles are returned.
     * @param g the input graph
     * @param <V> the type of the graph's vertices
     * @param <E> the type of the graph's edges
     * @return returns a FeedbackEdgeSetResult containing the feedback edges set and the cycles found in the graph.
     */
    public <V,E> FeedbackEdgeSetResult<V, E> execute(DefaultDirectedGraph<V, E> g){
        LOG.info("Searching of feedback edge set in graph with {} nodes and {} edges.", g.vertexSet().size(), g.edgeSet().size());
        Set<E> feedbackEdgeSet = new HashSet<>();
        Set<Set<E>> brokenCycles = new HashSet<>();
        List<GraphPath<V,E>> allCycles = new ArrayList<>();

        KosarajuStrongConnectivityInspector<V, E> alg = new KosarajuStrongConnectivityInspector<>(g);

        List<Graph<V, E>> comps = alg.getStronglyConnectedComponents();
        System.out.println();
        LOG.info("Processing {} strongly connected component.", comps.size());
        int i = 0;
        for(Graph<V, E> comp: comps ){
            i ++;
            LOG.info("Processing strongly connected component {}, with {} nodes and {} edges.", i, comp.vertexSet().size(), comp.edgeSet().size());
            if(comp.vertexSet().size() == 1)
                continue;
            // find the cycles in each component
//            Set<Set<E>> cycles = findCyclesInComponent(comp);
            List<GraphPath<V, E>> cycles = JGraphTUtils.findDirectedCycles(comp);
            allCycles.addAll(cycles);
            Set<Set<E>> cyclesToBreak = cycles.stream()
                    .map( p -> new HashSet<>(p.getEdgeList()))
                    .collect(Collectors.toSet());

            // order the edges along the number of cycles to which the edge belongs
            Map<E, Set<Set<E>>> edgeInCycles = new HashMap<>();
            cyclesToBreak.stream().forEach(c -> c.forEach(ee -> edgeInCycles.put(ee, new HashSet<>())));
            cyclesToBreak.stream().forEach(c -> c.forEach(ee -> edgeInCycles.get(ee).add(c)));
            List<E> edges = new ArrayList(edgeInCycles.keySet());
            edges.sort(Comparator.comparing(ee -> edgeInCycles.get(ee).size()).reversed());

            // remove edges according to the new order util there is no cycle for which an edge was not removed
            Set<Set<E>> compBrokenCycle = new HashSet<>();
            LOG.info("process {} cycles", cyclesToBreak.size());
            Iterator<E> iter = edges.iterator();
            while(compBrokenCycle.size() < cyclesToBreak.size()) {
                if(!iter.hasNext()) // this should never happen
                    break;

                E e = iter.next();
                // break cycles, remember broken cycles in brokenCycles and remove all
                Set<Set<E>> cs = edgeInCycles.get(e);
                if(compBrokenCycle.addAll(cs)) {
                    LOG.info("found next breaking edge, {}", e);
//                    cs.forEach(edges::removeAll);

                    feedbackEdgeSet.add(e);
                }
            }
            brokenCycles.addAll(compBrokenCycle);
        }

        return new FeedbackEdgeSetResult<V, E>(feedbackEdgeSet, brokenCycles, allCycles);
    }


    /**
     * Inefficient algorithm to find cycles in a graph.
     * Use the <code>{@link JGraphTUtils#findDirectedCycles}</code> instead.
     * @param comp
     * @param <V>
     * @param <E>
     * @return
     */
    @Deprecated
    protected <V, E> Set<Set<E>> findCyclesInComponent1(Graph<V, E> comp){
        AllDirectedPaths<V, E> pathsAlg = new AllDirectedPaths<>(comp);
        Set<E> es = new HashSet<>(comp.edgeSet());
        Set<Set<E>> cycles = new HashSet<>();
        while(!es.isEmpty()) {
            // for edge es with source and target nodes find the paths which connect target with source
            E e = es.iterator().next();
            V s = comp.getEdgeSource(e);
            V t = comp.getEdgeTarget(e);
            List<GraphPath<V, E>> paths = pathsAlg
                    .getAllPaths(t, s, true, Integer.MAX_VALUE);

            // construct cycles with found paths and edge e.
            paths.stream()
                    .filter(p -> p.getEdgeList().size() > 1)
                    .map(p -> new HashSet<>(p.getEdgeList()))
                    .forEach(c -> {
                        c.add(e);
//                            c.add(es);
                        if (es.removeAll(c))
                            cycles.add(c);
                    });
        }
        return cycles;
    }
}
