package cz.cvut.kbss.amaplas.model.base;

import cz.cvut.kbss.amaplas.model.AbstractPlan;

public interface LongInterval<T> {
    Long getStart();
    Long getEnd();

    default boolean isValid(){
        return getStart() != null && getEnd() != null;
    }

    default long getLength(){
        return getEnd() - getStart();
    }


    static LongInterval asInterval(final AbstractPlan p){
        return new LongIntervalWrapper<AbstractPlan> (){
            @Override
            public AbstractPlan getWrapped() {
                return p;
            }

            @Override
            public Long getStart() {
                return p.getStartTime().getTime();
            }

            @Override
            public Long getEnd() {
                return p.getEndTime().getTime();
            }
        };
    }
}
