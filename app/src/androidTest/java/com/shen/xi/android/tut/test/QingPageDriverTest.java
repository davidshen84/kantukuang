package com.shen.xi.android.tut.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.shen.xi.android.tut.sinablog.QingPageDriver;

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
        mDrive.load(resourceAsStream);
        resourceAsStream.close();
        Injector injector = Guice.createInjector(new TestModule());
        injector.injectMembers(this);
    }

    @Override
    public void tearDown() throws Exception {

    }

    public void testLoadPage() {
        assertNotNull(mDrive.getImageUrlList());
    }

    public void testGetImageUrls() {
        List<String> imageUrlList = mDrive.getImageUrlList();
        assertEquals(5, imageUrlList.size());
        assertEquals("http://ww4.sinaimg.cn/mw600/e2830b12jw1eevfj15tf0j20tn18ggys.jpg",
                     imageUrlList.get(0));
    }
}