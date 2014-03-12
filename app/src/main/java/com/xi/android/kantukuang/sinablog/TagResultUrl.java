package com.xi.android.kantukuang.sinablog;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

public class TagResultUrl extends GenericUrl {
    @Key
    public String tag;
    @Key
    public int page;

    public TagResultUrl() {
        super("http://qing.blog.sina.com.cn/blog/api/tagresult.php");
    }
}
