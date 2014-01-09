package com.xi.android.kantukuang.weibo;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

public class WeiboFriendsUrl extends GenericUrl {

    @Key("trim_status")
    private static final int trimStatus = 1;

    @Key("uid")
    public String uid;

    @Key("cursor")
    public String cursor;

    public WeiboFriendsUrl(String uid) {
        super("https://api.weibo.com/2/friendships/friends.json");

        this.uid = uid;
    }
}
