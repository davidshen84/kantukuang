package com.shen.xi.android.tut


import java.util.{List => JList}

import android.os.Bundle
import android.view.MenuItem
import com.google.common.base.Strings
import com.shen.xi.android.tut.sinablog.ArticleInfo


object QingImageViewActivity {
  val QING_SOURCE = "qing source"
  val QING_TITLE = "qing title"
}

class QingImageViewActivity extends AbstractImageViewActivity(R.menu.qing_image_view) {

  import com.shen.xi.android.tut.AbstractImageViewActivity._
  import com.shen.xi.android.tut.QingImageViewActivity._

  private var mArticleInfoList: JList[ArticleInfo] = null
  private var mSource: ImageSource = Unknown
  private var mImageUrlList: JList[String] = null

  TuTModule.getInjector.injectMembers(this)

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)

    val extras = getIntent.getExtras
    val item = extras.getInt(ITEM_POSITION, 0)
    val sourceString = extras.getString(QING_SOURCE)
    val jsonList = extras.getString(JSON_LIST)
    val jsonParser = mJsonFactory.createJsonParser(jsonList)
    var size = 0
    setTitle(extras.getString(QING_TITLE))

    if (!Strings.isNullOrEmpty(sourceString)) {
      try {
        mSource = ImageSource.fromName(sourceString)
      } catch {
        case e: IllegalArgumentException => mSource = Unknown
      }
    }



    mSource match {
      case QingTag =>
        mArticleInfoList = jsonParser.parseArray[ArticleInfo](classOf[JList[ArticleInfo]], classOf[ArticleInfo]).asInstanceOf[JList[ArticleInfo]]
        size = mArticleInfoList.size

      case QingPage =>
        mImageUrlList = jsonParser.parseArray[String](classOf[JList[String]], classOf[String]).asInstanceOf[JList[String]]
        size = mImageUrlList.size
    }

    if (size != 0) {
      val imagePagerAdapter = new ImagePagerAdapter(getSupportFragmentManager, size)
      setupPager(imagePagerAdapter, item)
    } else {
      finish()
    }
  }

  override def getImageUrlByOrder(order: Int) = mSource match {
    // switch to the high-def version
    case QingTag => mArticleInfoList.get(order).href.replace("mw205", "mw600")
    case QingPage => mImageUrlList.get(order)
  }

  // Handle action bar item clicks here. The action bar will
  // automatically handle clicks on the Home/Up button, so long
  // as you specify a parent activity in AndroidManifest.xml.
  override def onOptionsItemSelected(item: MenuItem) = item.getItemId match {
    case R.id.action_settings => true
    case _ => super.onOptionsItemSelected(item)
  }

}
