package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.base;

public interface LongIntervalWrapper<T> extends LongInterval<T> {
    T getWrapped();
}
