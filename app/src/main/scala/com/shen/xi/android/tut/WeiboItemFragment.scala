package com.shen.xi.android.tut

import java.util.{ArrayList => JArrayList, List => JList}

import android.app.Activity
import android.content.{Context, Intent}
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{AbsListView, AdapterView, ArrayAdapter, ImageView, ListAdapter, TextView, Toast}
import com.google.android.gms.ads.{AdRequest, AdView}
import com.google.inject.Inject
import com.google.inject.name.Named
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import com.nostra13.universalimageloader.core.{DisplayImageOptions, ImageLoader}
import com.shen.xi.android.tut.event.SectionAttachEvent
import com.shen.xi.android.tut.util.{AdapterModifier, MySimpleImageLoadingListener}
import com.shen.xi.android.tut.weibo.{WeiboClient, WeiboStatus}
import com.squareup.otto.Bus
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


object WeiboItemFragment {
  private val ARG_TAG = "tag"
  private val TAG = classOf[WeiboItemFragment].getName
  private val ARG_ID = "id"
  private val TestDevice: String = "32668407B2DA8E501AA7066FD541EB61"

  def newInstance(tag: String) = {
    val fragment = new WeiboItemFragment()
    fragment.setRetainInstance(true)

    val args = new Bundle()
    args.putString(ARG_TAG, tag)
    fragment.setArguments(args)

    fragment
  }
}

class WeiboItemFragment extends Fragment with AdapterView.OnItemClickListener with OnRefreshListener {

  import com.shen.xi.android.tut.AbstractImageViewActivity.JSON_LIST
  import com.shen.xi.android.tut.WeiboItemFragment._
  import org.json4s.JsonDSL._
  import org.json4s.native.JsonMethods._

  private val mSectionAttachEvent = new SectionAttachEvent()
  private val mWeiboStatuses = new JArrayList[WeiboStatus]()

  /**
   * The fragment's ListView/GridView.
   */
  private var mListView: AbsListView = null
  @Inject
  private var mWeiboClient: WeiboClient = null
  private var mWeiboItemViewArrayAdapter: WeiboItemArrayAdapter = null
  private var mEmptyView: View = null
  private var mPullToRefreshLayout: PullToRefreshLayout = null
  private var mMainActivity: MainActivity = null
  private var mLastId: String = null
  @Inject
  private var mBus: Bus = null
  private var mSectionName: String = null

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */

  TuTModule.getInjector.injectMembers(this)

  override def onAttach(activity: Activity) = {
    super.onAttach(activity)

    mMainActivity = activity.asInstanceOf[MainActivity]
  }

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)

    val mArguments = getArguments
    if (mArguments != null) {
      mSectionName = mArguments.getString(ARG_TAG)
    }

    mSectionAttachEvent.sectionName = mSectionName
    mSectionAttachEvent.source = Weibo
    mBus.post(mSectionAttachEvent)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    val view = inflater.inflate(R.layout.fragment_item, container, false)

    // Set the adapter
    mPullToRefreshLayout = view.findViewById(R.id.ptr_layout).asInstanceOf[PullToRefreshLayout]
    mListView = view.findViewById(android.R.id.list).asInstanceOf[AbsListView]
    mEmptyView = view.findViewById(android.R.id.empty)
    // set up data adapter
    mWeiboItemViewArrayAdapter = new WeiboItemArrayAdapter(mMainActivity, mWeiboStatuses)
    mListView.asInstanceOf[AdapterView[ListAdapter]].setAdapter(mWeiboItemViewArrayAdapter)

    // update empty view
    if (mWeiboStatuses.size() > 0) {
      setEmptyText(null)
    }

    mListView.setOnItemClickListener(this)

    // set up ads
    val adView = view.findViewById(R.id.adView).asInstanceOf[AdView]
    val adRequest = new AdRequest.Builder()

    if (BuildConfig.DEBUG)
      adRequest.addTestDevice(TestDevice)
    adView.loadAd(adRequest.build())

    view
  }

  override def onStart() = {
    super.onStart()

    // set up pull to refresh widget
    ActionBarPullToRefresh
      .from(mMainActivity)
      .theseChildrenArePullable(mListView)
      .listener(this)
      .setup(mPullToRefreshLayout)
  }

  override def onResume() = {
    super.onResume()

    mBus.register(this)
    // trigger refresh
    if (mWeiboStatuses.size() == 0)
      onRefreshStarted(null)

  }

  override def onDetach() = {
    // clean ref. to activity
    mMainActivity = null
    mBus.unregister(this)

    super.onDetach()
  }

  def setEmptyText(emptyText: CharSequence) = {
    if (emptyText == null) {
      mEmptyView.setVisibility(View.GONE)
      mListView.setVisibility(View.VISIBLE)
    } else {
      mEmptyView.asInstanceOf[TextView].setText(emptyText)
      mEmptyView.setVisibility(View.VISIBLE)
      mListView.setVisibility(View.INVISIBLE)
    }
  }

  override def onRefreshStarted(view: View) = {
    if (!mPullToRefreshLayout.isRefreshing)
      mPullToRefreshLayout.setRefreshing(true)

    Future {

      val timeline = mWeiboClient.getHomeTimeline(mLastId)
      if (timeline != null && timeline.statuses.size > 0) {
        (timeline.statuses filter (_.getImageUrl != null)).distinct
      } else null

    } onComplete {

      case Success(statuses) =>

        if (statuses == null || statuses.size == 0) {
          getActivity.runOnUiThread(new Runnable {
            override def run(): Unit =
              Toast.makeText(mMainActivity, R.string.message_info_no_update, Toast.LENGTH_SHORT).show()
          })
        } else {
          mLastId = statuses(0).id
          getActivity.runOnUiThread(new Runnable {
            override def run(): Unit = {
              val message = getResources.getString(R.string.format_info_new_data, int2Integer(statuses.size))
              Toast.makeText(mMainActivity, message, Toast.LENGTH_SHORT).show()

              mWeiboItemViewArrayAdapter += asJavaCollection(statuses)
            }
          })
        }

        refreshComplete()

      case Failure(e) =>
        e.printStackTrace()
        refreshComplete()
    }

    def refreshComplete() = getActivity.runOnUiThread(new Runnable {
      override def run(): Unit = {
        setEmptyText(
          if (mWeiboStatuses.size() == 0)
            getResources.getString(R.string.message_info_empty_list)
          else null)
        mPullToRefreshLayout.setRefreshComplete()
      }
    })

  }

  def getStatuses = mWeiboStatuses.asInstanceOf[JArrayList[WeiboStatus]]

  override def onItemClick(adapterView: AdapterView[_], view: View, i: Int, l: Long): Unit = {
    var jsonList = "[]"
    var picUrls = mWeiboStatuses.get(i).picUrls

    if (picUrls == null || picUrls.size() == 0) {
      picUrls = mWeiboStatuses.get(i).repostedStatus.picUrls
    }

    if (picUrls != null && picUrls.size() > 0) {
      val strings = (picUrls map (t => t.thumbnail_pic.replace("thumbnail", "large"))).toList
      jsonList = compact(render(strings))
    } else {
      val strings = (mWeiboStatuses map (s => s.getImageUrl)).toList
      jsonList = compact(render(strings))
    }

    val intent = new Intent(getActivity, classOf[WeiboImageViewActivity])
    intent.putExtra(JSON_LIST, jsonList)
    startActivity(intent)
  }

  private class WeiboItemArrayAdapter(context: Context, override val list: JList[WeiboStatus])
    extends ArrayAdapter[WeiboStatus](context, R.layout.item_image, list)
    with AdapterModifier[WeiboStatus] {

    @Inject
    @Named("low resolution")
    private var displayImageOptions: DisplayImageOptions = null
    @Inject
    private var mInflater: LayoutInflater = null
    @Inject
    private var mImageLoader: ImageLoader = null
    private var mListener: SimpleImageLoadingListener = null

    TuTModule.getInjector.injectMembers(this)

    override def getView(position: Int, convertView: View, container: ViewGroup) = {
      var newView: View = null
      if (convertView == null) {
        newView = mInflater.inflate(R.layout.item_image, container, false)
      } else {
        convertView.asInstanceOf[ImageView].setImageBitmap(null)
        newView = convertView
      }

      if (mListener == null) {
        val maxWidth = WeiboItemFragment.this.getView.getWidth
        val maxHeight = getResources.getDimensionPixelSize(R.dimen.item_image_height)
        mListener = new MySimpleImageLoadingListener(maxWidth, maxHeight)
      }

      mImageLoader.displayImage(getItem(position).getImageUrl, newView.asInstanceOf[ImageView],
        displayImageOptions, mListener)

      newView
    }
  }

}
