package com.xi.android.kantukuang.weibo;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

public abstract class AbstractWeiboTimelineUrl extends GenericUrl {
    @Key("trim_user")
    public final String TrimUser = "1";
    @Key("count")
    public int count = 10;
    @Key("feature")
    public String feature;
    @Key("since_id")
    public String SinceId = null;

    public AbstractWeiboTimelineUrl(String encodedUrl) {
        super(encodedUrl);
    }
}
