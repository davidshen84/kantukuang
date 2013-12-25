package com.xi.android.kantukuang.weibo;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

public class WeiboTagsUrl extends GenericUrl {
    @Key("uid")
    private String mUid;

    public WeiboTagsUrl(String uid) {
        super("https://api.weibo.com/2/tags.json");

        mUid = uid;
    }
}
