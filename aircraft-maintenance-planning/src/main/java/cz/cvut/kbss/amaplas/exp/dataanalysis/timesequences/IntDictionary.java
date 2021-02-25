package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;

import java.util.*;

public class IntDictionary<T> {
    protected List<T> elements;
    protected Map<T, Integer> index;

    public IntDictionary(List<T> elements) {
        this.elements = elements;
        index = new HashMap<>();
        for(int i = 0; i < elements.size(); i++){
            index.put(elements.get(i), i);
        }
    }

    public BitSet translate(Collection<T> vals){
        BitSet bs = new BitSet(elements.size());
        for(T v : vals){
            Integer i = index.get(v);
            if(i == null) {
                throw new RuntimeException(
                        String.format("attempt to translate an unknown element \"%s\"", String.valueOf(v))
                );
            }
            bs.set(i);
        }
        return bs;
    }

    public List<T> translate(BitSet bs){
        List<T> ret = new ArrayList<>(bs.cardinality());
        if(bs.size() != elements.size()) {
            throw new RuntimeException(
                    String.format(
                            "attempt to translate bitset with length %d using dictionary with %d terms",
                            bs.size(), elements.size()
                    )
            );
        }
        for(int i = 0; i < elements.size(); i ++){
            if(bs.get(i))
                ret.add(elements.get(i));
        }
        return ret;
    }

    public List<T> getElements() {
        return elements;
    }

    public void setElements(List<T> elements) {
        this.elements = elements;
    }

    public Map<T, Integer> getIndex() {
        return index;
    }

    public void setIndex(Map<T, Integer> index) {
        this.index = index;
    }
}
