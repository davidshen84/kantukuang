package com.shen.xi.android.tut

import android.app.Application


class MyApplication extends Application {

  override def onCreate() = {
    super.onCreate()

    // initialize Guice injector
    TuTModule.initialize(this)
  }
}
