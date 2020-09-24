package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.ExtractData;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SequencePattern {
    public List<List<Result>> instances = new ArrayList<>();
    public Function<Result, String> elMap;
    public List<String> pattern;

    public SequencePattern() {

    }

    public SequencePattern(Function<Result, String> elMap, Stream<Result> sequence) {
        this.elMap = elMap;
        // construct patter
        pattern = calculatePattern(sequence);
        add(sequence.collect(Collectors.toList()));
    }

    public SequencePattern(Function<Result, String> elMap, Result ... sequence) {
        this.elMap = elMap;
        // construct patter
        pattern = calculatePattern(sequence);
        add(sequence);
    }

    public SequencePattern(Function<Result, String> elMap, List<Result> sequence) {
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


    protected Stream<String> calculatePatternImpl(Stream<Result> sequence){
        return sequence.map(r -> elMap.apply(r));
    }

    public List<String> calculatePattern(Stream<Result> sequence){
        return calculatePatternImpl(sequence).collect(Collectors.toList());
    }

    public List<String> calculatePattern(Result ... sequence){
        return calculatePattern(Stream.of(sequence));
    }

    public List<String> calculatePattern(List<Result> sequence){
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
        Iterator<String> iter =  pattern.iterator();
        Stream<String> p = calculatePatternImpl(sequence);
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
        return String.join(";", pattern);
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
}
