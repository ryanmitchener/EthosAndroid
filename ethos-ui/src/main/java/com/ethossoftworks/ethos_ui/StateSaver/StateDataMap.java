package com.ethossoftworks.ethos.StateSaver;


import java.util.HashMap;

public class StateDataMap extends HashMap<String, Object> {
    public <T> T removeWithType(String key) {
        return (T) remove(key);
    }
}