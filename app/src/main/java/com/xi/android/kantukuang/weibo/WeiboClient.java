package com.xi.android.kantukuang.weibo;


import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.io.IOException;

public class WeiboClient {
    private static final String TAG = WeiboClient.class.getName();
    private HttpRequestFactory mRequestFactory;

    @Inject
    public WeiboClient(@Named("weibo") HttpRequestFactory requestFactory) {
        mRequestFactory = requestFactory;
    }

    private WeiboTimeline getWeiboTimelineByUrl(
            AbstractWeiboTimelineUrl url) throws WeiboTimelineException {
        WeiboTimeline weiboTimeline = null;
        HttpResponse httpResponse = null;

        boolean hasException = false;
        try {
            HttpRequest httpRequest = mRequestFactory.buildGetRequest(url);
            httpResponse = httpRequest.execute();
            Log.d(TAG, String.format("status: %d", httpResponse.getStatusCode()));

            if (!httpResponse.isSuccessStatusCode()) {
                Log.d(TAG, String.format("there's an error when getting timeline (%s): %s",
                                         httpResponse.getStatusMessage(),
                                         httpResponse.parseAsString()));
            }

            weiboTimeline = httpResponse.parseAs(WeiboTimeline.class);
        } catch (HttpResponseException e) {
            Log.d(TAG, "there's an error in response when getting timeline");
            hasException = true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "there's an error when getting timeline.");
            hasException = true;
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (hasException) throw new WeiboTimelineException();

        if (weiboTimeline == null || weiboTimeline.statuses.size() == 0)
            Log.v(TAG, "json parse result is empty or no status is returned");

        return weiboTimeline;
    }

    /**
     * get public timeline with all ids greater than {@code sinceId}
     *
     * @param sinceId the "since_id" parameter
     * @return {@link com.xi.android.kantukuang.weibo.WeiboTimeline}
     * @throws WeiboTimelineException
     */
    public WeiboTimeline getPublicTimeline(String sinceId) throws WeiboTimelineException {
        AbstractWeiboTimelineUrl url = new PublicTimelineUrl();
        if (sinceId != null)
            url.sinceId = sinceId;

        return getWeiboTimelineByUrl(url);
    }

}
