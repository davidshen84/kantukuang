package com.shen.xi.android.tut.weibo;

import com.google.api.client.util.Key;

import java.util.List;

public class WeiboTimeline {
    @Key("total_number")
    public int totalNumber;
    @Key
    public List<WeiboStatus> statuses;
}


