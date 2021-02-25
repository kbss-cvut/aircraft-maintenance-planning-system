package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Index<T> {
    protected List<T> types;
    protected Map<T, Integer> index;

    public <I> void prepareIndex(List<List<I>> sequences, Function<I, T> typeFunction, Comparator<T> comp) {
        types = sequences.stream().flatMap(s -> s.stream().map(r -> typeFunction.apply(r)))
                .distinct()
                .sorted(comp)
                .collect(Collectors.toList());
        index = new HashMap<>();

        for (int i = 0; i < types.size(); i++) {
            index.put(types.get(i), i);
        }
    }
}
