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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.xi.android.kantukuang.sinablog.ArticleInfo;
import com.xi.android.kantukuang.sinablog.QingClient;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class QingItemFragment extends Fragment implements AbsListView.OnItemClickListener, OnRefreshListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TAG = "Qing Tag";
    private final List<ArticleInfo> articleInfoList = new ArrayList<ArticleInfo>();
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
    private QingClient mQingClient;
    private PullToRefreshLayout mPullToRefreshLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public QingItemFragment() {
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

        mQingClient = QingClient.createForTag(mQingTag);
    }

    private void asyncLoad() {
        new AsyncTask<String, String, List<ArticleInfo>>() {
            @Override
            protected List<ArticleInfo> doInBackground(String... strings) {
                mQingClient.load();

                return mQingClient.hasLoaded() ? mQingClient.getArticleInfoList() : null;
            }

            @Override
            protected void onPostExecute(List<ArticleInfo> articleInfos) {
                if (articleInfos != null && articleInfos.size() > 0) {
                    articleInfoList.addAll(0, articleInfos);
                    mAdapter.notifyDataSetChanged();
                    if (mPullToRefreshLayout.isRefreshing())
                        mPullToRefreshLayout.setRefreshComplete();
                } else {
                    Toast.makeText(getActivity(), "no more", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qingitemfragment, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
        mAdapter = new ArticleInfoArrayAdapter(getActivity(), articleInfoList);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // TODO: display waite message
        asyncLoad();

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

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
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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
// mBus.post(RefreshQingEvent)
        asyncLoad();
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
            super(context, R.layout.fragment_qingitemfragment, list);

            KanTuKuangModule.getInjector().injectMembers(this);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_image, parent, false);
            }

            mImageLoader.displayImage(getItem(position).imageSrc,
                                      (ImageView) convertView,
                                      displayImageOptions);

            return convertView;
        }
    }
}
