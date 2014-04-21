package com.xi.android.kantukuang.weibo;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;


public class AbstractTimelineUrl extends GenericUrl {
    @Key("trim_user")
    public final String trimUser = "1";
    @Key("count")
    public int count = 10;
    @Key("feature")
    public String feature = "2";
    @Key("since_id")
    public String sinceId = null;

    public AbstractTimelineUrl(String encodedUrl) {
        super(encodedUrl);
    }
}
