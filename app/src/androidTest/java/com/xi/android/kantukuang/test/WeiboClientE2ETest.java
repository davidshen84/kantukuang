package com.xi.android.kantukuang.test;

import android.app.Application;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.xi.android.kantukuang.KanTuKuangModule;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboTimeline;
import com.xi.android.kantukuang.weibo.WeiboTimelineException;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * This test case is only used to check if the access token still works.
 */
public class WeiboClientE2ETest extends TestCase {
    @Inject
    private WeiboClient mClient;

    @Override
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new KanTuKuangModule(new Application()));
        injector.injectMembers(this);
    }

    public void testGetPublicTimeline() throws IOException, InterruptedException, WeiboTimelineException {
        WeiboTimeline timeline = mClient.getPublicTimeline(null);

        assertNotNull(timeline);
        assertTrue(timeline.statuses.size() > 0);
    }

}
