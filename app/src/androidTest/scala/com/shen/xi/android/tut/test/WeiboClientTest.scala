package com.shen.xi.android.tut.test

import android.app.Application
import com.google.inject.Guice
import com.shen.xi.android.tut.TuTModule
import com.shen.xi.android.tut.weibo.WeiboClient
import junit.framework.TestCase


class WeiboClientTest extends TestCase {

  import junit.framework.Assert._


  private var client: WeiboClient = null

  override def setUp() = {
    val injector = Guice.createInjector(new TuTModule(new Application()))
    client = injector.getInstance(classOf[WeiboClient])
  }

  def testGetPublicTimeline() = {
    val timeline = client.getPublicTimeline(null)

    assertNotNull(timeline)

    assertTrue(timeline.statuses.size > 0)
    assertNotNull(timeline.statuses.get(0).imageUrl)
  }

  def testGetTimeline_uid() = {
    val timeline = client.getPublicTimeline(null)

    val status = timeline.statuses.get(0)
    assertNotNull(status.uid)
    assertFalse(0L == status.uid.longValue())
  }

}
