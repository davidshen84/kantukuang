package com.xi.android.kantukuang.weibo;

class HomeTimelineUrl extends AbstractWeiboTimelineUrl {
    private static final String HOME_TIMELINE_URL = "https://api.weibo.com/2/statuses/home_timeline.json";

    public HomeTimelineUrl() {
        super(HOME_TIMELINE_URL);

        count = 20;
    }
}
