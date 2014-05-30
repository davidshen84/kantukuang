package com.shen.xi.android.tut.weibo;

import com.google.api.client.util.Key;

import java.util.List;

public final class WeiboStatus {

    @Key("created_at")
    public String createdAt;
    @Key
    public String text;
    @Key("thumbnail_pic")
    public String thumbnailUrl;
    @Key("original_pic")
    public String imageUrl;
    @Key("idstr")
    public String id;
    @Key("retweeted_status")
    public WeiboStatus repostedStatus;
    @Key("pic_urls")
    public List<WeiboThumbnail> picUrls;

    /**
     * the user id who authored this status
     */
    @Key
    public long uid;

    public String getImageUrl() {
        // get self image url
        if (imageUrl != null && !imageUrl.equalsIgnoreCase(""))
            return imageUrl;

        // get reposted status image url
        if (repostedStatus != null)
            return repostedStatus.getImageUrl();

        // fall back to thumbnail :(
        return getThumbnailUrl();
    }

    private String getThumbnailUrl() {
        if (thumbnailUrl != null && !thumbnailUrl.equalsIgnoreCase(""))
            return thumbnailUrl;

        if (repostedStatus != null)
            return repostedStatus.getThumbnailUrl();

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if ((o instanceof WeiboStatus)) {
            WeiboStatus other = (WeiboStatus) o;

            return other.getImageUrl().equalsIgnoreCase(this.getImageUrl());
        }

        return false;
    }
}
