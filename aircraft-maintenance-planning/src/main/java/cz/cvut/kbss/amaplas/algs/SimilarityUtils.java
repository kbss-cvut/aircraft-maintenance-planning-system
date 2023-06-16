package cz.cvut.kbss.amaplas.algs;

import java.util.HashSet;
import java.util.Set;

public class SimilarityUtils {
    /**
     * Calculates the Jaccard Similarity between two sets. The Jaccard Similarity is defined as the size of the
     * intersection divided by the size of the union of the sets.
     * @param p1
     * @param p2
     * @return
     */
    public static double calculateSetSimilarity(Set<?> p1, Set<?> p2){
        HashSet<?> and = new HashSet<>(p1);
        and.retainAll(p2);
        return and.size()/(p1.size() + p2.size() - and.size());
    }
}
