package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class LongInterval {
    public long s;
    public long e;



    public LongInterval(long s, long e) {
        this.s = s;
        this.e = e;
    }

    public LongInterval(LongInterval i) {
        this(i.s, i.e);
    }

    public LongInterval(Date d1, Date d2){
        this(d1.getTime(), d2.getTime());
    }

    public void set(LongInterval i){
        this.s = i.s;
        this.e = i.e;
    }

    public long duration(){
        return this.e - this.s;
    }

    public static long overlap(LongInterval i1, LongInterval i2){
        return Math.min(i1.e, i2.e) - Math.max(i1.s, i2.s);
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
    public static boolean mergeOverlap(LongInterval i1, LongInterval i2){
        if(overlap(i1, i2) < 0){
           return false;
        }
        i1.s = Math.min(i1.s, i2.s);
        i2.e = Math.max(i1.e, i2.e);
        return true;
    }

    /**
     * Returns a reduced interval set that covers all of the intervals of the input interval set. The return value has
     * the property of being the smallest set of smallest non overlapping intervals that cover all the intervals of the
     * original set and each point that belongs to the new interval set is also a point of the input interval set.
     * @param intervals assumes the list is ordered by start
     * @return
     */
    public static List<LongInterval> mergeOverlaps(List<LongInterval> intervals){
        // handle extreme cases
        if(intervals == null)
            return null;
        if(intervals.isEmpty())
            return Collections.emptyList();
        if(intervals.size() == 1)
            return new ArrayList(intervals);

        // initialize
        List<LongInterval> ret = new ArrayList<>(intervals.size());
        LongInterval current = intervals.get(0);
        ret.add(current);
        // processing
        for(int i = 1; i < intervals.size(); i ++){
            boolean merged = LongInterval.mergeOverlap(current, intervals.get(i));
            if(!merged){ // condition based on the assumption
                current = intervals.get(i);
                ret.add(current);
                break;
            }
        }
        return ret;
    }
}
