package com.xi.android.kantukuang;

import android.app.Application;

import com.google.inject.Inject;

public final class MyApplication extends Application {

    @Inject
    private WeiboClientManager weiboClientManager;

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize Guice injector
        KanTuKuangModule.initialize(this);

        KanTuKuangModule.getInjector().injectMembers(this);
    }
}
