package com.shen.xi.android.tut.sinablog

import com.google.api.client.http.GenericUrl
import com.google.api.client.util.Key

class TagResultUrl extends GenericUrl("http://qing.blog.sina.com.cn/blog/api/tagresult.php") {
    @Key
    var tag: String = ""
    @Key
    var page: Int = 0
}
