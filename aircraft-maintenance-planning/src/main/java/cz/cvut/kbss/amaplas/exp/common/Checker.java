package cz.cvut.kbss.amaplas.exp.common;

import java.util.Collection;
import java.util.Map;

public class Checker {
    public static String nll(Object o){
        return o == null ? "x" : "v";
    }

    public static String diffRef(String id, Object o1, Object o2, String prefix){
        return String.format("%s\"%s\": %s -> %s", prefix,id,nll(o1), nll(o2));
    }

    public static String diffSize(Map<?, ?> c1, Map<?, ?> c2, String prefix){
        return String.format("%s: %d -> %d ; %d", prefix, c1.size(), c2.size(), c2.size() - c1.size());
    }

    public static String diffSize(Collection<?> c1, Collection<?> c2, String prefix){
        return String.format("%s: %d -> %d ; %d", prefix, c1.size(), c2.size(), c2.size() - c1.size());
    }
}
