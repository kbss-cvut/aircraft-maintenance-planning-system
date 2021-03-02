package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import org.apache.commons.collections4.PredicateUtils;
import org.checkerframework.checker.units.qual.K;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class Matrix {

    protected List<int[]>[][] m;

    public Matrix() {
    }

    public Matrix(int d) {
        this(d,d);
    }
    public Matrix(int w, int h) {
        initMatrix(w, h);
    }

    public final void initMatrix(int w, int h){
        m = new List[h][];
        for(int i = 0; i < h; i ++){
            m[i] = new List[w];
            Arrays.fill(m[i], null);
        }
    }

    public void addEdgesFromSequences(int[][] sequences, boolean adjacent){
        for(int i = 0; i < sequences.length; i ++) {
            int[] s = sequences[i];
        }
    }


    public void addEdgesFromSequence(int[] sequence, int sequenceId, EdgeFilter filter){
        for (int j1 = 0; j1 < sequence.length - 1; j1++) {
            for (int j2 = j1 + 1; j2 < sequence.length; j2++) {
                List<int[]> edges = m[sequence[j1]][sequence[j2]];
//                int to = adjacent ? j1 + 2 : sequence.length;
                if(edges != null && filter.test(sequenceId, j1, j2)) {
                    int[] edge = new int[]{sequenceId, j1, j2};
                    edges.add(edge);
                }
            }
        }
    }



    public List<int[]>[][] getM() {
        return m;
    }

    public void setM(List<int[]>[][] m) {
        this.m = m;
    }

    public static interface EdgeFilter {
        boolean test(int sequenceIndex, int from, int to);

        default EdgeFilter or(EdgeFilter other){
            return (i, from, to) -> this.test(i, from, to) || other.test(i, from, to);
        }
        default EdgeFilter and(EdgeFilter other){
            return (i, from, to) -> this.test(i, from, to) && other.test(i, from, to);
        }
        default EdgeFilter negate(){
            return (i, from, to) -> !this.test(i, from, to);
        }
    }

//    public static EdgeFilter adjacent = (i, from, to) -> Math
}
