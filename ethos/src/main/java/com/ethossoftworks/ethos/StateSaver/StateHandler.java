package com.ethossoftworks.ethos.StateSaver;


public interface StateHandler<T> {
    void saveState(T target, StateDataMap dataMap);
    void restoreState(T target, StateDataMap dataMap);
}