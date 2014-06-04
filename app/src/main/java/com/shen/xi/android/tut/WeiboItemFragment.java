package com.shen.xi.android.tut;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.api.client.json.JsonFactory;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.shen.xi.android.tut.event.RefreshCompleteEvent;
import com.shen.xi.android.tut.event.RefreshWeiboEvent;
import com.shen.xi.android.tut.event.SectionAttachEvent;
import com.shen.xi.android.tut.event.SelectItemEvent;
import com.shen.xi.android.tut.util.MySimpleImageLoadingListener;
import com.shen.xi.android.tut.weibo.WeiboClient;
import com.shen.xi.android.tut.weibo.WeiboStatus;
import com.shen.xi.android.tut.weibo.WeiboThumbnail;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import static com.shen.xi.android.tut.MainActivity.ImageSource.Weibo;


public class WeiboItemFragment extends Fragment implements AbsListView.OnItemClickListener, OnRefreshListener {

    private static final String ARG_TAG = "tag";
    private static final String TAG = WeiboItemFragment.class.getName();
    private static final String ARG_ID = "id";
    private final SectionAttachEvent mSectionAttachEvent = new SectionAttachEvent();
    private final RefreshWeiboEvent mRefreshWeiboEvent = new RefreshWeiboEvent();
    private final List<WeiboStatus> mWeiboStatuses = new ArrayList<WeiboStatus>();
    private final SelectItemEvent mSelectItemEvent = new SelectItemEvent();
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
    @Inject
    private JsonFactory mJsonFactory;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WeiboItemFragment() {
        Injector injector = TuTModule.getInjector();
        injector.injectMembers(this);

        mSelectItemEvent.source = Weibo;
    }

    public static WeiboItemFragment newInstance(String tag) {
        WeiboItemFragment fragment = new WeiboItemFragment();
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
        mRefreshWeiboEvent.activity = mActivity;
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle mArguments = getArguments();
        if (mArguments != null) {
            mSectionName = mArguments.getString(ARG_TAG);
        }

        mSectionAttachEvent.sectionName = mSectionName;
        mSectionAttachEvent.source = Weibo;
        mBus.post(mSectionAttachEvent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weibo_item, container, false);

        // Set the adapter
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

        mListView.setOnItemClickListener(this);

        // set up ads
        AdView adView = (AdView) view.findViewById(R.id.adView);
        adView.loadAd(new AdRequest()
                              .addTestDevice("3D3B40496EA6FF9FDA8215AEE90C0808"));

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
        mRefreshWeiboEvent.activity = null;
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
            mWeiboStatuses.addAll(0, statusList);
            mWeiboItemViewArrayAdapter.notifyDataSetChanged();

            mLastId = event.getLastId();
        }

        setEmptyText(mWeiboStatuses.size() == 0
                             ? getResources().getString(R.string.message_info_empty_list)
                             : null);
        mPullToRefreshLayout.setRefreshComplete();
    }

    public ArrayList<WeiboStatus> getStatuses() {
        return (ArrayList<WeiboStatus>) mWeiboStatuses;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Bundle extras = new Bundle();


        String jsonList = "[]";
        List<WeiboThumbnail> picUrls = mWeiboStatuses.get(i).picUrls();
        if (picUrls == null || picUrls.size() == 0) {
            picUrls = mWeiboStatuses.get(i).repostedStatus().picUrls();
        }

        if (picUrls != null && picUrls.size() > 0) {
            try {
                Iterable<String> strings = Iterables
                        .transform(picUrls,
                                   new Function<WeiboThumbnail, String>() {
                                       @Nullable
                                       @Override
                                       public String apply(@Nullable WeiboThumbnail input) {
                                           return input.thumbnail_pic().replace("thumbnail", "large");
                                       }
                                   }
                        );
                jsonList = mJsonFactory.toString(strings);
            } catch (IOException e) {
                e.printStackTrace();
            }
            extras.putInt(AbstractImageViewActivity.ITEM_POSITION, 0);
        } else {
            try {
                Iterable<String> strings = Iterables
                        .transform(mWeiboStatuses,
                                   new Function<WeiboStatus, String>() {
                                       @Nullable
                                       @Override
                                       public String apply(
                                               @Nullable WeiboStatus input) {
                                           return input.getImageUrl();
                                       }
                                   }
                        );
                jsonList = mJsonFactory.toString(strings);
            } catch (IOException e) {
                e.printStackTrace();
            }
            extras.putInt(AbstractImageViewActivity.ITEM_POSITION, i);
        }
        extras.putString(AbstractImageViewActivity.JSON_LIST, jsonList);

        mSelectItemEvent.extras = extras;
        mSelectItemEvent.source = Weibo;
        mBus.post(mSelectItemEvent);
    }

    private class WeiboItemViewArrayAdapter extends ArrayAdapter<WeiboStatus> {
        @Inject
        @Named("low resolution")
        private DisplayImageOptions displayImageOptions;
        @Inject
        private LayoutInflater mInflater;
        @Inject
        private ImageLoader mImageLoader;
        private SimpleImageLoadingListener mListener = null;

        public WeiboItemViewArrayAdapter(Context context, List<WeiboStatus> statuses) {
            super(context, R.layout.item_image, statuses);

            TuTModule.getInjector().injectMembers(this);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = mInflater.inflate(
                        R.layout.item_image, container, false);
            } else {
                ((ImageView) convertView).setImageBitmap(null);
            }

            if (mListener == null) {
                int maxWidth = WeiboItemFragment.this.getView().getWidth();
                int maxHeight = getResources().getDimensionPixelSize(
                        R.dimen.item_image_height);
                mListener = new MySimpleImageLoadingListener(maxWidth,
                                                             maxHeight);
            }

            mImageLoader.displayImage(getItem(position).getImageUrl(), (ImageView) convertView,
                                      displayImageOptions, mListener);

            return convertView;
        }
    }
}
