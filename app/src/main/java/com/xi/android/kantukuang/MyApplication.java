package com.xi.android.kantukuang;

import android.app.Application;

public final class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // initialize Guice injector
        KanTuKuangModule.initialize(this);
    }
}
