package com.xi.android.kantukuang.weibo;

class HomeTimelineUrl extends AbstractWeiboTimelineUrl {
    public HomeTimelineUrl() {
        super("https://api.weibo.com/2/statuses/home_timeline.json");

        feature = "2";
    }
}
