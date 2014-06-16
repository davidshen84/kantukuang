package com.shen.xi.android.tut

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, FileNotFoundException, FileOutputStream, IOException}

import android.app.{Activity, WallpaperManager}
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.{FragmentManager, FragmentPagerAdapter}
import android.support.v4.view.{MenuItemCompat, ViewPager}
import android.support.v7.app.{ActionBar, ActionBarActivity}
import android.support.v7.widget.ShareActionProvider
import android.util.Log
import android.view.{Menu, MenuItem, View}
import android.widget.Toast
import com.google.android.gms.ads.{AdRequest, AdView}
import com.google.api.client.json.JsonFactory
import com.google.inject.Inject
import com.nostra13.universalimageloader.cache.disc.DiskCache
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import com.nostra13.universalimageloader.utils.DiskCacheUtils
import com.squareup.otto.Bus
import com.viewpagerindicator.UnderlinePageIndicator


object AbstractImageViewActivity {
  val ITEM_POSITION = "item position"
  val JSON_LIST = "json list"
  val TAG = classOf[AbstractImageViewActivity].getName
  val TestDevice: String = "32668407B2DA8E501AA7066FD541EB61"

  /**
   * A [[com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener]]
   * that set the image as wallpaper
   */
  private class WallpaperSaver(activity: Activity) extends SimpleImageLoadingListener {

    val mWallpaperManager: WallpaperManager = WallpaperManager.getInstance(activity)

    override def onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap): Unit = {
      val outputStream = new ByteArrayOutputStream()
      loadedImage.compress(Bitmap.CompressFormat.PNG, 0, outputStream)
      try {
        mWallpaperManager.setStream(new ByteArrayInputStream(outputStream.toByteArray))
        Toast.makeText(activity, R.string.message_info_success, Toast.LENGTH_SHORT).show()
      } catch {
        case e: IOException => e.printStackTrace()
      }
    }
  }


  /**
   * A [[com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener]]
   * that saves the image to external storage
   */
  private class ImageSaver(activity: Activity) extends SimpleImageLoadingListener {

    override def onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) = saveImage(imageUri, loadedImage)

    private def saveImage(imageUri: String, loadedImage: Bitmap) = getImageDirectory match {
      case Some(d) => try {
        val filename: String = Integer.toHexString(imageUri.hashCode())
        val file = new File(d, s"$filename.png")
        val stream = new FileOutputStream(file)
        loadedImage.compress(Bitmap.CompressFormat.PNG, 100, stream)
        Toast.makeText(activity, R.string.message_info_success, Toast.LENGTH_SHORT).show()
      } catch {
        case e: FileNotFoundException => e.printStackTrace()
      }
      case None =>
        Toast.makeText(activity, R.string.message_error_save_image, Toast.LENGTH_LONG).show()
    }

    private def getImageDirectory: Option[File] = {
      import android.os.Environment.{DIRECTORY_PICTURES, getExternalStoragePublicDirectory => espd}

      val tutDir = new File(espd(DIRECTORY_PICTURES), "TuT")

      if (!tutDir.isDirectory) {
        if (!tutDir.delete()) {
          Log.w(TAG, s"cannot remove ${tutDir.toString}")
          return None
        }
        if (!tutDir.mkdir()) {
          Log.w(TAG, s"cannot create ${tutDir.toString}")
          return None
        }
      }

      Some(tutDir)
    }
  }

}

abstract class AbstractImageViewActivity(menuId: Int) extends ActionBarActivity {

  import com.shen.xi.android.tut.AbstractImageViewActivity._

  private var mImageSaver: ImageSaver = null
  private var mWallpaperSaver: WallpaperSaver = null
  @Inject
  protected var mBus: Bus = null
  @Inject
  protected var mJsonFactory: JsonFactory = null

  /**
   * The [[android.support.v4.view.ViewPager]] that will host the section contents.
   */
  private var mViewPager: ViewPager = null
  private val mMenuId = menuId
  private var mActionProvider: ShareActionProvider = null
  @Inject
  private var mDiscCache: DiskCache = null
  @Inject
  private var mImageLoader: ImageLoader = null

  TuTModule.getInjector.injectMembers(this)


  private def setUpActionBar(): Unit = {
    val actionBar = getSupportActionBar
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD)
    actionBar.setDisplayHomeAsUpEnabled(true)
    actionBar.setHomeButtonEnabled(true)
  }

  protected def setupPager(pagerAdapter: FragmentPagerAdapter, item: Int): Unit = {
    // Set up the ViewPager with the sections adapter.
    mViewPager = findViewById(R.id.pager).asInstanceOf[ViewPager]
    mViewPager.setAdapter(pagerAdapter)
    mViewPager.setCurrentItem(item)

    // set up pager indicator
    val indicator = findViewById(R.id.indicator).asInstanceOf[UnderlinePageIndicator]
    indicator.setViewPager(mViewPager)
    indicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      override def onPageSelected(position: Int): Unit = {
        super.onPageSelected(position)
        doEasyShare(position)
      }
    })
  }

  protected def getCurrentItem = mViewPager.getCurrentItem

  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_image_view)
    setUpActionBar()

    mWallpaperSaver = new WallpaperSaver(this)
    mImageSaver = new ImageSaver(this)

    // set up ads
    val adRequest = new AdRequest.Builder()

    if (BuildConfig.DEBUG)
      adRequest.addTestDevice(TestDevice)
    findViewById(R.id.adView).asInstanceOf[AdView].loadAd(adRequest.build())
  }

  override protected def onResume(): Unit = {
    super.onResume()

    mBus.register(this)
  }

  override protected def onPause(): Unit = {
    super.onPause()

    mBus.unregister(this)
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(mMenuId, menu)
    setupShareActionProvider(menu)
    // initialize easy share on load
    doEasyShare(getCurrentItem)

    true
  }

  override def onOptionsItemSelected(item: MenuItem) = item.getItemId match {
    case R.id.action_save_image =>
      mImageLoader.loadImage(getImageUrlByOrder(getCurrentItem), mImageSaver)
      true
    case R.id.action_set_wallpaper =>
      mImageLoader.loadImage(getImageUrlByOrder(getCurrentItem), mWallpaperSaver)
      true
    case _ => super.onOptionsItemSelected(item)
  }

  /**
   * Delegate to derivative class to implement how to retrieve image url
   */
  protected[tut] def getImageUrlByOrder(order: Int): String

  private def doEasyShare(itemOrder: Int) = {
    val imageUrl = getImageUrlByOrder(itemOrder)
    // this logic assume the user loaded the image first
    // so a copy can be found from the disk cache
    val imageFile = DiskCacheUtils.findInCache(imageUrl, mDiscCache)
    if (imageFile != null && imageFile.exists())
      mActionProvider.setShareIntent(new Intent() {
        setAction(Intent.ACTION_SEND)
        putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile))
        setType("image/*")
      })
  }

  private def setupShareActionProvider(menu: Menu) = menu.findItem(R.id.action_share) match {
    case i: MenuItem => mActionProvider = MenuItemCompat.getActionProvider(i).asInstanceOf[ShareActionProvider]
    case null => throw new IllegalStateException()
  }

  /**
   * A [[android.support.v4.app.FragmentPagerAdapter]] that returns a fragment corresponding to
   * one of the sections/tabs/pages.
   */
  class ImagePagerAdapter(fm: FragmentManager, pageCount: Int) extends FragmentPagerAdapter(fm) {

    private val mPageCount: Int = pageCount

    // getItem is called to instantiate the fragment for the given page.
    override def getItem(position: Int) = ImageViewFragment.newInstance(position)

    override def getCount = mPageCount

    def getPageTitle(position: Integer) = String.format(getString(R.string.format_info_page_order), position)

  }

}
