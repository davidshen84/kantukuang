package com.xi.android.kantukuang;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Bus;
import com.xi.android.kantukuang.event.SectionAttachEvent;
import com.xi.android.kantukuang.event.SelectItemEvent;
import com.xi.android.kantukuang.sinablog.ArticleInfo;
import com.xi.android.kantukuang.sinablog.QingTagDriver;
import com.xi.android.kantukuang.util.MySimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import static com.xi.android.kantukuang.MainActivity.SelectEventSource.Qing;

public class QingItemFragment extends Fragment implements AbsListView.OnItemClickListener, OnRefreshListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TAG = "Qing Tag";
    private static final String TAG = QingItemFragment.class.getName();
    private final List<ArticleInfo> articleInfoList = new ArrayList<ArticleInfo>();
    private final SelectItemEvent mSelectItemEvent = new SelectItemEvent();
    private String mQingTag;
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
    private final SectionAttachEvent mSectionAttachEvent = new SectionAttachEvent();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public QingItemFragment() {
        Injector injector = KanTuKuangModule.getInjector();
        injector.injectMembers(this);

        mSelectItemEvent.source = Qing;
    }

    public static QingItemFragment newInstance(String tag) {
        QingItemFragment fragment = new QingItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, tag);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mQingTag = getArguments().getString(ARG_TAG);
        }

        mSectionAttachEvent.sectionName = mQingTag;
        mBus.post(mSectionAttachEvent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qing_item, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
        mAdapter = new ArticleInfoArrayAdapter(getActivity(), articleInfoList);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        // set up ads
        AdView adView = (AdView) view.findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder()
                              .addTestDevice("3D3B40496EA6FF9FDA8215AEE90C0808")
                              .build());

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

        // TODO: display waite message

        HttpRequest httpRequest = mQingTagDriver.buildTagRequest(mQingTag, mPage);

        asyncLoad(httpRequest);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mSelectItemEvent.position = position;
        mBus.post(mSelectItemEvent);
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
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
                if (articleInfoList != null && articleInfoList.size() > 0) {
                    QingItemFragment.this.articleInfoList.addAll(0, articleInfoList);
                    mAdapter.notifyDataSetChanged();
                    if (mPullToRefreshLayout.isRefreshing())
                        mPullToRefreshLayout.setRefreshComplete();
                } else {
                    Toast.makeText(getActivity(), "no more", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(httpRequest);
    }

    public List<ArticleInfo> getArticleInfoList() {
        return mQingTagDriver.getArticleInfoList();
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

            KanTuKuangModule.getInjector().injectMembers(this);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MySimpleImageLoadingListener listener = null;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_image, parent, false);
            }

            if (listener == null) {
                int maxWidth = QingItemFragment.this.getView().getWidth();
                int maxHeight = getResources().getDimensionPixelSize(R.dimen.item_image_height);
                listener = new MySimpleImageLoadingListener(maxWidth,
                                                            maxHeight);
            }

            mImageLoader.displayImage(getItem(position).imageSrc, (ImageView) convertView,
                                      displayImageOptions, listener);

            return convertView;
        }

    }

}
