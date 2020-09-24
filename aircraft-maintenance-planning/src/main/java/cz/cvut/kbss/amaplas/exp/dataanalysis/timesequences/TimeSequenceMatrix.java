package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.SequencePattern;
import org.apache.jena.rdf.model.Seq;

import java.util.*;
import java.util.stream.Collectors;

public class TimeSequenceMatrix {

    protected List<String> types;
    protected Map<String, Integer> index;


    public List<SequencePattern> execute(List<List<Result>> sequences, float minSupport, boolean filterInverseEdges){
        prepareIndex(sequences);

        //translate
        int[][] ss = translate(sequences);

        // create matrix structure
        List<int[]>[][] m = new List[types.size()][];
        for(int i = 0 ; i < types.size(); i++ ) {
            m[i] = new List[types.size()];
            Arrays.fill(m[i], null);
        }
        calculateSupportMatrix(ss, m);

        // prepare return



        // count non zero edges
        int edgeCount = 0;

        for(int i = 0; i < m.length; i ++){
            for(int j = filterInverseEdges ? i + 1 : 0; j < m.length; j ++){
                if(m[i][j] != null && m[i][j].size() > minSupport)
                    edgeCount ++;
            }
        }
        List<SequencePattern> patterns = new ArrayList<>(edgeCount);

        for(int i = 0; i < m.length; i ++){
            for(int j = filterInverseEdges ? i + 1 : 0; j < m.length; j ++){
                if(m[i][j] != null && m[i][j].size() > minSupport) {
                    SequencePattern p = new SequencePattern();
                    p.pattern = Arrays.asList(types.get(i), types.get(j));
                    patterns.add(p);
                    for(int[] e: m[i][j]){
                        List<Result> s = sequences.get(e[0]);
                        p.instances.add(Arrays.asList(s.get(e[1]), s.get(e[2])));
                    }
                }
            }
        }


        return patterns;
    }


    protected void calculateSupportMatrix(int[][] ss, List<int[]>[][] m){
        for(int i = 0; i < ss.length; i ++) {
            int[] s = ss[i];
            for (int j1 = 0; j1 < s.length - 1; j1++) {
                for (int j2 = i + 1; j2 < s.length; j2++) {
//                    SequencePattern sp = new SequencePattern();
                    List<int[]> edges = m[s[j1]][s[j2]];
                    Arrays.asList();
                    if(edges == null){
                        edges = new ArrayList<>();
                        m[s[j1]][s[j2]] = edges;
                    }
                    int[] edge = new int[]{i, j1, j2};
                    edges.add(edge);
                }
            }
        }
    }

    protected int[][] translate(List<List<Result>> sequences){
        int[][] ss = new int[sequences.size()][];
        for(int i = 0; i < sequences.size(); i ++){
            ss[i] = sequences.get(i).stream().mapToInt(r -> index.get(r.type)).toArray();
        }
        return ss;
    }

    protected void prepareIndex(List<List<Result>> sequences){
        types = sequences.stream().flatMap( s -> s.stream().map(r -> r.type)).distinct().sorted().collect(Collectors.toList());
        index = new HashMap<>();

        for(int i = 0; i < types.size(); i++){
            index.put(types.get(i), i);
        }
    }


}
