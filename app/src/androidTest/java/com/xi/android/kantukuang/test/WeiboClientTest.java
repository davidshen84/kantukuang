package com.xi.android.kantukuang.test;

import android.app.Application;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.xi.android.kantukuang.KanTuKuangModule;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboStatus;
import com.xi.android.kantukuang.weibo.WeiboTimeline;
import com.xi.android.kantukuang.weibo.WeiboTimelineException;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.logging.Logger;

public class WeiboClientTest extends TestCase {
    private static final Logger logger = Logger.getLogger(WeiboClientTest.class.getName());
    private WeiboClient client;


    @Override
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new KanTuKuangModule(new Application()));
        client = injector.getInstance(WeiboClient.class);
        client.setAccessToken("2.00uOPaHD1JlHSDcc83013405KD6O9D");
    }

    public void testGetPublicTimeline() throws IOException, InterruptedException, WeiboTimelineException {
        WeiboTimeline timeline = client.getPublicTimeline(null);

        assertNotNull(timeline);

        assertTrue(timeline.statuses.size() > 0);
        assertNotNull(timeline.statuses.get(0).imageUrl);
    }

    public void testGetTimeline_uid() throws WeiboTimelineException {
        WeiboTimeline timeline = client.getPublicTimeline(null);

        WeiboStatus status = timeline.statuses.get(0);
        assertNotNull(status.uid);
        assertFalse(0L == status.uid);

    }

}
