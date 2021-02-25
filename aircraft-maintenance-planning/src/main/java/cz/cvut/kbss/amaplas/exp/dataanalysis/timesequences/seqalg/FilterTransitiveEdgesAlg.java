package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.seqalg;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.ExtractData;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.graphalgs.FeedbackEdgeSetAlg;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.graphalgs.FeedbackEdgeSetResult;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.SequencePattern;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FilterTransitiveEdgesAlg {

    private static final Logger LOG = LoggerFactory.getLogger(FilterTransitiveEdgesAlg.class);

    public List<SequencePattern> inputSequencePatterns;
    public List<SequencePattern> filteredSequencePatterns;
    public FeedbackEdgeSetResult<TaskType, SequencePattern> feedbackEdgeSet;

    public FilterTransitiveEdgesAlg() {
    }

    public FilterTransitiveEdgesAlg(List<SequencePattern> inputSequencePatterns, List<SequencePattern> filteredSequencePatterns, FeedbackEdgeSetResult<TaskType, SequencePattern> feedbackEdgeSet) {
        this.inputSequencePatterns = inputSequencePatterns;
        this.filteredSequencePatterns = filteredSequencePatterns;
        this.feedbackEdgeSet = feedbackEdgeSet;
    }

    public static FilterTransitiveEdgesAlg filterTransitiveEdges(List<SequencePattern> patterns){
        // assuming patterns are of length 2
//        DirectedGra
        patterns = filterTransitiveEdgesInTheSameSupportExtension(patterns);
//        return patterns;
        DefaultDirectedGraph<TaskType, SequencePattern> g = ExtractData.asGraph(patterns);


        // make the graph acyclic
//        Set<SequencePattern> breakingEdges = findMinimalStrongConnectiveEdges(g);
        FeedbackEdgeSetResult<TaskType, SequencePattern> feedbackEdgeSetResult = new FeedbackEdgeSetAlg().execute(g);

        //remove all edges from all cycles
        feedbackEdgeSetResult.getCycles().stream()
                .flatMap(cg -> cg.getEdgeList().stream())
                .forEach(g::removeEdge);
        // remove just the feedback edges
//        for(SequencePattern sp : feedbackEdgeSetResult.getEdgeSet()){
//            g.removeEdge(sp);
//        }


        System.out.println(String.format("computing transitive reduction of graph with %d nodes and %d edges", g.vertexSet().size(), g.edgeSet().size()));

        DefaultDirectedGraph<String, SequencePattern> g1 = (DefaultDirectedGraph<String, SequencePattern>)g.clone();
        TransitiveReduction.INSTANCE.reduce(g);
        // return feedback edges in the graph
//        for(SequencePattern sp : feedbackEdgeSetResult.getEdgeSet()){
//            g.addEdge(sp.pattern.get(0), sp.pattern.get(1), sp);
//        }
        // TODO make sure that all edges which belong to the cycles are present in the graph
        for(GraphPath<TaskType, SequencePattern> gp : feedbackEdgeSetResult.getCycles()){
            for(SequencePattern sp : gp.getEdgeList()){
                if(!g.containsEdge(sp)){
                    g.addEdge(sp.pattern.get(0), sp.pattern.get(1), sp);
                }
            }
        }
//        checkTransitiveReduction(g1, g);


        System.out.println(String.format("done computing transitive reduction. Graph now has %d nodes and %d edges", g.vertexSet().size(), g.edgeSet().size()));
        return new FilterTransitiveEdgesAlg(patterns, new ArrayList<>(g.edgeSet()), feedbackEdgeSetResult);
    }

    /**
     * remove transitive edges in sub graphs induced by the equivalence classes of support extensions.
     * @param patterns
     * @return
     */
    public static List<SequencePattern> filterTransitiveEdgesInTheSameSupportExtension(List<SequencePattern> patterns){
        List<Set<SequencePattern>> eqClasses = SequencePattern.supportEquivalenceGraphs(patterns);
        List<SequencePattern> ret = new ArrayList<>();
        for(Set<SequencePattern> cls : eqClasses) {
            DefaultDirectedGraph<TaskType, SequencePattern> g = ExtractData.asGraph(cls);
//            DefaultDirectedGraph<String, SequencePattern> g1 = (DefaultDirectedGraph<String, SequencePattern>)g.clone();
            TransitiveReduction.INSTANCE.reduce(g);
            ret.addAll(g.edgeSet());
        }
        LOG.debug("reduced {} edges to {} in filterTransitiveEdgesInTheSameSupportExtension", patterns.size(), ret.size());
        return ret;
    }
}

