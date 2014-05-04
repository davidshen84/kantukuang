package com.shen.xi.android.tut.sinablog;


import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class QingTagDriver {

    private static final String BASE_URI = "qing.blog.sina.com.cn";
    private static final String TAG = QingTagDriver.class.getName();

    private HttpRequestFactory httpRequestFactory;
    private TagResult mTagResult;
    private ArrayList<ArticleInfo> articleInfoList;

    @Inject
    public QingTagDriver(@Named("qing request factory") HttpRequestFactory requestFactory) {
        httpRequestFactory = requestFactory;
    }

    /**
     * Helper method to build the URL object
     *
     * @param tag  the tag string
     * @param page the page number, based on 1
     * @return the URL object
     */
    public HttpRequest buildTagRequest(String tag, int page) {
        TagResultUrl url = new TagResultUrl();
        url.tag = tag;
        url.page = page;

        try {
            return httpRequestFactory.buildGetRequest(url);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }

        return null;
    }

    public boolean hasLoaded() {
        return mTagResult != null;
    }

    public boolean isLast() {
        return hasLoaded() && mTagResult.isLastPage;
    }

    /**
     * Execute the given request and pares the result
     *
     * @param request a HTTP request
     * @return true for successful loading
     */
    public boolean load(HttpRequest request) {
        HttpResponse response = null;
        try {
            response = request.execute();
            if (response.isSuccessStatusCode()) {
                TagResponse tagResponse = response.parseAs(TagResponse.class);
                mTagResult = tagResponse.data;
            } else {
                return false;
            }

            if (mTagResult.cnt > 0) {
                parseList(mTagResult.list);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.disconnect();
                } catch (IOException ignored) {
                }
            }
        }

        return true;
    }

    private void parseList(Collection<String> list) {
        articleInfoList = new ArrayList<ArticleInfo>();
        for (String i : list) {
            Document article = Jsoup.parse(i, BASE_URI);
            Elements articleInfoElements = article.select(
                    ".itemArticle > .itemInfo > :first-child");
            for (Element e : articleInfoElements) {
                Elements imgElements = e.select("img");
                if (imgElements.size() > 0) {
                    ArticleInfo articleInfo = new ArticleInfo();

                    articleInfo.href = e.attr("href");
                    articleInfo.imageSrc = imgElements.first().attr("src");
                    articleInfoList.add(articleInfo);
                }
            }
        }
    }

    public ArrayList<ArticleInfo> getArticleInfoList() {
        return articleInfoList;
    }

}
