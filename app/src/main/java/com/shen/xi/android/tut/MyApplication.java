package com.shen.xi.android.tut;

import android.app.Application;

import com.google.inject.Inject;

public final class MyApplication extends Application {

  @Inject
  private WeiboClientManager weiboClientManager;

  @Override
  public void onCreate() {
    super.onCreate();

    // initialize Guice injector
    TuTModule.initialize(this);

    TuTModule.getInjector().injectMembers(this);
  }
}
