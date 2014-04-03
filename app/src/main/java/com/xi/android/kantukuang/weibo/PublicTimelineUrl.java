package com.xi.android.kantukuang.weibo;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

class PublicTimelineUrl extends GenericUrl {
    private static final String PUBLIC_TIMELINE_URL = "https://api.weibo.com/2/statuses/public_timeline.json";

    @Key("trim_user")
    public final String trimUser = "1";
    @Key("count")
    public int count = 10;
    @Key("feature")
    public String feature = "2";
    @Key("since_id")
    public String sinceId = null;

    public PublicTimelineUrl() {
        super(PUBLIC_TIMELINE_URL);
    }
}
