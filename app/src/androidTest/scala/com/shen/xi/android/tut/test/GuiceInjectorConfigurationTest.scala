package com.shen.xi.android.tut.test

import java.util

import android.app.Application
import com.google.api.client.http.HttpRequestFactory
import com.google.inject.name.{Named, Names}
import com.google.inject.{Guice, Inject, Injector, Key}
import com.shen.xi.android.tut.TuTModule
import com.shen.xi.android.tut.weibo.WeiboClient
import junit.framework.TestCase


object GuiceInjectorConfigurationTest {

  private class WeiboScope @Inject()(@Named("scope") scope: util.Collection[String]) {

    def getScope = scope

  }

}

class GuiceInjectorConfigurationTest extends TestCase {

  import com.shen.xi.android.tut.test.GuiceInjectorConfigurationTest.WeiboScope
  import junit.framework.Assert._


  private var injector: Injector = null

  @throws[Exception]
  override def setUp() = injector = Guice.createInjector(new TuTModule(new Application()))

  def testInjectWeiboClient() = {
    val client = injector.getInstance(classOf[WeiboClient])

    assertNotNull(client)
    assertTrue(client.isInstanceOf[WeiboClient])
  }

  def testInjectWeiboScope() = {
    val o = injector.getInstance(classOf[WeiboScope])

    assertNotNull(o)
    assertFalse(o.getScope.isEmpty)
  }

  def testInjectQingRequestFactory() = {
    val instance = injector.getInstance(Key.get(classOf[HttpRequestFactory], Names.named("qing request factory")))

    assertNotNull(instance)
  }

}
