package com.ethossoftworks.ethos.Util;


import android.app.Activity;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

public class StateSaver {
    public static void save(Activity activity) {
        try {
            Field[] publicFields = activity.getClass().getFields();
            for (Field field : publicFields) {
                Annotation annotation = field.getAnnotation(State.class);
                if (annotation != null) {
                    DataStore.set(field.getName(), field.get(activity));
                }
            }


            Field[] privateFields = activity.getClass().getDeclaredFields();
            for (Field field : privateFields) {
                Annotation annotation = field.getAnnotation(State.class);
                if (annotation != null) {
                    field.setAccessible(true);
                    DataStore.set(field.getName(), field.get(activity));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    public static void restore(Activity activity) {
        try {
            Field[] publicFields = activity.getClass().getFields();
            for (Field field : publicFields) {
                Annotation annotation = field.getAnnotation(State.class);
                if (annotation != null) {
                    field.set(activity, DataStore.retrieve(field.getName()));
                }
            }


            Field[] privateFields = activity.getClass().getDeclaredFields();
            for (Field field : privateFields) {
                Annotation annotation = field.getAnnotation(State.class);
                if (annotation != null) {
                    field.setAccessible(true);
                    field.set(activity, DataStore.retrieve(field.getName()));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface State {}
}
