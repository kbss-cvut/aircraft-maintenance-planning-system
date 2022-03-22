package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.SequencePattern;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class TimeSequenceMatrix extends Index<TaskType> {

    protected List<int[]>[][] m;
    /**
     * Extract type sequence patterns of length 2 using a matrix based algorithm.
     * Implementation notes:
     * @param sequences - a list of ordered sequences of elements with a type
     * @param minSupport - the minimal support of the type sequence
     * @param adjacent - extract only adjacent task types
     * @param filterEdges - ignore sequences of types if the input also contains the inverse sequence.
     * @param filter - the filter used to filter which type pairs are relevant, e.g. types pairs are relevant if they
     *               start in the same day.
     * @return
     */
    public List<SequencePattern> execute(List<List<Result>> sequences, float minSupport,
                                         boolean adjacent,
                                         BiConsumer<Integer, Integer> filterEdges,
                                         BiPredicate<Result, Result> filter){
        prepareIndex(sequences);

        //translate the sequences of elements to sequences of element type codes (types are translated to integers)
        int[][] ss = translate(sequences);

        // create matrix structure - cells in the matrix are lists which contain the instance of the sequence pattern
        m = new List[types.size()][];
        for(int i = 0 ; i < types.size(); i++ ) {
            m[i] = new List[types.size()];
            Arrays.fill(m[i], null);
        }

        initSupportMatrix(ss, m, sequences, adjacent, filter);// optimization, does not change outcome of algorithm. is it good optimization?
        calculateSupportMatrix(ss, m, sequences, adjacent, filter);

        // filter bidirectional patterns - set values to null
        if(filterEdges != null) {
            for (int i = 0; i < m.length; i++) {
                for (int j = i + 1; j < m.length; j++) {
//                    if (m[i][j] != null && m[j][i] != null) {// both directions have support
//                        m[i][j] = m[j][i] = null;
//                    }
                    filterEdges.accept(i, j);
                }
            }
        }

        // count edges with support greater than 0
        int edgeCount = 0;
        for(int i = 0; i < m.length; i ++){
            for(int j = 0; j < m.length && j != i; j ++){
                if(m[i][j] != null && m[i][j].size() >= minSupport)
                    edgeCount ++;
            }
        }

        // translate the found patterns back into  and prepare the return value
        List<SequencePattern> patterns = new ArrayList<>(edgeCount);
        for(int i = 0; i < m.length - 1; i ++){
            for(int j = i + 1; j < m.length; j ++){
//            for(int j = filterInverseEdges ? i + 1 : 0; j < m.length; j ++){
                if(m[i][j] != null && m[i][j].size() >= minSupport) {
                    SequencePattern p1 = create(i, j, sequences);
                    patterns.add(p1);
                    // check the reverse edge
                    if(m[j][i] != null && m[j][i].size() >= minSupport) {
                        SequencePattern p2 = create(j, i, sequences);
                        patterns.add(p2);
                        p1.reverse = p2;
                        p2.reverse = p1;
                    }
//                    new SequencePattern();
//                    p.pattern = Arrays.asList(types.get(i), types.get(j));
//                    patterns.add(p);
//                    p.direct = Math.abs(i - j) == 1;
//                    for(int[] e: m[i][j]){
//                        List<Result> s = sequences.get(e[0]);
//                        p.instances.add(Arrays.asList(s.get(e[1]), s.get(e[2])));
//                    }
                }
            }
        }

        return patterns;
    }

    protected SequencePattern create(int i, int j, List<List<Result>> sequences){
        SequencePattern p = new SequencePattern();
        p.pattern = Arrays.asList(types.get(i), types.get(j));
        p.direct = Math.abs(i - j) == 1;
        for(int[] e: m[i][j]){
            List<Result> s = sequences.get(e[0]);
            p.instances.add(Arrays.asList(s.get(e[1]), s.get(e[2])));
        }
        return p;
    }

    public void removeBiEdges(int i, int j){
        if (m[i][j] != null && m[j][i] != null) {// both directions have support
            m[i][j] = m[j][i] = null;
        }
    }

    public void preferBiEdgeWithBiggerSupport(int i, int j){
        if (m[i][j] != null && m[j][i] != null) {// both directions have support
            if(m[i][j].size() > m[j][i].size()) {
                m[j][i] = null;
            }else if(m[j][i].size() > m[i][j].size()) {
                m[i][j] = null;
            }
        }
    }

    protected void initSupportMatrix(int[][] ss, List<int[]>[][] m, List<List<Result>> sequences,
                                     boolean adjacent,
                                     BiPredicate<Result, Result> filter){


        for(int i = 0; i < ss.length; i ++) {
            int[] s = ss[i];
            List<Result> seq = sequences.get(i);
            for (int j1 = 0; j1 < s.length - 1; j1++) {
                Result r1 = seq.get(j1);
                int to = adjacent ? j1 + 2 : s.length;
                for (int j2 = j1 + 1; j2 < to; j2++) {
//                    SequencePattern sp = new SequencePattern();
                    if(filter.test(r1, seq.get(j2))){
                        m[s[j1]][s[j2]] = new ArrayList<>();
                    }
                }
            }
        }
    }


    protected void calculateSupportMatrix(int[][] ss, List<int[]>[][] m, List<List<Result>> sequences,
                                          boolean adjacent,
                                          BiPredicate<Result, Result> filter){
        for(int i = 0; i < ss.length; i ++) {
            int[] s = ss[i];
            List<Result> seq = sequences.get(i);
            for (int j1 = 0; j1 < s.length - 1; j1++) {
                Result r1 = seq.get(j1);
                for (int j2 = j1 + 1; j2 < s.length; j2++) {
                    List<int[]> edges = m[s[j1]][s[j2]];
                    int to = adjacent ? j1 + 2 : s.length;
                    if(edges != null && filter.test(r1, seq.get(j2))) {
                        int[] edge = new int[]{i, j1, j2};
                        edges.add(edge);
                    }
                }
            }
        }
    }

    protected int[][] translate(List<List<Result>> sequences){
        int[][] ss = new int[sequences.size()][];
        for(int i = 0; i < sequences.size(); i ++){
            List<Result> seq = sequences.get(i);
            ss[i] = new int[seq.size()];
            for(int j = 0; j < seq.size(); j ++) {
                ss[i][j] = index.get(seq.get(j).taskType);
            }
        }
        return ss;
    }

    protected void prepareIndex(List<List<Result>> sequences){
        prepareIndex(sequences, r -> r.taskType, Comparator.comparing(t -> t.code));
    }
}
