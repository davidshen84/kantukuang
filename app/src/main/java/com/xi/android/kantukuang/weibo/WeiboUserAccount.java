package com.xi.android.kantukuang.weibo;


import com.google.api.client.util.Key;

public class WeiboUserAccount {

    @Key("id")
    public long id;

    @Key("screen_name")
    public String screenName;

    @Key("profile_image_url")
    public String photoUrl;

}
