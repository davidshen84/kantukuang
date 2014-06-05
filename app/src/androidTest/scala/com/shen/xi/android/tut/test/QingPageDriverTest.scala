package com.shen.xi.android.tut.test

import com.google.inject.Guice
import com.google.inject.Injector
import com.shen.xi.android.tut.sinablog.QingPageDriver

import junit.framework.TestCase

import java.io.InputStream
import java.util.List

class QingPageDriverTest extends TestCase {

  import junit.framework.Assert._

  private val mDrive = new QingPageDriver()

  @throws[Exception]
  override def setUp() {
    super.setUp()

    val resourceAsStream = this.getClass
      .getClassLoader
      .getResourceAsStream("assets/qingPage.html")
    mDrive.load(resourceAsStream)
    resourceAsStream.close()
    val injector = Guice.createInjector(new TestModule())
    injector.injectMembers(this)
  }

  @throws[Exception]
  override def tearDown() {
  }

  def testLoadPage() {
    assertNotNull(mDrive.getImageUrlList)
  }

  def testGetImageUrls() {
    val imageUrlList = mDrive.getImageUrlList
    assertEquals(5, imageUrlList.size())
    assertEquals("http://ww4.sinaimg.cn/mw600/e2830b12jw1eevfj15tf0j20tn18ggys.jpg",
      imageUrlList.get(0))
  }
}
