package com.xi.android.kantukuang.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.xi.android.kantukuang.sinablog.QingPageDriver;

import junit.framework.TestCase;

import java.io.InputStream;
import java.util.List;

public class QingPageDriverTest extends TestCase {

    private QingPageDriver mDrive = new QingPageDriver();

    @Override
    public void setUp() throws Exception {
        super.setUp();

        InputStream resourceAsStream = this.getClass().getClassLoader()
                .getResourceAsStream("assets/qingPage.html");
        mDrive.parse(resourceAsStream);
        resourceAsStream.close();
        Injector injector = Guice.createInjector(new TestModule());
        injector.injectMembers(this);
    }

    @Override
    public void tearDown() throws Exception {

    }

    public void testParsePage() {
        assertNotNull(mDrive.getImageUrlList());
    }

    public void testGetImageUrls() {
        List<String> imageUrlList = mDrive.getImageUrlList();
        assertEquals(5, imageUrlList.size());
        assertEquals("http://ww4.sinaimg.cn/mw600/e2830b12jw1eevfj15tf0j20tn18ggys.jpg", imageUrlList.get(0));
    }
}