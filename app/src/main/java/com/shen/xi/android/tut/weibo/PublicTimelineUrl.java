package com.shen.xi.android.tut.weibo;

class PublicTimelineUrl extends AbstractTimelineUrl {

    protected static final String PUBLIC_TIMELINE_URL = "https://api.weibo.com/2/statuses/public_timeline.json";

    public PublicTimelineUrl() {
        super(PUBLIC_TIMELINE_URL);
    }
}
