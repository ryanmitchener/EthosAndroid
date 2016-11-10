package com.ethossoftworks.ethos;


import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class EthosApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}