package com.xi.android.kantukuang.weibo;


import com.google.api.client.util.Key;

public class WeiboRepostResponse {
    @Key("created_at")
    public String createdAt;

    @Key
    public long id;

    @Key
    public String text;
}
