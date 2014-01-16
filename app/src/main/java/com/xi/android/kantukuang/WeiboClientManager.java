package com.xi.android.kantukuang;


import android.os.AsyncTask;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboStatus;
import com.xi.android.kantukuang.weibo.WeiboTimeline;
import com.xi.android.kantukuang.weibo.WeiboTimelineException;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

public class WeiboClientManager {

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
            private final Predicate<WeiboStatus> predicate = new Predicate<WeiboStatus>() {
                @Override
                public boolean apply(@Nullable WeiboStatus status) {
                    return status != null && status.getImageUrl() != null;
                }
            };

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
                    }

                    if (timeline != null && timeline.statuses.size() > 0) {
                        Collection<WeiboStatus> statusCollection =
                                Collections2.filter(timeline.statuses, predicate);
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

    @Override
    protected void finalize() throws Throwable {
        mBus.unregister(this);

        super.finalize();
    }


    public static class RefreshStatusEvent {
        private String mAccountId;
        private String mSinceId;

        public String getAccountId() {
            return mAccountId;
        }

        public void setAccountId(String accountId) {
            mAccountId = accountId;
        }

        public String getSinceId() {
            return mSinceId;
        }

        public void setSinceId(String sinceId) {
            mSinceId = sinceId;
        }
    }

    public class RefreshStatusCompleteEvent {
        private List<WeiboStatus> mStatusList;

        public List<WeiboStatus> getStatus() {
            return mStatusList;
        }

        public RefreshStatusCompleteEvent setStatus(List<WeiboStatus> status) {
            this.mStatusList = status;

            return this;
        }
    }
}
