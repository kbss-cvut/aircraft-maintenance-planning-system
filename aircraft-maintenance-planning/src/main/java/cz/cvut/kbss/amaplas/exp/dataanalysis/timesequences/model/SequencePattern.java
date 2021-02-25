package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;


import org.apache.jena.rdf.model.Seq;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SequencePattern {
    public List<List<Result>> instances = new ArrayList<>();
    public Function<Result, TaskType> elMap;
    public List<TaskType> pattern;
    public PatternType patternType = PatternType.STRICT_ORDER;
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
        this.elMap = sp.elMap;
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

    public SequencePattern(Function<Result, TaskType> elMap, Stream<Result> sequence) {
        this.elMap = elMap;
        // construct patter
        pattern = calculatePattern(sequence);
        add(sequence.collect(Collectors.toList()));
    }

    public SequencePattern(Function<Result, TaskType> elMap, Result ... sequence) {
        this.elMap = elMap;
        // construct patter
        pattern = calculatePattern(sequence);
        add(sequence);
    }

    public SequencePattern(Function<Result, TaskType> elMap, List<Result> sequence) {
        this.elMap = elMap;
        // construct patter
        pattern = calculatePattern(sequence);
        add(sequence);
    }

    protected void addFirst(Stream<Result> sequence){
        pattern = calculatePattern(sequence);
        instances.add(sequence.collect(Collectors.toList()));
    }

    protected void addFirst(Result ... sequence){
        addFirst(Stream.of(sequence));
    }

    protected void addFirst(List<Result> sequence){
        pattern = calculatePattern(sequence);
        instances.add(sequence);
    }


    protected Stream<TaskType> calculatePatternImpl(Stream<Result> sequence){
        return sequence.map(r -> elMap.apply(r));
    }

    public List<TaskType> calculatePattern(Stream<Result> sequence){
        return calculatePatternImpl(sequence).collect(Collectors.toList());
    }

    public List<TaskType> calculatePattern(Result ... sequence){
        return calculatePattern(Stream.of(sequence));
    }

    public List<TaskType> calculatePattern(List<Result> sequence){
        return calculatePattern(sequence.stream());
    }

    public boolean addIfInstance(Stream<Result>  sequence){
        if(pattern == null) {
            addFirst(sequence);
            return true;
        }
        boolean ret = isInstance(sequence);
        if(ret)
            instances.add(sequence.collect(Collectors.toList()));
        return ret;
    }

    public boolean addIfInstance(Result ... sequence){
        return addIfInstance(Arrays.asList(sequence));
    }

    public boolean addIfInstance(List<Result> sequence){
        if(pattern == null) {
            addFirst(sequence);
            return true;
        }

        boolean ret = isInstance(sequence);
        if(ret)
            instances.add(sequence);
        return ret;
    }

    public Set<String> extensionClass(){
        return instances.stream()
                .flatMap(i -> i.stream().map(e -> e.wp))
                .collect(Collectors.toSet());
    }

    public boolean startsWith(Stream<Result> sequence ){
        return compare(sequence, true);
    }

    public boolean startsWith(Result ... sequence ){
        return startsWith(Stream.of(sequence));
    }

    public boolean startsWith(List<Result> sequence ){
        return startsWith(sequence.stream());
    }

    protected boolean compare(Stream<Result> sequence, boolean ignoreLength ){
        Iterator<TaskType> iter =  pattern.iterator();
        Stream<TaskType> p = calculatePatternImpl(sequence);
        return p.allMatch(s -> iter.hasNext() && iter.next().equals(s)) && (ignoreLength || !iter.hasNext());
    }

    public boolean isInstance(Stream<Result> sequence){
        return pattern == null || compare(sequence, false);
    }

    public boolean isInstance(Result ... sequence){
        return isInstance(Stream.of(sequence));
    }

    public boolean isInstance(List<Result> sequence){
        return isInstance(sequence.stream());
    }

    public void add(List<Result> instance){
        instances.add(instance);
    }

    public void add(Result ... sequence){
        List<Result> instance = Stream.of(sequence).collect(Collectors.toList());
        add(instance);
    }

    public String patternId(){
        return pattern.stream().map(tt -> tt.type).collect(Collectors.joining(";"));
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
////        Map<SequencePattern, Set<String>> patternSupExt = new HashMap<>();
//        patterns.forEach(p -> p.supportClass = p.extensionClass());
////        patterns.forEach(p -> {
////            Set<String> supExt = new HashSet<>(p.extensionClass());
////            patternSupExt.put(p, supExt);
////        });
//        Map<Integer, Set<SequencePattern>> extensionToClass = new HashMap<>();
//        patterns.forEach(p -> {
//            Set<String> supExt = p.supportClass;
//            Set<SequencePattern> eqClass = extensionToClass.get(supExt);
//            if(eqClass == null){
//                eqClass = new HashSet<>();
//                extensionToClass.put(supExt, eqClass);
//            }
//            eqClass.add(p.getKey());
//        });
//        return new ArrayList<>(extensionToClass.values());
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
