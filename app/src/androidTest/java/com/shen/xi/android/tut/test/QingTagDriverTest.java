package com.shen.xi.android.tut.test;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.common.io.CharStreams;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.shen.xi.android.tut.sinablog.ArticleInfo;
import com.shen.xi.android.tut.sinablog.QingTagDriver;
import com.shen.xi.android.tut.sinablog.TagResultUrl;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class QingTagDriverTest extends TestCase {


    private QingTagDriver mDriver;
    @Inject
    private JsonObjectParser jsonObjectParser;
    @Inject
    private TestModule.MockHttpTransport httpTransport;
    @Inject
    private MockLowLevelHttpResponse mResponse;
    @Inject
    private HttpRequestInitializer mInitializer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        InputStream resourceAsStream = this.getClass().getClassLoader()
                .getResourceAsStream("assets/tagresult.json");
        String sampleContent = CharStreams.toString(new InputStreamReader(resourceAsStream));
        resourceAsStream.close();

        Injector injector = Guice.createInjector(new TestModule());
        injector.injectMembers(this);

        // setup mock response
        mResponse.setContent(sampleContent);
        mResponse.setContentType(Json.MEDIA_TYPE);
        httpTransport.setResponse(mResponse);
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory(mInitializer);
        mDriver = new QingTagDriver(requestFactory);
    }

    public void testBuildTagRequest() {
        HttpRequest test = mDriver.buildTagRequest("test", 1);
        TagResultUrl testUrl = (TagResultUrl) test.getUrl();
        assertEquals("test", testUrl.tag);
        assertEquals(1, testUrl.page);
    }

    public void testLoad() throws IOException {
        HttpRequest httpRequest = mDriver.buildTagRequest("", 1);
        assertTrue(mDriver.load(httpRequest));

        ArrayList<ArticleInfo> articleInfoList = mDriver.getArticleInfoList();
        assertNotNull(articleInfoList);
    }

    public void testParseArticleInfo() {
        mDriver.load(mDriver.buildTagRequest("", 1));
        ArrayList<ArticleInfo> articleInfoList = mDriver.getArticleInfoList();

        assertEquals(10, articleInfoList.size());

        ArticleInfo info1 = articleInfoList.get(0);
        assertEquals("http://qing.blog.sina.com.cn/tj/8e18904032004q8g.html", info1.href);
        assertEquals("http://ww3.sinaimg.cn/mw205/8e189040jw1edkautot30j20ij0ijta2.jpg",
                     info1.imageSrc);

        ArticleInfo info2 = articleInfoList.get(9);
        assertEquals("http://qing.blog.sina.com.cn/tj/5c3aa1ba32004o92.html", info2.href);
        assertEquals("http://ww4.sinaimg.cn/mw205/5c3aa1bajw1ecjapp17pej20dw0kudgo.jpg",
                     info2.imageSrc);
    }

}
