package com.ethossoftworks.ethos.StateSaver;

import java.util.HashMap;

public class StateSaver {
    private static final String FILE_SUFFIX = "_SaveState";
    private static HashMap<String, StateDataMap> sDataMapContainer = new HashMap<>();

    public static <T> void save(T target) {
        try {
            StateHandler<T> stateHandler = (StateHandler<T>) Class.forName(target.getClass().getCanonicalName() + FILE_SUFFIX).newInstance();
            StateDataMap dataMap = new StateDataMap();
            stateHandler.saveState(target, dataMap);
            sDataMapContainer.put(stateHandler.getClass().getCanonicalName(), dataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static <T> void restore(T target) {
        try {
            StateHandler<T> stateHandler = (StateHandler<T>) Class.forName(target.getClass().getCanonicalName() + FILE_SUFFIX).newInstance();
            String canonicalName = stateHandler.getClass().getCanonicalName();
            if (!sDataMapContainer.containsKey(canonicalName)) {
                return;
            }
            stateHandler.restoreState(target, sDataMapContainer.get(canonicalName));
            sDataMapContainer.remove(canonicalName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}