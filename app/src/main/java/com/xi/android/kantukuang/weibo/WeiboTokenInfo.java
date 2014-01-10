package com.xi.android.kantukuang.weibo;


import com.google.api.client.util.Key;

public class WeiboTokenInfo {
    @Key
    public long uid;
    @Key("expire_in")
    public long expireIn;
}
