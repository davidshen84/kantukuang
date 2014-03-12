package com.xi.android.kantukuang.weibo;


import android.util.Log;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.io.IOException;

public class WeiboClient {
    private static final String TAG = WeiboClient.class.getName();
    private static final EmptyContent EMPTY_CONTENT = new EmptyContent();
    private final Credential.AccessMethod mAccessMethod;
    private final HttpTransport mHttpTransport;
    private final JsonObjectParser mJsonObjectParser;
    private Credential mCredential;
    private HttpRequestFactory mRequestFactory;
    private String mAccessToken;

    @Inject
    public WeiboClient(Credential.AccessMethod accessMethod,
                       HttpTransport httpTransport,
                       JsonObjectParser jsonObjectParser,
                       @Named("access token") String accessToken,
                       @Named("redirect uri") String redirectUri) {
        mAccessMethod = accessMethod;
        mHttpTransport = httpTransport;
        mJsonObjectParser = jsonObjectParser;
        mAccessToken = accessToken;
    }

    private HttpRequestFactory getRequestFactory() {
        if (mRequestFactory == null) {
            // build credential
            mCredential = new Credential(mAccessMethod).setAccessToken(mAccessToken);
            Log.d(TAG, String.format("created credential with token: %s", mAccessToken));

            // create request factory
            mRequestFactory = mHttpTransport.createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest httpRequest) throws IOException {
                    mCredential.initialize(httpRequest);
                    httpRequest.setParser(mJsonObjectParser);
                }

            });
            Log.v(TAG, "created new request factory");
        }

        return mRequestFactory;
    }

    private WeiboTimeline getWeiboTimelineByUrl(
            AbstractWeiboTimelineUrl url) throws WeiboTimelineException {
        WeiboTimeline weiboTimeline = null;
        HttpResponse httpResponse = null;

        boolean hasException = false;
        try {
            HttpRequest httpRequest = getRequestFactory().buildGetRequest(url);

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

    @Deprecated
    public WeiboTimeline getHomeTimeline(String sinceId) throws WeiboTimelineException {
        AbstractWeiboTimelineUrl url = new HomeTimelineUrl();
        if (sinceId != null)
            url.sinceId = sinceId;

        return getWeiboTimelineByUrl(url);
    }

    @Deprecated
    public WeiboTimeline getFriendsTimeline(String sinceId) throws WeiboTimelineException {
        FriendsTimelineUrl url = new FriendsTimelineUrl();
        if(sinceId!=null)
            url.sinceId=sinceId;

        return getWeiboTimelineByUrl(url);
    }

    public void setAccessToken(String accessToken) {
        this.mAccessToken = accessToken;
    }

    @Deprecated
    public WeiboFriends getFriends(String id, String cursor) {

        WeiboFriendsUrl url = new WeiboFriendsUrl(id);

        if (!Strings.isNullOrEmpty(cursor)) {
            url.cursor = cursor;
        }

        HttpResponse httpResponse = null;
        try {
            HttpRequest httpRequest = getRequestFactory().buildGetRequest(url);
            httpResponse = httpRequest.execute();
            if (httpResponse.isSuccessStatusCode())
                return httpResponse.parseAs(WeiboFriends.class);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    @Deprecated
    public WeiboTokenInfo getTokenInfo() {
        WeiboTokenInfoUrl url = new WeiboTokenInfoUrl();

        HttpResponse httpResponse = null;
        try {
            HttpRequest httpRequest = getRequestFactory().buildPostRequest(url, EMPTY_CONTENT);
            httpResponse = httpRequest.execute();

            if (httpResponse.isSuccessStatusCode())
                return httpResponse.parseAs(WeiboTokenInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     * Repost a weibo status by id
     *
     * @param weiboId weibo id
     * @param comment if not null, add the comment to current repost
     * @return posted status, or null if failed
     */
    @Deprecated
    public WeiboRepostResponse repost(String weiboId, String comment) {
        WeiboRepostUrl url = new WeiboRepostUrl(weiboId);

        if (!Strings.isNullOrEmpty(comment))
            url.setComment(comment);

        HttpResponse httpResponse = null;
        try {
            HttpRequest httpRequest = getRequestFactory().buildPostRequest(url, EMPTY_CONTENT);
            httpResponse = httpRequest.execute();

            if (httpResponse.isSuccessStatusCode())
                return httpResponse.parseAs(WeiboRepostResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     * Query account information
     *
     * @param uid weibo user account id
     * @return {@link com.xi.android.kantukuang.weibo.WeiboUserAccount}
     */
    @Deprecated
    public WeiboUserAccount show(String uid) {
        WeiboShowUserUrl url = new WeiboShowUserUrl(uid);

        try {
            HttpRequest httpRequest = getRequestFactory().buildGetRequest(url);
            HttpResponse httpResponse = httpRequest.execute();

            if (httpResponse.isSuccessStatusCode())
                return httpResponse.parseAs(WeiboUserAccount.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
