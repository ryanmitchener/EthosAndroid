package com.ethossoftworks.ethos.Util;


import java.util.HashMap;

public class DataStore {
    private static HashMap<String, Object> sObjects = new HashMap<>();


    public static void set(String key, Object data) {
        sObjects.put(key, data);
    }


    public static <T> T get(String key) {
        return (T) sObjects.get(key);
    }


    public static <T> T retrieve(String key) {
        return (T) sObjects.remove(key);
    }


    public static void remove(String key) {
        sObjects.remove(key);
    }


    public static boolean contains(String key) {
        return sObjects.containsKey(key);
    }
}