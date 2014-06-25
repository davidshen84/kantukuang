package com.shen.xi.android.tut


import android.os.Bundle
import android.view.MenuItem
import com.google.inject.Inject
import com.shen.xi.android.tut.weibo.WeiboClient

object WeiboImageViewActivity {
  private val TAG = classOf[WeiboImageViewActivity].getName
}

class WeiboImageViewActivity extends AbstractImageViewActivity(R.menu.weibo_image_view) {

  import com.shen.xi.android.tut.AbstractImageViewActivity._
  import org.json4s._
  import org.json4s.native.JsonMethods._


  private var mStatusList: List[String] = null
  @Inject
  private var mWeiboClient: WeiboClient = null

  TuTModule.getInjector.injectMembers(this)

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)

    // Create the adapter that will return a fragment for each of the three
    // primary sections of the activity.
    val intent = getIntent
    val item = intent.getIntExtra(ITEM_POSITION, 0)
    val jsonList = intent.getStringExtra(JSON_LIST)

    mStatusList = for (JString(i) <- parse(jsonList)) yield i
    if (mStatusList.size == 0) {
      finish()
    } else {
      /** The 'android.support.v4.view.PagerAdapter' that will provide
        * fragments for each of the sections. We use a
        * 'FragmentPagerAdapter' derivative, which will keep every
        * loaded fragment in memory. If this becomes too memory intensive, it
        * may be best to switch to a
        * 'android.support.v4.app.FragmentStatePagerAdapter'.
        */
      val pagerAdapter = new ImagePagerAdapter(getSupportFragmentManager, mStatusList.size)
      setupPager(pagerAdapter, item)
    }
  }

  override def getImageUrlByOrder(order: Int) = mStatusList(order)

  // Handle action bar item clicks here. The action bar will
  // automatically handle clicks on the Home/Up button, so long
  // as you specify a parent activity in AndroidManifest.xml.
  override def onOptionsItemSelected(item: MenuItem) = item.getItemId match {
    case R.id.menu_settings => true

    case android.R.id.home =>
      this.finish()
      true

    case _ => super.onOptionsItemSelected(item)
  }
}
