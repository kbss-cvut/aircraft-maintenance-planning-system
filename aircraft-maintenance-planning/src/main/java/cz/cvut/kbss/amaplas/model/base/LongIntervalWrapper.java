package cz.cvut.kbss.amaplas.model.base;

public interface LongIntervalWrapper<T> extends LongInterval<T> {
    T getWrapped();
}
