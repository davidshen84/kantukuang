package com.xi.android.kantukuang.weibo;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WeiboTimelineAsyncTaskLoader extends AsyncTaskLoader<List<String>> {
    private static final String TAG = WeiboTimelineAsyncTaskLoader.class.getName();
    private final String mTag;
    private WeiboClient mWeiboClient;
    private String mSinceId = null;

    public WeiboTimelineAsyncTaskLoader(Context context, WeiboClient weiboClient,
                                        String tag) {
        super(context);

        mWeiboClient = weiboClient;
        mTag = tag;
        setUpdateThrottle(10000);
    }

    @Override
    protected void onStartLoading() {
        if (takeContentChanged() || mSinceId == null) {
            forceLoad();
        }
    }

    @Override
    public List<String> loadInBackground() {

        WeiboTimeline weiboTimeline = null;
        List<String> stringList = null;

        if (isAbandoned()) {
            mSinceId = null;
            return null;
        }

        Log.v(TAG, String.format("start loading weibo timeline: %s", mTag));

        try {
            if (mTag.equalsIgnoreCase("public")) {
                weiboTimeline = mWeiboClient.getPublicTimeline(mSinceId);
            } else if (mTag.equalsIgnoreCase("home")) {
                weiboTimeline = mWeiboClient.getHomeTimeline(mSinceId);
            } else {
                Log.d(TAG, String.format("i don't know how to load %s", mTag));
            }

            if (weiboTimeline != null && weiboTimeline.statuses.size() > 0) {
                stringList = new ArrayList<String>();
                Log.v(TAG, String.format("received %d statuses.", weiboTimeline.statuses.size()));
                if (!weiboTimeline.statuses.get(0).id.equalsIgnoreCase(mSinceId)) {
                    mSinceId = weiboTimeline.statuses.get(0).id;
                }

                for (WeiboStatus status : weiboTimeline.statuses) {
                    if (status.getImageUrl() != null) {
                        stringList.add(status.getImageUrl());
                    } else if (status.thumbnailUrl != null) {
                        stringList.add(status.thumbnailUrl);
                    }
                }
            }
        } catch (WeiboTimelineException ignored) {
            Log.v(TAG, "there's an error when loading timeline");
        }

        return stringList;
    }
}
