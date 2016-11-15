package com.ethossoftworks.ethos.StateSaver;

//import android.app.Activity;

import java.util.HashMap;

// TODO: Allow access to superclass private fields if possible
public class StateSaver {
    private static final String FILE_SUFFIX = "_SaveState";

    private static HashMap<String, StateDataMap> sDataMap = new HashMap<>();

    public static void save(Object target) {
        try {
            StateHandler stateHandler = (StateHandler) Class.forName(target.getClass().getCanonicalName() + FILE_SUFFIX).newInstance();
            StateDataMap dataMap = new StateDataMap();
            stateHandler.saveState(target, dataMap);
            sDataMap.put(stateHandler.getClass().getCanonicalName(), dataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void restore(Object target) {
        try {
            StateHandler stateHandler = (StateHandler) Class.forName(target.getClass().getCanonicalName() + FILE_SUFFIX).newInstance();
            String canonicalName = stateHandler.getClass().getCanonicalName();
            if (!sDataMap.containsKey(canonicalName)) {
                return;
            }
            stateHandler.restoreState(target, sDataMap.get(canonicalName));
            sDataMap.remove(canonicalName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static void save(Activity activity) {
//        try {
//            Field[] publicFields = activity.getClass().getFields();
//            for (Field field : publicFields) {
//                Annotation annotation = field.getAnnotation(SaveState.class);
//                if (annotation != null) {
//                    DataStore.set(field.getName(), field.get(activity));
//                }
//            }
//
//
//            Field[] privateFields = activity.getClass().getDeclaredFields();
//            for (Field field : privateFields) {
//                Annotation annotation = field.getAnnotation(SaveState.class);
//                if (annotation != null) {
//                    field.setAccessible(true);
//                    DataStore.set(field.getName(), field.get(activity));
//                }
//            }
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//    }


//    public static void restore(Activity activity) {
//        try {
//            Field[] publicFields = activity.getClass().getFields();
//            for (Field field : publicFields) {
//                Annotation annotation = field.getAnnotation(SaveState.class);
//                if (annotation != null) {
//                    field.set(activity, DataStore.retrieve(field.getName()));
//                }
//            }
//
//
//            Field[] privateFields = activity.getClass().getDeclaredFields();
//            for (Field field : privateFields) {
//                Annotation annotation = field.getAnnotation(SaveState.class);
//                if (annotation != null) {
//                    field.setAccessible(true);
//                    field.set(activity, DataStore.retrieve(field.getName()));
//                }
//            }
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//    }
}