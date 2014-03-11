package com.xi.android.kantukuang.test;

import android.app.Application;

import com.google.common.collect.Lists;
import com.xi.android.kantukuang.KanTuKuangModule;
import com.xi.android.kantukuang.sinablog.ArticleInfo;
import com.xi.android.kantukuang.sinablog.QingClient;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

public class QingClientClientTest extends TestCase {

    private QingClient qingClient;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        KanTuKuangModule.initialize(new Application());

        InputStream resourceAsStream = this.getClass().getClassLoader()
                .getResourceAsStream("assets/tagresult.json");

        qingClient = QingClient.createFromJson(new InputStreamReader(resourceAsStream));
    }

    public void testCreateFromJson() throws IOException {
        assertNotNull(qingClient);
        assertEquals("猫", qingClient.getTag());

        Collection<ArticleInfo> list = qingClient.getArticleInfoList();
        assertNotNull(list);
        assertEquals(10, list.size());
    }

    public void testCreateFromTag() {
        QingClient client = QingClient.createForTag("猫");
        assertNotNull(client);
    }

    public void testCanLoadDocument() {
        QingClient client = QingClient.createForTag("猫");
        assertTrue(client.load());
    }

    public void testParseArticleInfo() {
        ArrayList<ArticleInfo> articleInfos = Lists.newArrayList(qingClient.getArticleInfoList());

        assertEquals(10, articleInfos.size());

        ArticleInfo info1 = articleInfos.get(0);
        assertEquals("http://qing.blog.sina.com.cn/tj/8e18904032004q8g.html", info1.href);
        assertEquals("http://ww3.sinaimg.cn/mw205/8e189040jw1edkautot30j20ij0ijta2.jpg",
                     info1.imageSrc);

        ArticleInfo info2 = articleInfos.get(9);
        assertEquals("http://qing.blog.sina.com.cn/tj/5c3aa1ba32004o92.html", info2.href);
        assertEquals("http://ww4.sinaimg.cn/mw205/5c3aa1bajw1ecjapp17pej20dw0kudgo.jpg",
                     info2.imageSrc);
    }
}
