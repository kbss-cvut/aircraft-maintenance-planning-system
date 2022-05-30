package cz.cvut.kbss.amaplas.algs;

import java.util.HashSet;
import java.util.Set;

public class SimilarityUtils {
    public static double calculateSetSimilarity(Set<?> p1, Set<?> p2){
        HashSet<?> and = new HashSet<>(p1);
        and.retainAll(p2);
        return 2d*and.size()/(p1.size() + p2.size());
    }
}
