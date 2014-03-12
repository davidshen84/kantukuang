package com.xi.android.kantukuang;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Lists;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.xi.android.kantukuang.event.FilterStatusEvent;
import com.xi.android.kantukuang.event.RefreshCompleteEvent;
import com.xi.android.kantukuang.event.RefreshWeiboEvent;
import com.xi.android.kantukuang.event.SectionAttachEvent;
import com.xi.android.kantukuang.event.SelectItemEvent;
import com.xi.android.kantukuang.util.Util;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


public class ItemFragment extends Fragment implements OnRefreshListener {

    private static final String ARG_TAG = "tag";
    private static final int FORCE_TOP_PADDING = 256;
    private static final String TAG = ItemFragment.class.getName();
    private static final String ARG_ID = "id";
    private static final String PREF_FILTER_BLACKLIST = "filter blacklist";
    private final SectionAttachEvent mSectionAttachEvent = new SectionAttachEvent();
    private final RefreshWeiboEvent mRefreshWeiboEvent = new RefreshWeiboEvent();
    private final List<WeiboStatus> mWeiboStatuses = new ArrayList<WeiboStatus>();
    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;
    @Inject
    private WeiboClient mWeiboClient;
    private ArrayAdapter<WeiboStatus> mWeiboItemViewArrayAdapter;
    private View mEmptyView;
    private PullToRefreshLayout mPullToRefreshLayout;
    private MainActivity mActivity;
    private String mLastId;
    @Inject
    private Bus mBus;
    private String mSectionName;
    private boolean mFilterBlackList;
    @Inject
    private JsonFactory mJsonFactory;
    private int mSectionId;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
        Injector injector = KanTuKuangModule.getInjector();
        injector.injectMembers(this);
    }

    public static ItemFragment newInstance(String tag) {
        ItemFragment fragment = new ItemFragment();
        fragment.setRetainInstance(true);

        Bundle args = new Bundle();
        args.putString(ARG_TAG, tag);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (MainActivity) activity;
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(activity);
        mFilterBlackList = sp.getBoolean(PREF_FILTER_BLACKLIST, true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle mArguments = getArguments();
        if (mArguments != null) {
            mSectionName = mArguments.getString(ARG_TAG);
            mSectionId = mArguments.getInt(ARG_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        // Set the adapter
        assert view != null;
        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mEmptyView = view.findViewById(android.R.id.empty);
        // set up data adapter
        mWeiboItemViewArrayAdapter = new WeiboItemViewArrayAdapter(mActivity, mWeiboStatuses);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mWeiboItemViewArrayAdapter);

        // update empty view
        if (mWeiboStatuses.size() > 0) {
            setEmptyText(null);
        }

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private final SelectItemEvent event = new SelectItemEvent();

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                event.setPosition(position);
                mBus.post(event);
            }
        });

        mSectionAttachEvent.sectionName = mSectionName;
        mSectionAttachEvent.sectionId = mSectionId;
        mBus.post(mSectionAttachEvent);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // set up pull to refresh widget
        ActionBarPullToRefresh
                .from(mActivity)
                .theseChildrenArePullable(mListView)
                .listener(this)
                .setup(mPullToRefreshLayout);
    }

    @Override
    public void onResume() {
        super.onResume();

        mBus.register(this);
        // trigger refresh
        if (mWeiboStatuses.size() == 0) {
            mBus.post(mRefreshWeiboEvent);
            mPullToRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mPullToRefreshLayout.setRefreshing(true);
                }
            });
        }
    }

    @Override
    public void onDetach() {
        // clean ref. to activity
        mActivity = null;
        mBus.unregister(this);
        super.onDetach();
    }

    private void setEmptyText(CharSequence emptyText) {
        if (emptyText == null) {
            mEmptyView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        } else {
            ((TextView) mEmptyView).setText(emptyText);
            mEmptyView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onRefreshStarted(View view) {
        if (!mPullToRefreshLayout.isRefreshing())
            mPullToRefreshLayout.setRefreshing(true);

        mRefreshWeiboEvent.sinceId = mLastId;
        mBus.post(mRefreshWeiboEvent);
    }

    @Subscribe
    public void refreshComplete(RefreshCompleteEvent event) {
        Collection<WeiboStatus> statusList = event.getStatusList();
        if (statusList != null && statusList.size() > 0) {
            if (mFilterBlackList)
                statusList = filterBlacklist(statusList);
            mWeiboStatuses.addAll(0, statusList);
            mWeiboItemViewArrayAdapter.notifyDataSetChanged();

            mLastId = event.getLastId();
        }

        setEmptyText(mWeiboStatuses.size() == 0
                             ? getResources().getString(R.string.message_info_empty_list)
                             : null);
        mPullToRefreshLayout.setRefreshComplete();
    }

    @Subscribe
    public void filterStatus(FilterStatusEvent event) {
        if (event.shouldFilter) {
            List<WeiboStatus> statuses = Lists.newArrayList(mWeiboStatuses);
            Collection<WeiboStatus> filteredStatuses = filterBlacklist(statuses);
            mWeiboStatuses.clear();
            mWeiboStatuses.addAll(filteredStatuses);
            mWeiboItemViewArrayAdapter.notifyDataSetChanged();
        }
    }

    private Collection<WeiboStatus> filterBlacklist(Collection<WeiboStatus> statusList) {
        Collection<Long> blacklist = getBlacklist();
        if (blacklist != null && blacklist.size() > 0) {
            Predicate<WeiboStatus> blacklistPredictor =
                    Util.createBlacklistPredictor(blacklist);
            statusList = Collections2.filter(statusList, blacklistPredictor);
        }

        return statusList;
    }

    private Collection<Long> getBlacklist() {
        BlacklistSQLiteOpenHelper sqLiteOpenHelper =
                new BlacklistSQLiteOpenHelper(mActivity);

        Collection<Long> blockedUIDs = sqLiteOpenHelper.getBlockedUIDs();
        sqLiteOpenHelper.close();

        return blockedUIDs;
    }

    public ArrayList<WeiboStatus> getStatuses() {
        return (ArrayList<WeiboStatus>) mWeiboStatuses;
    }

    private class WeiboItemViewArrayAdapter extends ArrayAdapter<WeiboStatus> {
        @Inject
        @Named("low resolution")
        private DisplayImageOptions displayImageOptions;
        @Inject
        private LayoutInflater mInflater;
        @Inject
        private ImageLoader mImageLoader;

        public WeiboItemViewArrayAdapter(Context context, List<WeiboStatus> statuses) {
            super(context, R.layout.item_image, statuses);

            KanTuKuangModule.getInjector().injectMembers(this);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = mInflater.inflate(
                        R.layout.item_image, container, false);
            } else {
                ((ImageView) convertView).setImageBitmap(null);
            }

            mImageLoader.displayImage(getItem(position).getImageUrl(), (ImageView) convertView,
                                      displayImageOptions,
                                      new SimpleImageLoadingListener() {
                                          @Override
                                          public void onLoadingComplete(String imageUri,
                                                                        View view,
                                                                        Bitmap loadedImage) {
                                              if (loadedImage.getHeight() / 2 < FORCE_TOP_PADDING) {
                                                  view.setPadding(0, 0, 0, 0);
                                              } else {
                                                  view.setPadding(0, FORCE_TOP_PADDING, 0,
                                                                  0);
                                              }
                                          }
                                      }
            );

            return convertView;
        }
    }
}
