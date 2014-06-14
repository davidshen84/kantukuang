package com.shen.xi.android.tut;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.JsonFactory;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shen.xi.android.tut.event.SectionAttachEvent;
import com.shen.xi.android.tut.event.SelectItemEvent;
import com.shen.xi.android.tut.sinablog.ArticleInfo;
import com.shen.xi.android.tut.sinablog.QingPageDriver;
import com.shen.xi.android.tut.sinablog.QingTagDriver;
import com.shen.xi.android.tut.util.MySimpleImageLoadingListener;
import com.squareup.otto.Bus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import static com.shen.xi.android.tut.ImageSource.QingPage;
import static com.shen.xi.android.tut.ImageSource.QingTag;


public class QingItemFragment extends Fragment implements AdapterView.OnItemClickListener, OnRefreshListener {

  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_TAG = "Qing Tag";
  private static final String TAG = QingItemFragment.class.getName();
  private static final String ARG_PARSE_PAGE = "Parse Page";
  private static final String ARG_SECTION = "section name displayed as title";
  private final List<ArticleInfo> mArticleInfoList = new ArrayList<ArticleInfo>();
  private final SelectItemEvent mSelectItemEvent = new SelectItemEvent();
  private final SectionAttachEvent mSectionAttachEvent = new SectionAttachEvent();
  private String mQingTag;
  private MySimpleImageLoadingListener mImageLoadingListener;
  /**
   * The fragment's ListView/GridView.
   */
  private AbsListView mListView;
  /**
   * The Adapter which will be used to populate the ListView/GridView with
   * Views.
   */
  private ArrayAdapter mAdapter;
  @Inject
  private QingTagDriver mQingTagDriver;
  private PullToRefreshLayout mPullToRefreshLayout;
  @Inject
  private Bus mBus;
  private int mPage = 1;
  private ImageSource mPageType;
  private TextView mEmptyView;
  @Inject
  private JsonFactory mJsonFactory;
  @Inject
  private QingPageDriver mQingPageDriver;
  private String mTitle = null;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public QingItemFragment() {
    Injector injector = TuTModule.getInjector();
    injector.injectMembers(this);

    mSelectItemEvent.source = QingTag;
  }

  public static QingItemFragment newInstance(String section, String tag, boolean parsePage) {
    QingItemFragment fragment = new QingItemFragment();
    Bundle args = new Bundle();
    args.putString(ARG_SECTION, section);
    args.putString(ARG_TAG, tag);
    args.putBoolean(ARG_PARSE_PAGE, parsePage);
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle arguments = getArguments();

    if (arguments != null) {
      mTitle = arguments.getString(ARG_SECTION);
      mQingTag = arguments.getString(ARG_TAG);
      mPageType = arguments.getBoolean(ARG_PARSE_PAGE) ? QingPage : QingTag;
    }

    mSectionAttachEvent.sectionName = mTitle;
    mSectionAttachEvent.source = mPageType;
    mBus.post(mSectionAttachEvent);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_qing_item, container, false);

    // Set the adapter
    mListView = (AbsListView) view.findViewById(android.R.id.list);
    mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
    mAdapter = new ArticleInfoArrayAdapter(getActivity(), mArticleInfoList);
    ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

    // Set OnItemClickListener so we can be notified on item clicks
    mListView.setOnItemClickListener(this);

    mEmptyView = (TextView) view.findViewById(android.R.id.empty);

    // set up ads
    AdView adView = (AdView) view.findViewById(R.id.adView);
    adView.loadAd(new AdRequest.Builder()
      .addTestDevice("3D3B40496EA6FF9FDA8215AEE90C0808")
      .build());

    // initialize the image loading listener
    mImageLoadingListener = new MySimpleImageLoadingListener(
      container.getMeasuredWidth(),
      getResources().getDimensionPixelSize(R.dimen.item_image_height), null);

    return view;
  }

  @Override
  public void onStart() {
    super.onStart();

    // set up pull to refresh widget
    ActionBarPullToRefresh
      .from(getActivity())
      .theseChildrenArePullable(mListView)
      .listener(this)
      .setup(mPullToRefreshLayout);

  }

  @Override
  public void onResume() {
    super.onResume();

    if (mAdapter.getCount() == 0) {
      mPullToRefreshLayout.setRefreshing(true);
      HttpRequest httpRequest = mQingTagDriver.buildTagRequest(mQingTag, mPage);
      asyncLoad(httpRequest);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (mPullToRefreshLayout.isRefreshing())
      mPullToRefreshLayout.setRefreshComplete();
  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    // do not post when loading
    if (!mPullToRefreshLayout.isRefreshing()) {
      switch (mPageType) {
        case QingTag:
          Bundle extras = new Bundle();
          extras.putInt(AbstractImageViewActivity.ITEM_POSITION(), position);
          extras.putString(QingImageViewActivity.QING_SOURCE, QingTag.toString());
          String jsonList = "[]";
          try {
            jsonList = mJsonFactory.toString(mArticleInfoList);
          } catch (IOException e) {
            e.printStackTrace();
          }
          extras.putString(AbstractImageViewActivity.JSON_LIST(), jsonList);
          extras.putString(QingImageViewActivity.QING_TITLE, mTitle);

          mSelectItemEvent.source = QingTag;
          mSelectItemEvent.extras = extras;
          mBus.post(mSelectItemEvent);
          break;

        case QingPage:
          loadQingPage(position);
          break;
      }
    }
  }

  private void loadQingPage(int position) {
    mPullToRefreshLayout.setRefreshing(true);
    new AsyncTask<String, Integer, List<String>>() {

      @Override
      protected List<String> doInBackground(String... strings) {
        if (mQingPageDriver.load(strings[0]))
          return mQingPageDriver.getImageUrlList();
        else
          return null;
      }

      @Override
      protected void onPostExecute(List<String> strings) {
        if (strings != null && strings.size() > 0) {
          Bundle extras = new Bundle();
          extras.putInt(AbstractImageViewActivity.ITEM_POSITION(), 0);

          String jsonList = "[]";
          try {
            jsonList = mJsonFactory.toString(strings);
          } catch (IOException e) {
            e.printStackTrace();
          }
          extras.putInt(AbstractImageViewActivity.ITEM_POSITION(), 0);
          extras.putString(AbstractImageViewActivity.JSON_LIST(), jsonList);
          extras.putString(QingImageViewActivity.QING_SOURCE,
            QingPage.toString());
          extras.putString(QingImageViewActivity.QING_TITLE, mTitle);

          mSelectItemEvent.source = QingPage;
          mSelectItemEvent.extras = extras;
          mBus.post(mSelectItemEvent);
        } else {
          Log.w(TAG, "no images");
          Toast.makeText(getActivity(), R.string.no_image,
            Toast.LENGTH_SHORT).show();
        }
        mPullToRefreshLayout.setRefreshComplete();
      }

    }.execute(mArticleInfoList.get(position).href());
  }

  /**
   * The default content for this Fragment has a TextView that is shown when
   * the list is empty. If you would like to change the text, call this method
   * to supply the text it should use.
   */
  public void setEmptyText(CharSequence emptyText) {
    if (emptyText == null) {
      mEmptyView.setVisibility(View.GONE);
    } else {
      mEmptyView.setVisibility(View.VISIBLE);
      mEmptyView.setText(emptyText);
    }
  }

  @Override
  public void onRefreshStarted(View view) {
    HttpRequest httpRequest;

    if (mQingTagDriver.isLast()) {
      return;
    } else {
      httpRequest = mQingTagDriver.buildTagRequest(mQingTag, mPage);
    }

    asyncLoad(httpRequest);
  }

  private void asyncLoad(HttpRequest httpRequest) {

    new AsyncTask<HttpRequest, String, List<ArticleInfo>>() {
      @Override
      protected List<ArticleInfo> doInBackground(HttpRequest... requests) {
        if (mQingTagDriver.load(requests[0]))
          mPage++;

        return mQingTagDriver.hasLoaded() ? mQingTagDriver.getArticleInfoList() : null;
      }

      @Override
      protected void onPostExecute(List<ArticleInfo> articleInfoList) {
        if (mPullToRefreshLayout.isRefreshing())
          mPullToRefreshLayout.setRefreshComplete();

        if (articleInfoList != null && articleInfoList.size() > 0) {
          Log.i(TAG, "article count = " + articleInfoList.size());

          QingItemFragment.this.mArticleInfoList.addAll(0, articleInfoList);
          mAdapter.notifyDataSetChanged();

          setEmptyText(null);
        } else {
          if (mAdapter.getCount() == 0)
            setEmptyText(getString(R.string.message_info_empty_list));
        }
      }
    }.execute(httpRequest);

  }

  private class ArticleInfoArrayAdapter extends ArrayAdapter<ArticleInfo> {
    @Inject
    private LayoutInflater mInflater;
    @Inject
    private ImageLoader mImageLoader;
    @Inject
    @Named("low resolution")
    private DisplayImageOptions displayImageOptions;

    public ArticleInfoArrayAdapter(Context context, List<ArticleInfo> list) {
      super(context, R.layout.fragment_qing_item, list);

      TuTModule.getInjector().injectMembers(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = mInflater.inflate(R.layout.item_image, parent, false);
      } else {
        ((ImageView) convertView).setImageBitmap(null);
      }

      mImageLoader.displayImage(getItem(position).imageSrc(), (ImageView) convertView,
        displayImageOptions, mImageLoadingListener);

      return convertView;
    }

  }

}
