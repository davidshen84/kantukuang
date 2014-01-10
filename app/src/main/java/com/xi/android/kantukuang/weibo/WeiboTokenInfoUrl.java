package com.xi.android.kantukuang.weibo;

import com.google.api.client.http.GenericUrl;

public class WeiboTokenInfoUrl extends GenericUrl {
    public WeiboTokenInfoUrl() {
        super("https://api.weibo.com/oauth2/get_token_info");
    }
}
