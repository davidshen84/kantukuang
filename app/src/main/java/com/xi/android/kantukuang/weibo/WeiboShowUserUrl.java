package com.xi.android.kantukuang.weibo;


import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

public class WeiboShowUserUrl extends GenericUrl{
    @Key
    private final String uid;

    public WeiboShowUserUrl(String uid){
        super("https://api.weibo.com/2/users/show.json");
        this.uid = uid;
    }
}
