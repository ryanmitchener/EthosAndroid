package com.ethossoftworks.ethos;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ethossoftworks.annotations.SaveState;
import com.ethossoftworks.ethos.StateSaver.StateSaver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import butterknife.ButterKnife;


public class EthosActivity extends AppCompatActivity {
    @SaveState public int testVarParent1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutAnnotation());

        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            StateSaver.restore(this);
//            Icepick.restoreInstanceState(this, savedInstanceState);
        } else {
            testVarParent1 = 5;
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        StateSaver.save(this);
//        Icepick.saveInstanceState(this, outState);
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