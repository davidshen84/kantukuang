package com.shen.xi.android.tut


import android.os.Bundle
import android.view.MenuItem
import com.google.api.client.repackaged.com.google.common.base.Strings
import com.shen.xi.android.tut.ImageSource._


object QingImageViewActivity {
  val QING_SOURCE = "qing source"
  val QING_TITLE = "qing title"
}

class QingImageViewActivity extends AbstractImageViewActivity(R.menu.qing_image_view) {

  import com.shen.xi.android.tut.AbstractImageViewActivity._
  import com.shen.xi.android.tut.QingImageViewActivity._
  import org.json4s._
  import org.json4s.native.JsonMethods._


  private var mArticleInfoList: List[(String, String)] = null
  private var mSource = Unknown
  private var mImageUrlList: List[String] = null

  TuTModule.getInjector.injectMembers(this)

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)

    val extras = getIntent.getExtras
    val item = extras.getInt(ITEM_POSITION, 0)
    val sourceString = extras.getString(QING_SOURCE)
    val jsonList = extras.getString(JSON_LIST)
    setTitle(extras.getString(QING_TITLE))

    if (!Strings.isNullOrEmpty(sourceString)) {
      try {
        mSource = ImageSource.valueOf(sourceString)
      } catch {
        case e: IllegalArgumentException => mSource = Unknown
      }
    }

    var imagePagerAdapter: ImagePagerAdapter = null
    var size = 0

    mSource match {
      case QingTag =>
        mArticleInfoList =
          for {JObject(o) <- parse(jsonList)
               JField("href", JString(href)) <- o
               JField("imageSrc", JString(imageSrc)) <- o} yield (href, imageSrc)

        size = mArticleInfoList.size

      case QingPage =>
        mImageUrlList = for {JString(s) <- parse(jsonList)} yield s
        size = mImageUrlList.size
    }

    if (size != 0) {
      imagePagerAdapter = new ImagePagerAdapter(getSupportFragmentManager, size)
      setupPager(imagePagerAdapter, item)
    } else {
      finish()
    }
  }

  override def getImageUrlByOrder(order: Int) =
    if (mSource == QingTag)
    // switch to the high-def version
      mArticleInfoList(order)._2.asInstanceOf[String].replace("mw205", "mw600")
    else
      mImageUrlList(order)

  // Handle action bar item clicks here. The action bar will
  // automatically handle clicks on the Home/Up button, so long
  // as you specify a parent activity in AndroidManifest.xml.
  override def onOptionsItemSelected(item: MenuItem) = item.getItemId match {
    case R.id.action_settings => true
    case _ => super.onOptionsItemSelected(item)
  }

}
