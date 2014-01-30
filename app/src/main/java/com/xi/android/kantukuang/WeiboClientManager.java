package com.xi.android.kantukuang;


import android.os.AsyncTask;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.xi.android.kantukuang.event.AccessTokenCompleteEvent;
import com.xi.android.kantukuang.event.AccessTokenEvent;
import com.xi.android.kantukuang.event.RefreshStatusCompleteEvent;
import com.xi.android.kantukuang.event.RefreshStatusEvent;
import com.xi.android.kantukuang.util.Util;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboStatus;
import com.xi.android.kantukuang.weibo.WeiboTimeline;
import com.xi.android.kantukuang.weibo.WeiboTimelineException;

import java.util.Collection;
import java.util.List;

public class WeiboClientManager {

    private static final String TAG = WeiboClientManager.class.getName();
    private final WeiboClient mClient;
    private final Bus mBus;

    @Inject
    public WeiboClientManager(Bus bus, WeiboClient client) {
        mBus = bus;
        mClient = client;

        mBus.register(this);
    }

    @Subscribe
    public void refreshStatus(RefreshStatusEvent event) {

        new AsyncTask<String, Integer, List<WeiboStatus>>() {

            private final RefreshStatusCompleteEvent completeEvent = new RefreshStatusCompleteEvent();

            /**
             * Predicate the status has image url
             */


            @Override
            protected List<WeiboStatus> doInBackground(String... strings) {
                List<WeiboStatus> statusList = null;

                try {
                    WeiboTimeline timeline = null;
                    String accountId = strings[0];
                    String sinceId = strings[1];

                    if (accountId.equalsIgnoreCase("home")) {
                        timeline = mClient.getHomeTimeline(sinceId);
                    } else if (accountId.equalsIgnoreCase("public")) {
                        timeline = mClient.getPublicTimeline(sinceId);
                    } else if (accountId.equalsIgnoreCase("friends")) {
                        timeline = mClient.getFriendsTimeline(sinceId);
                    }

                    if (timeline != null && timeline.statuses.size() > 0) {
                        Collection<WeiboStatus> statusCollection =
                                Collections2.filter(timeline.statuses, Util.ImageUrlPredictor);
                        statusList = Lists.newArrayList(statusCollection);
                    }
                } catch (WeiboTimelineException e) {
                    e.printStackTrace();
                }

                return statusList;
            }

            @Override
            protected void onPostExecute(List<WeiboStatus> weiboStatuses) {
                mBus.post(completeEvent.setStatus(weiboStatuses));
            }

        }.execute(event.getAccountId(), event.getSinceId());
    }

    @Subscribe
    public void accessToken(AccessTokenEvent event) {

        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... strings) {
                return mClient.requestAccessToken(strings[0]);
            }

            @Override
            protected void onPostExecute(String s) {
                mBus.post(new AccessTokenCompleteEvent(s));
            }
        }.execute(event.code);

    }

    @Override
    protected void finalize() throws Throwable {
        mBus.unregister(this);

        super.finalize();
    }

}
