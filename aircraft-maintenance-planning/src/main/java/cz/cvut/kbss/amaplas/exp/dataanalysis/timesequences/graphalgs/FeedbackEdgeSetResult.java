package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.graphalgs;

import org.jgrapht.GraphPath;

import java.util.List;
import java.util.Set;


public class FeedbackEdgeSetResult<V, E> {
    protected Set<E> edgeSet;
    protected Set<Set<E>> brokenCycles;
    protected List<GraphPath<V, E>> cycles;

    public FeedbackEdgeSetResult() {
    }

    public FeedbackEdgeSetResult(Set<E> es, Set<Set<E>> brokenCycles, List<GraphPath<V, E>> cycles) {
        this.edgeSet = es;
        this.cycles = cycles;
        this.brokenCycles = brokenCycles;
    }

    public Set<E> getEdgeSet() {
        return edgeSet;
    }

    public void setEdgeSet(Set<E> edgeSet) {
        this.edgeSet = edgeSet;
    }

    public Set<Set<E>> getBrokenCycles() {
        return brokenCycles;
    }

    public void setBrokenCycles(Set<Set<E>> brokenCycles) {
        this.brokenCycles = brokenCycles;
    }

    public List<GraphPath<V, E>> getCycles() {
        return cycles;
    }

    public void setCycles(List<GraphPath<V, E>> cycles) {
        this.cycles = cycles;
    }

}
