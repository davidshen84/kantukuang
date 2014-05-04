package com.shen.xi.android.tut.sinablog;

import android.util.Log;

import com.google.inject.Inject;

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

    private List<String> mImageUrlList = new ArrayList<String>();

    @Inject
    public QingPageDriver() {
    }

    public boolean load(String url) {
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            return false;
        }

        parse(document);

        return true;
    }

    public boolean load(InputStream inputStream) {
        Document document = null;
        try {
            document = Jsoup.parse(inputStream, "utf-8", "qing.blog.sina.com.cn");
        } catch (IOException e) {
            Log.w(TAG, "cannot load page");

            return false;
        }

        parse(document);

        return true;
    }

    private void parse(Document document) {
        mImageUrlList.clear();
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
