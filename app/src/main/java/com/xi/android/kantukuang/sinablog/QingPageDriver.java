package com.xi.android.kantukuang.sinablog;

import android.util.Log;

import com.google.api.client.http.HttpRequestFactory;
import com.google.inject.Inject;
import com.xi.android.kantukuang.KanTuKuangModule;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class QingPageDriver {
    private static final String TAG = QingPageDriver.class.getName();

    @Inject
    private HttpRequestFactory httpRequestFactory;
    private List<String> mImageUrlList = new ArrayList<String>();

    @Inject
    public QingPageDriver() {
    }

    public void parse(InputStream inputStream) {
        Document document = null;
        try {
            document = Jsoup.parse(inputStream, "utf-8", "qing.blog.sina.com.cn");
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, "cannot parse page");
        }

        if (document != null) {
            Elements mImageElement = document.select(".feedInfo .imgArea img");
            for (Element i : mImageElement) {
                if (i.hasAttr("real_src")) {
                    mImageUrlList.add(i.attr("real_src"));
                } else if (i.hasAttr("src")) {
                    mImageUrlList.add(i.attr("src"));
                } else {
                    Log.i(TAG, "img tag without src attribute");
                }
            }
        }
    }

    public List<String> getImageUrlList() {
        return mImageUrlList;
    }
}
