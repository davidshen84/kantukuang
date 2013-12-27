package com.xi.android.kantukuang.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.xi.android.kantukuang.KanTuKuangModule;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboTimeline;
import com.xi.android.kantukuang.weibo.WeiboTimelineException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class WeiboClientTest {
    private WeiboClient client;

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new KanTuKuangModule());
        client = injector.getInstance(WeiboClient.class);
        client.setAccessToken("2.00uOPaHD1JlHSDcc83013405KD6O9D");
    }

    @Test
    public void testGetPublicTimeline() throws IOException, InterruptedException, WeiboTimelineException {
        WeiboTimeline timeline = client.getPublicTimeline(null);

        assertNotNull(timeline);

        assertTrue(timeline.statuses.size() > 0);
        assertNotNull(timeline.statuses.get(0).imageUrl);
    }

    @Test
    public void testGetHomeTimeline() throws WeiboTimelineException {

        WeiboTimeline timeline = client.getHomeTimeline(null);

        assertNotNull(timeline);
        assertTrue(timeline.statuses.size() > 0);
        assertNotNull(timeline.statuses.get(0).text);
    }

    @Test
    public void testGetAuthorizeUrl() {
        String url = client.getAuthorizeUrl();

        assertEquals(
                "https://api.weibo.com/oauth2/authorize?client_id=3016222086&redirect_uri=kantukuang.com/&response_type=code&scope=all&display=mobile",
                url);
    }

}
