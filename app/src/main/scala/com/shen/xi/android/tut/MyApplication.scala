package com.shen.xi.android.tut

import android.app.Application
import com.google.inject.Inject


class MyApplication extends Application {

  @Inject
  private var weiboClientManager: WeiboClientManager = null

  override def onCreate() = {
    super.onCreate()

    // initialize Guice injector
    TuTModule.initialize(this)
    TuTModule.getInjector.injectMembers(this)
  }
}
