package com.shen.xi.android.tut.test

import junit.framework.TestCase
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.Json
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.testing.http.MockLowLevelHttpResponse
import com.google.common.io.CharStreams
import com.google.inject.Guice
import com.google.inject.Inject

import com.shen.xi.android.tut.sinablog.QingTagDriver
import com.shen.xi.android.tut.sinablog.TagResultUrl

import java.io.IOException
import java.io.InputStreamReader


class QingTagDriverTest extends TestCase {

  import junit.framework.Assert._

  var mDriver: QingTagDriver = null
  @Inject
  var jsonObjectParser: JsonObjectParser = null
  @Inject
  var httpTransport: TestModule.MockHttpTransport = null
  @Inject
  var mResponse: MockLowLevelHttpResponse = null
  @Inject
  var mInitializer: HttpRequestInitializer = null

  @throws[Exception]
  override def setUp() {
    super.setUp()

    val resourceAsStream = this.getClass.getClassLoader
      .getResourceAsStream("assets/tagresult.json")
    val sampleContent = CharStreams.toString(new InputStreamReader(resourceAsStream))
    resourceAsStream.close()

    val injector = Guice.createInjector(new TestModule())
    injector.injectMembers(this)

    // setup mock response
    mResponse.setContent(sampleContent)
    mResponse.setContentType(Json.MEDIA_TYPE)
    httpTransport.setResponse(mResponse)
    val requestFactory = httpTransport.createRequestFactory(mInitializer)
    mDriver = new QingTagDriver(requestFactory)
  }

  def testBuildTagRequest() {
    val test = mDriver.buildTagRequest("test", 1)
    val testUrl = test.getUrl.asInstanceOf[TagResultUrl]
    assertEquals("test", testUrl.tag)
    assertEquals(1, testUrl.page)
  }

  @throws[IOException]
  def testLoad() {
    val httpRequest = mDriver.buildTagRequest("", 1)
    assertTrue(mDriver.load(httpRequest))

    val articleInfoList = mDriver.getArticleInfoList
    assertNotNull(articleInfoList)
  }

  def testParseArticleInfo() {
    mDriver.load(mDriver.buildTagRequest("", 1))
    val articleInfoList = mDriver.getArticleInfoList

    assertEquals(10, articleInfoList.size())

    val info1 = articleInfoList.get(0)
    assertEquals("http://qing.blog.sina.com.cn/tj/8e18904032004q8g.html", info1.href)
    assertEquals("http://ww3.sinaimg.cn/mw205/8e189040jw1edkautot30j20ij0ijta2.jpg", info1.imageSrc)

    val info2 = articleInfoList.get(9)
    assertEquals("http://qing.blog.sina.com.cn/tj/5c3aa1ba32004o92.html", info2.href)
    assertEquals("http://ww4.sinaimg.cn/mw205/5c3aa1bajw1ecjapp17pej20dw0kudgo.jpg", info2.imageSrc)
  }

}
