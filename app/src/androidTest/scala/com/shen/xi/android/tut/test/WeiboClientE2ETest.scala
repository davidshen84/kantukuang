package com.shen.xi.android.tut.test

import android.app.Application
import com.google.inject.{Guice, Inject}
import com.shen.xi.android.tut.TuTModule
import com.shen.xi.android.tut.weibo.WeiboClient
import junit.framework.TestCase


/**
 * This test case is only used to check if the access token still works.
 */
class WeiboClientE2ETest extends TestCase {

  import junit.framework.Assert._


  @Inject
  var mClient: WeiboClient = null

  @throws[Exception]
  override def setUp() = {
    val injector = Guice.createInjector(new TuTModule(new Application()))
    injector.injectMembers(this)
  }

  def testGetPublicTimeline() {
    val timeline = mClient.getHomeTimeline(null)

    assertNotNull(timeline)
    assertTrue(timeline.statuses.size > 0)
  }

}
