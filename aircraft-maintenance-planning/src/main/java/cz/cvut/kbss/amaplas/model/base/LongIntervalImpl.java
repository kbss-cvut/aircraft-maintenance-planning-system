package cz.cvut.kbss.amaplas.model.base;


import java.util.*;

public class LongIntervalImpl implements LongInterval{
    private Long start;
    private Long end;

    public LongIntervalImpl(long start, long end) {
        this.start = start;
        this.end = end;
    }

//    public LongIntervalImpl(LongInterval i) {
//        this(i.getStart(), i.getEnd());
//    }

    public LongIntervalImpl(Date d1, Date d2){
        this(d1.getTime(), d2.getTime());
    }

    @Override
    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    @Override
    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public void setAs(LongInterval i){
        this.start = i.getStart();
        this.end = i.getEnd();
    }

//    public long duration(){
//        return this.end - this.start;
//    }

    public static LongIntervalImpl union(Collection<LongInterval> intervals){
        if(intervals == null || intervals.isEmpty())
            return null;
        long start = intervals.stream().mapToLong(i -> i.getStart()).min().getAsLong();
        long end = intervals.stream().mapToLong(i -> i.getEnd()).max().getAsLong();
        return new LongIntervalImpl(start, end);
    }

    public static long overlap(LongIntervalImpl i1, LongIntervalImpl i2){
        return Math.min(i1.end, i2.end) - Math.max(i1.start, i2.start);
    }

    public static long overlap(long i1s, long i1e, long i2s, long i2e){
        return Math.min(i1e, i2e) - Math.max(i1s, i2s);
    }

    /**
     * If intervals overlap set the boundaries of the merged interval in i1. Otherwise nothing changes
     * @param i1
     * @param i2
     * @return returns true if the intervals were overlapping and merged otherwise returns false;
     */
    public static boolean mergeOverlap(LongIntervalImpl i1, LongIntervalImpl i2){
        if(overlap(i1, i2) < 0){
           return false;
        }
        i1.start = Math.min(i1.start, i2.start);
        i2.end = Math.max(i1.end, i2.end);
        return true;
    }

    /**
     * Returns a reduced interval set that covers all of the intervals of the input interval set. The return value has
     * the property of being the smallest set of smallest non overlapping intervals that cover all the intervals of the
     * original set and each point that belongs to the new interval set is also a point of the input interval set.
     * @param intervals assumes the list is ordered by start
     * @return
     */
    public static List<LongIntervalImpl> mergeOverlaps(List<LongIntervalImpl> intervals){
        // handle extreme cases
        if(intervals == null)
            return null;
        if(intervals.isEmpty())
            return Collections.emptyList();
        if(intervals.size() == 1)
            return new ArrayList(intervals);

        // initialize
        List<LongIntervalImpl> ret = new ArrayList<>(intervals.size());
        LongIntervalImpl current = intervals.get(0);
        ret.add(current);
        // processing
        for(int i = 1; i < intervals.size(); i ++){
            boolean merged = LongIntervalImpl.mergeOverlap(current, intervals.get(i));
            if(!merged){ // condition based on the assumption
                current = intervals.get(i);
                ret.add(current);
                break;
            }
        }
        return ret;
    }
}
