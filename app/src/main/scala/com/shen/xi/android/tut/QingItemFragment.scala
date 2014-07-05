package com.shen.xi.android.tut

import java.util.{ArrayList => JArrayList, List => JList}

import android.content.{Context, Intent}
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget._
import com.google.android.gms.ads.{AdRequest, AdView}
import com.google.api.client.http.HttpRequest
import com.google.inject.Inject
import com.google.inject.name.Named
import com.nostra13.universalimageloader.core.{DisplayImageOptions, ImageLoader}
import com.shen.xi.android.tut.event.SectionAttachEvent
import com.shen.xi.android.tut.sinablog.{ArticleInfo, QingPageDriver, QingTagDriver}
import com.shen.xi.android.tut.util.{AdapterModifier, MySimpleImageLoadingListener}
import com.squareup.otto.Bus
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener
import uk.co.senab.actionbarpulltorefresh.library.{ActionBarPullToRefresh, PullToRefreshLayout}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


object QingItemFragment {
  val ARG_TAG = "Qing Tag"
  val TAG = classOf[QingItemFragment].getName
  val ARG_PARSE_PAGE = "Parse Page"
  val ARG_SECTION = "section name displayed as title"

  def newInstance(section: String, tag: String, parsePage: Boolean) = {
    val fragment = new QingItemFragment()
    val args = new Bundle()

    args.putString(ARG_SECTION, section)
    args.putString(ARG_TAG, tag)
    args.putBoolean(ARG_PARSE_PAGE, parsePage)
    fragment.setArguments(args)

    fragment
  }
}

class QingItemFragment extends Fragment with AdapterView.OnItemClickListener with OnRefreshListener {

  import com.shen.xi.android.tut.AbstractImageViewActivity.{ITEM_POSITION, JSON_LIST, TestDevice}
  import com.shen.xi.android.tut.QingImageViewActivity.{QING_SOURCE, QING_TITLE}
  import com.shen.xi.android.tut.QingItemFragment._
  import org.json4s.JsonDSL._
  import org.json4s.native.JsonMethods._


  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private val mArticleInfoList = new JArrayList[ArticleInfo]()
  private val mSectionAttachEvent = new SectionAttachEvent()
  private var mQingTag: String = null
  private var mImageLoadingListener: MySimpleImageLoadingListener = null

  /**
   * The fragment's ListView/GridView.
   */
  private var mListView: AbsListView = null
  /**
   * The Adapter which will be used to populate the ListView/GridView with
   * Views.
   */
  private var mAdapter: ArticleInfoArrayAdapter = null
  @Inject
  private var mQingTagDriver: QingTagDriver = null
  private var mPullToRefreshLayout: PullToRefreshLayout = null
  @Inject
  private var mBus: Bus = null
  private var mPage = 1
  private var mPageType: ImageSource = null
  private var mEmptyView: TextView = null

  @Inject
  private var mQingPageDriver: QingPageDriver = null
  private var mTitle: String = null

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */

  TuTModule.getInjector.injectMembers(this)

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
    val arguments = getArguments

    if (arguments != null) {
      mTitle = arguments.getString(ARG_SECTION)
      mQingTag = arguments.getString(ARG_TAG)
      mPageType = if (arguments.getBoolean(ARG_PARSE_PAGE)) QingPage else QingTag
    }

    mSectionAttachEvent.sectionName = mTitle
    mSectionAttachEvent.source = mPageType
    mBus.post(mSectionAttachEvent)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {

    val view = inflater.inflate(R.layout.fragment_item, container, false)

    // Set the adapter
    mListView = view.findViewById(android.R.id.list).asInstanceOf[AbsListView]
    mPullToRefreshLayout = view.findViewById(R.id.ptr_layout).asInstanceOf[PullToRefreshLayout]
    mAdapter = new ArticleInfoArrayAdapter(getActivity, mArticleInfoList)
    mListView.asInstanceOf[AdapterView[ListAdapter]].setAdapter(mAdapter)

    // Set OnItemClickListener so we can be notified on item clicks
    mListView.setOnItemClickListener(this)

    mEmptyView = view.findViewById(android.R.id.empty).asInstanceOf[TextView]

    // set up ads
    val adView = view.findViewById(R.id.adView).asInstanceOf[AdView]
    val adRequest = new AdRequest.Builder()
    if (BuildConfig.DEBUG)
      adRequest.addTestDevice(TestDevice)

    adView.loadAd(adRequest.build())

    // initialize the image loading listener
    mImageLoadingListener = new MySimpleImageLoadingListener(
      container.getMeasuredWidth,
      getResources.getDimensionPixelSize(R.dimen.item_image_height), null)

    view
  }

  override def onStart() = {
    super.onStart()

    // set up pull to refresh widget
    ActionBarPullToRefresh
      .from(getActivity)
      .theseChildrenArePullable(mListView)
      .listener(this)
      .setup(mPullToRefreshLayout)
  }

  override def onResume() = {
    super.onResume()

    if (mAdapter.getCount == 0) {
      mPullToRefreshLayout.setRefreshing(true)
      val httpRequest = mQingTagDriver.buildTagRequest(mQingTag, mPage)
      asyncLoad(httpRequest)
    }
  }

  override def onPause() = {
    super.onPause()
    if (mPullToRefreshLayout.isRefreshing)
      mPullToRefreshLayout.setRefreshComplete()
  }

  override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) = {

    // do not post when loading
    if (!mPullToRefreshLayout.isRefreshing) {
      mPageType match {
        case QingTag =>
          val list = (for (a <- mArticleInfoList) yield ("href" -> a.href) ~ ("imageSrc" -> a.imageSrc)).toList

          startActivity(new Intent(getActivity, classOf[QingImageViewActivity]) {
            putExtra(AbstractImageViewActivity.JSON_LIST, compact(render(list)))
            putExtra(QingImageViewActivity.QING_TITLE, mTitle)
            putExtra(ITEM_POSITION, position)
            putExtra(QING_SOURCE, QingTag.toString)
          })

        case QingPage => loadQingPage(position)
      }
    }

  }

  def loadQingPage(position: Int) = {
    mPullToRefreshLayout.setRefreshing(true)

    Future {

      if (mQingPageDriver.load(mArticleInfoList.get(position).href))
        mQingPageDriver.getImageUrlList
      else null

    } onComplete {

      case Success(strings) =>
        if (strings != null && strings.size() > 0) {
          startActivity(new Intent(getActivity, classOf[QingImageViewActivity]) {
            putExtra(ITEM_POSITION, 0)
            putExtra(JSON_LIST, compact(render(strings.toList)))
            putExtra(QING_TITLE, mTitle)
            putExtra(QING_SOURCE, QingPage.toString)
          })
        } else {
          Log.w(TAG, "no images")
          getActivity.runOnUiThread(new Runnable {
            override def run(): Unit = Toast.makeText(getActivity, R.string.no_image, Toast.LENGTH_SHORT).show()
          })
        }

        onComplete()

      case Failure(e) =>
        e.printStackTrace()
        onComplete()

    }

    def onComplete() = getActivity.runOnUiThread(new Runnable {
      override def run(): Unit = mPullToRefreshLayout.setRefreshComplete()
    })
  }

  /**
   * The default content for this Fragment has a TextView that is shown when
   * the list is empty. If you would like to change the text, call this method
   * to supply the text it should use.
   */
  def setEmptyText(emptyText: CharSequence) = {
    if (emptyText == null) {
      mEmptyView.setVisibility(View.GONE)
    } else {
      mEmptyView.setVisibility(View.VISIBLE)
      mEmptyView.setText(emptyText)
    }
  }

  override def onRefreshStarted(view: View) = {
    if (!mQingTagDriver.isLast) {
      val httpRequest = mQingTagDriver.buildTagRequest(mQingTag, mPage)
      asyncLoad(httpRequest)
    }
  }

  private def asyncLoad(httpRequest: HttpRequest) = {

    Future {
      if (mQingTagDriver.load(httpRequest))
        mPage += 1

      if (mQingTagDriver.hasLoaded)
        mQingTagDriver.getArticleInfoList
      else null
    } onComplete {

      case Success(articleInfoList) => getActivity.runOnUiThread(new Runnable {
        override def run(): Unit = {
          if (mPullToRefreshLayout.isRefreshing)
            mPullToRefreshLayout.setRefreshComplete()

          if (articleInfoList != null && articleInfoList.size > 0) {
            Log.i(TAG, s"article count = ${articleInfoList.size}")

            mAdapter += articleInfoList
            setEmptyText(null)
          } else {
            if (mAdapter.getCount == 0)
              setEmptyText(getString(R.string.message_info_empty_list))
          }
        }
      })

      case Failure(e) => e.printStackTrace()
    }
  }

  private class ArticleInfoArrayAdapter(context: Context, override val list: JList[ArticleInfo])
    extends ArrayAdapter[ArticleInfo](context, R.layout.fragment_item, list)
    with AdapterModifier[ArticleInfo] {

    @Inject
    private var mInflater: LayoutInflater = null
    @Inject
    private var mImageLoader: ImageLoader = null
    @Inject
    @Named("low resolution")
    private var displayImageOptions: DisplayImageOptions = null

    TuTModule.getInjector.injectMembers(this)

    override def getView(position: Int, convertView: View, parent: ViewGroup) = {
      var newView: View = null

      if (convertView == null) {
        newView = mInflater.inflate(R.layout.item_image, parent, false)
      } else {
        convertView.asInstanceOf[ImageView].setImageBitmap(null)
        newView = convertView
      }

      mImageLoader.displayImage(getItem(position).imageSrc, newView.asInstanceOf[ImageView],
        displayImageOptions, mImageLoadingListener)

      newView
    }
  }

}
