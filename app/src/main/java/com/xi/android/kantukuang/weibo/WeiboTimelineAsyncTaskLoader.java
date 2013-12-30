package com.xi.android.kantukuang.weibo;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeiboTimelineAsyncTaskLoader extends AsyncTaskLoader<List<String>> {
    private static final String TAG = WeiboTimelineAsyncTaskLoader.class.getName();
    private final String mTag;
    private WeiboClient mWeiboClient;
    private String mSinceId = null;
    private int newDataCount;
    private boolean hasError;

    public WeiboTimelineAsyncTaskLoader(Context context, WeiboClient weiboClient,
                                        String tag) {
        super(context);

        mWeiboClient = weiboClient;
        mTag = tag;
        setUpdateThrottle(10000);
    }

    @Override
    protected void onReset() {
        newDataCount = 0;
        mSinceId = null;
        hasError=false;
    }

    @Override
    protected void onStartLoading() {
        if (mSinceId == null) {
            forceLoad();
        }
    }

    @Override
    public List<String> loadInBackground() {

        WeiboTimeline weiboTimeline = null;
        List<String> stringList;

        if (isAbandoned()) {
            reset();
            return null;
        }

        Log.v(TAG, String.format("start loading weibo timeline: %s", mTag));

        // make sure do not return null ref.
        stringList = new ArrayList<String>();
        try {
            if (mTag.equalsIgnoreCase("public")) {
                weiboTimeline = mWeiboClient.getPublicTimeline(mSinceId);
            } else if (mTag.equalsIgnoreCase("home")) {
                weiboTimeline = mWeiboClient.getHomeTimeline(mSinceId);
            } else {
                Log.d(TAG, String.format("i don't know how to load %s", mTag));
            }

            if (weiboTimeline != null && weiboTimeline.statuses.size() > 0) {
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

                newDataCount = stringList.size();
            }
        } catch (WeiboTimelineException ignored) {
            Log.v(TAG, "there's an error when loading timeline");
            hasError = true;
        }

        return stringList;
    }

    public int takeNewDataCount() {
        int result = newDataCount;
        newDataCount = 0;

        return result;
    }

    public boolean takeHasError() {
        boolean result = hasError;
        hasError = false;

        return result;
    }
}
