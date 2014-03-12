package com.xi.android.kantukuang.test;

import android.app.Application;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.xi.android.kantukuang.KanTuKuangModule;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboFriends;
import com.xi.android.kantukuang.weibo.WeiboRepostResponse;
import com.xi.android.kantukuang.weibo.WeiboStatus;
import com.xi.android.kantukuang.weibo.WeiboTimeline;
import com.xi.android.kantukuang.weibo.WeiboTimelineException;
import com.xi.android.kantukuang.weibo.WeiboTokenInfo;
import com.xi.android.kantukuang.weibo.WeiboUserAccount;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Date;
import java.util.Random;
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


    public void testGetHomeTimeline() throws WeiboTimelineException {

        WeiboTimeline timeline = client.getHomeTimeline(null);

        assertNotNull(timeline);
        assertTrue(timeline.statuses.size() > 0);
        assertNotNull(timeline.statuses.get(0).text);
    }


    public void testGetTimeline_uid() throws WeiboTimelineException {
        WeiboTimeline timeline = client.getPublicTimeline(null);

        WeiboStatus status = timeline.statuses.get(0);
        assertNotNull(status.uid);
        assertFalse(0L == status.uid);

    }

    public void testGetFriendsTimeline() throws WeiboTimelineException {
        WeiboTimeline timeline = client.getFriendsTimeline(null);
        assertNotNull(timeline);
        assertTrue(timeline.statuses.size() > 0);
    }

    public void testGetFriends() {
        WeiboFriends friends = client.getFriends("2860471240", null);

        assertNotNull(friends);
        assertTrue(friends.users.size() > 0);

        logger.info(friends.users.get(0).screenName);
    }

    public void testGetTokenInfo() {
        WeiboTokenInfo tokenInfo = client.getTokenInfo();

        assertNotNull(tokenInfo);
        logger.info(String.format("%d", tokenInfo.uid));
    }

    public void testRepost() {
        String weiboId = "2759524863";
        Random random = new Random(new Date().getTime());

        String comment = String.format("test - %f", random.nextFloat());
        WeiboRepostResponse repost = client.repost(weiboId, comment);

        assertNotNull(repost);
        assertEquals(comment, repost.text);
        logger.info(repost.createdAt);
    }

    public void testShowAccountInfo() {
        String uid = "2860471240";
        WeiboUserAccount account = client.show(uid);

        assertNotNull(account);
        assertEquals(uid, String.valueOf(account.id));
    }
}
