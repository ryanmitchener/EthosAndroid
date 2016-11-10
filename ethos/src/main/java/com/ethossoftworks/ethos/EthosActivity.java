package com.ethossoftworks.ethos;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ethossoftworks.ethos.Util.StateSaver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


public class EthosActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layout = getLayoutAnnotation();
        if (layout != -1) {
            setContentView(layout);
        }
        if (savedInstanceState != null) {
            StateSaver.restore(this);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        StateSaver.save(this);
    }


    public int getLayoutAnnotation() {
        if (getClass().isAnnotationPresent(Layout.class)) {
            Layout layoutAnnotation = getClass().getAnnotation(Layout.class);
            return layoutAnnotation.value();
        }
        return -1;
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Layout {
        int value();
    }
}