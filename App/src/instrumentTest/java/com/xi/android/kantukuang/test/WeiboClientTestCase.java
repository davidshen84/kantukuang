package com.xi.android.kantukuang.test;

import android.test.AndroidTestCase;

import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.xi.android.kantukuang.KanTuKuangModule;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboTimeline;
import com.xi.android.kantukuang.weibo.WeiboTimelineException;

import java.io.IOException;

/**
 * Created by shend on 12/11/13.
 */

public class WeiboClientTestCase extends AndroidTestCase {
    private final String accessToken = "2.00uOPaHD1JlHSDcc83013405KD6O9D";
    private WeiboClient client;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Injector injector = Guice.createInjector(new KanTuKuangModule());
        client = injector.getInstance(WeiboClient.class);
        client.setAccessToken(accessToken);

    }

    // TODO remove this
/*
    public void testGetMyTags() throws IOException, JSONException {


        client.configureRequestFactory();
        String[] tags = client.getMyTags();

        assertNotNull(tags);
        assertTrue(tags[0].contains("美图摄影"));


    }
*/
    public void testGetPublicTimeline() throws IOException, InterruptedException, WeiboTimelineException {
//        client.configureRequestFactory();
        WeiboTimeline timeline = client.getPublicTimeline(null);

        assertNotNull(timeline);
//        assertEquals(20, timeline.totalNumber);
        assertTrue(timeline.statuses.size() > 0);
        assertNotNull(timeline.statuses.get(0).imageUrl);
    }

    public void testGetHomeTimeline() throws WeiboTimelineException {
//        client.configureRequestFactory();
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

    public void testJacksonParser() throws IOException {
        JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
        JsonParser parser = jacksonFactory.createJsonParser(
                "{\"statuses\":[{\"created_at\":\"Tue Dec 24 10:41:14 +0800 2013\",\"id\":3658982511919883,\"mid\":\"3658982511919883\",\"idstr\":\"3658982511919883\",\"text\":\"#微盘点#2013微博娱乐精神：这一年，娱乐精神属于微博网友，他们正年轻，为#小时代#争论不休；他们爱憎分，动辄摇旗呐喊 #xx 滚出xx圈#；他们脑洞大，力宏和云迪的故事YY一整年；他们敢表达，逮到土豪就要做朋友；他们不孤单，什么事小伙伴儿都会出马；他们没长大，知不知道也要问，爸爸我们去哪儿呀？\",\"source\":\"<a href=\\\"http://weibo.com/\\\" rel=\\\"nofollow\\\">新浪微博</a>\",\"favorited\":false,\"truncated\":false,\"in_reply_to_status_id\":\"\",\"in_reply_to_user_id\":\"\",\"in_reply_to_screen_name\":\"\",\"pic_urls\":[{\"thumbnail_pic\":\"http://ww3.sinaimg.cn/thumbnail/61ecce97jw1ebulflu1lzj20c837uqov.jpg\"}],\"thumbnail_pic\":\"http://ww3.sinaimg.cn/thumbnail/61ecce97jw1ebulflu1lzj20c837uqov.jpg\",\"bmiddle_pic\":\"http://ww3.sinaimg.cn/bmiddle/61ecce97jw1ebulflu1lzj20c837uqov.jpg\",\"original_pic\":\"http://ww3.sinaimg.cn/large/61ecce97jw1ebulflu1lzj20c837uqov.jpg\",\"geo\":null,\"uid\":1642909335,\"reposts_count\":22,\"comments_count\":7,\"attitudes_count\":27,\"mlevel\":0,\"visible\":{\"type\":0,\"list_id\":0}}],\"advertises\":[],\"ad\":[],\"hasvisible\":false,\"previous_cursor\":0,\"next_cursor\":3658980968623250,\"total_number\":2805}");
        WeiboTimeline obj = parser.parse(WeiboTimeline.class);
        parser.close();

        assertNotNull(obj);
//        assertEquals(obj.totalNumber, 1);
        assertEquals(obj.statuses.size(), 1);
        assertNotNull(obj.statuses.get(0).thumbnailUrl);
    }
/*    public void testGetAccessTokenUrl() throws IOException {
        String url = client.getAccessTokenUrl("123code");
    }*/
}
