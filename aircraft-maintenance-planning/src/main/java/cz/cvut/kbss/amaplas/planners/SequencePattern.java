package cz.cvut.kbss.amaplas.planners;


import cz.cvut.kbss.amaplas.model.Diff;
import cz.cvut.kbss.amaplas.model.TaskExecution;
import cz.cvut.kbss.amaplas.model.TaskType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SequencePattern {
    public List<List<TaskExecution>> instances = new ArrayList<>();
    public List<TaskType> pattern;
    public PatternType patternType = PatternType.STRICT_DIRECT_ORDER;
    public int supportClassId;
    public String supportClass;
    public String supportLabel;
    public int supportClassSize;
    public Set<String> supportSet;
    public boolean direct;
    public Diff diff;
    public SequencePattern reverse;
    public int removed = 0;

    public SequencePattern() {

    }

    public SequencePattern(SequencePattern sp) {
        this.instances = sp.instances;
        this.pattern = sp.pattern;
        this.patternType = sp.patternType;
        this.supportClassId = sp.supportClassId;
        this.supportClass = sp.supportClass;
        this.supportLabel = sp.supportLabel;
        this.supportClassSize = sp.supportClassSize;
        this.supportSet = sp.supportSet;
        this.direct = sp.direct;
        this.diff = sp.diff;
    }

    public Set<String> extensionClass(){
        return instances.stream()
                .flatMap(i -> i.stream().map(e -> e.getWorkpackage().getId()))
                .collect(Collectors.toSet());
    }

    public void add(List<TaskExecution> instance){
        instances.add(instance);
    }

    public String patternId(){
        return pattern.stream().map(tt -> tt.getCode()).collect(Collectors.joining(";"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SequencePattern)) return false;
        SequencePattern that = (SequencePattern) o;
        return pattern.equals(that.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }


    /**
     * This method requires that the supportClass and supportId fields of each sequencePattern is calculated.
     * To that call <code>{@link SequencePattern#calculateSupportClasses(Collection)}</code>
     * @param patterns
     * @return
     */
    public static List<Set<SequencePattern>> supportEquivalenceGraphs(Collection<SequencePattern> patterns){

        return new ArrayList<>(
                patterns.stream()
                        .collect(Collectors.groupingBy(p -> p.supportClassId, Collectors.toSet()))
                        .values()
        );
    }


    public static void calculateSupportClasses(Collection<SequencePattern> patterns){
        Map<Set<String>, SequencePattern> extensionToClass = new HashMap<>();
        int lastClass = 0;
        for (SequencePattern p : patterns) {
            p.supportSet = p.extensionClass();
            SequencePattern proto = extensionToClass.get(p.supportSet);
            if(proto == null){ // initialize the rest of the supportClass fields for the prototype SequencePattern
                proto = p;
                proto.supportClassId = lastClass++;
                proto.supportClass = proto.supportSet.stream().sorted().collect(Collectors.joining(";\n"));
                extensionToClass.put(proto.supportSet, proto);
            }
            p.supportClassId = proto.supportClassId;
            p.supportClass = proto.supportClass;
            p.supportClassSize = p.extensionClass().size();
            p.supportLabel = p.supportClassSize + "(" + p.supportClassId + ")";
        }

    }
}
