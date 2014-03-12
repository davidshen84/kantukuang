package com.xi.android.kantukuang.sinablog;


import android.util.Log;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.JsonObjectParser;
import com.google.inject.Inject;
import com.xi.android.kantukuang.KanTuKuangModule;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

public class QingClient {

    private static final String BASE_URI = "qing.blog.sina.com.cn";
    private static final String TAG = QingClient.class.getName();
    private final TagResultUrl tagResultUrl = new TagResultUrl();
    private String mTag;
    private int mPageNumber = 1;
    @Inject
    private JsonObjectParser jsonObjectParser;
    @Inject
    private HttpRequestFactory httpRequestFactory;
    private TagResult tagResult;
    private ArrayList<ArticleInfo> articleArrayList;

    private QingClient() {
        KanTuKuangModule.getInjector().injectMembers(this);
    }

    public static QingClient createForTag(String tag) {
        QingClient client = new QingClient();

        client.setTag(tag);

        return client;
    }

    public static QingClient createFromJson(Reader jsonReader) throws IOException {
        QingClient client = new QingClient();
        client.parseJson(jsonReader);

        return client;
    }

    private void parseJson(Reader jsonReader) throws IOException {
        tagResult = jsonObjectParser.parseAndClose(jsonReader, TagResultResponse.class).data;
        setTag(tagResult.qjson);
        parseList(tagResult.list);
    }

    public boolean hasLoaded() {
        return tagResult != null;
    }

    public boolean isLast() {
        return hasLoaded() && tagResult.isLastPage;
    }

    public boolean load() {
        if (isLast())
            throw new IllegalStateException();

        try {
            tagResultUrl.tag = getTag();
            tagResultUrl.page = getPage();
            HttpResponse httpResponse = httpRequestFactory
                    .buildGetRequest(tagResultUrl)
                    .execute();

            if (httpResponse.isSuccessStatusCode()) {
                TagResultResponse tagResultResponse = httpResponse.parseAs(TagResultResponse.class);
                tagResult = tagResultResponse.data;
            } else {
                throw new IllegalStateException();
            }

            if (tagResult.cnt > 0) {
                parseList(tagResult.list);
            }
        } catch (IOException e) {
            e.printStackTrace();

            tagResult = null;
            articleArrayList = null;
        } finally {
            if (tagResult != null && !tagResult.isLastPage)
                mPageNumber++;
        }

        return hasLoaded();
    }

    private void parseList(Collection<String> list) {
        articleArrayList = new ArrayList<ArticleInfo>();
        for (String i : list) {
            Document article = Jsoup.parse(i, BASE_URI);
            Elements articleInfoElements = article.select(".itemArticle > .itemInfo > :first-child");
            for (Element e : articleInfoElements) {
                Elements imgElements = e.select("img");
                if (imgElements.size() > 0) {
                    ArticleInfo articleInfo = new ArticleInfo();

                    articleInfo.href = e.attr("href");
                    articleInfo.imageSrc = imgElements.first().attr("src");
                    articleArrayList.add(articleInfo);
                }
            }
        }
    }

    public ArrayList<ArticleInfo> getArticleInfoList() {
        return articleArrayList;
    }

    public String getTag() {
        return mTag;
    }

    public void setTag(String tag) {
        this.mTag = tag;
    }

    public int getPage() {
        return mPageNumber;
    }
}
