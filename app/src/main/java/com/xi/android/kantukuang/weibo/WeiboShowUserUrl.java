package com.xi.android.kantukuang.weibo;


import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

public class WeiboShowUserUrl extends GenericUrl{
    @Key
    private final long uid;

    public WeiboShowUserUrl(long uid){
        super("https://api.weibo.com/2/users/show.json");
        this.uid = uid;
    }
}
