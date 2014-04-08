package com.xi.android.kantukuang.test;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.Json;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.common.io.CharStreams;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboStatus;
import com.xi.android.kantukuang.weibo.WeiboTimeline;
import com.xi.android.kantukuang.weibo.WeiboTimelineException;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WeiboClientTest extends TestCase {

    private WeiboClient mClient;
    @Inject
    private TestModule.MockHttpTransport httpTransport;
    @Inject
    private HttpRequestInitializer mInitializer;
    @Inject
    private MockLowLevelHttpResponse mResponse;


    @Override
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new TestModule());
        injector.injectMembers(this);

        InputStream resourceAsStream = this.getClass().getClassLoader()
                .getResourceAsStream("assets/publicTimeline.json");
        String publicTimeline = CharStreams.toString(new InputStreamReader(resourceAsStream));
        resourceAsStream.close();

        mResponse.setContentType(Json.MEDIA_TYPE);
        mResponse.setContent(publicTimeline);
        httpTransport.setResponse(mResponse);
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory(mInitializer);
        mClient = new WeiboClient(requestFactory);
    }

    public void testGetPublicTimeline() throws IOException, InterruptedException, WeiboTimelineException {
        WeiboTimeline timeline = mClient.getPublicTimeline(null);

        assertNotNull(timeline);
        assertTrue(timeline.statuses.size() > 0);
        assertNotNull(timeline.statuses.get(0).imageUrl);
    }

    public void testGetTimeline_uid() throws WeiboTimelineException {
        WeiboTimeline timeline = mClient.getPublicTimeline(null);

        WeiboStatus status = timeline.statuses.get(0);
        assertNotNull(status.uid);
        assertFalse(0L == status.uid);
    }

}
