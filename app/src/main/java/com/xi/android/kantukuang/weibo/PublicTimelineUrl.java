package com.xi.android.kantukuang.weibo;

class PublicTimelineUrl extends AbstractWeiboTimelineUrl {
    private static final String PUBLIC_TIMELINE_URL = "https://api.weibo.com/2/statuses/public_timeline.json";

    public PublicTimelineUrl() {
        super(PUBLIC_TIMELINE_URL);

        feature = "2";
    }
}
