package com.xi.android.kantukuang.weibo;


import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

public class WeiboRepostUrl extends GenericUrl {
    @Key
    public long id;
    @Key("status")
    public String comment;
    @Key("is_comment")
    public int isComment;

    public WeiboRepostUrl(long weiboId) {
        super("https://api.weibo.com/2/statuses/repost.json");

        this.id = weiboId;
    }

    public void setComment(String comment) {
        this.comment = comment;
        isComment = 1;
    }
}
