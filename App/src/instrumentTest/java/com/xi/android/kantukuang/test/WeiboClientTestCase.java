package com.xi.android.kantukuang.test;

import android.test.AndroidTestCase;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.xi.android.kantukuang.KanTuKuangModule;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboTimeline;
import com.xi.android.kantukuang.weibo.WeiboTimelineException;

import java.io.IOException;

public class WeiboClientTestCase extends AndroidTestCase {
    private WeiboClient client;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Injector injector = Guice.createInjector(new KanTuKuangModule());
        client = injector.getInstance(WeiboClient.class);
        client.setAccessToken("2.00uOPaHD1JlHSDcc83013405KD6O9D");
    }

    public void testGetPublicTimeline() throws IOException, InterruptedException, WeiboTimelineException {
        WeiboTimeline timeline = client.getPublicTimeline(null);

        assertNotNull(timeline);

        assertTrue(timeline.statuses.size() > 0);
        assertNotNull(timeline.statuses.get(0).imageUrl);
    }

    public void testGetHomeTimeline() throws WeiboTimelineException {

        WeiboTimeline timeline = client.getHomeTimeline(null);

        assertNotNull(timeline);
        assertTrue(timeline.statuses.size() > 0);
        assertNotNull(timeline.statuses.get(0).getImageUrl());
    }

    public void testGetAuthorizeUrl() {
        String url = client.getAuthorizeUrl();

        assertEquals(
                "https://api.weibo.com/oauth2/authorize?client_id=3016222086&redirect_uri=kantukuang.com/&response_type=code&scope=all&display=mobile",
                url);
    }

}
