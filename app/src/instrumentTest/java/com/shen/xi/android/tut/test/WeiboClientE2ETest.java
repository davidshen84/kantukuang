package com.shen.xi.android.tut.test;

import android.app.Application;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.shen.xi.android.tut.TuTModule;
import com.shen.xi.android.tut.weibo.WeiboClient;
import com.shen.xi.android.tut.weibo.WeiboTimeline;
import com.shen.xi.android.tut.weibo.WeiboTimelineException;

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
        Injector injector = Guice.createInjector(new TuTModule(new Application()));
        injector.injectMembers(this);
    }

    public void testGetPublicTimeline() throws IOException, InterruptedException, WeiboTimelineException {
        WeiboTimeline timeline = mClient.getHomeTimeline(null);

        assertNotNull(timeline);
        assertTrue(timeline.statuses.size() > 0);
    }

}
