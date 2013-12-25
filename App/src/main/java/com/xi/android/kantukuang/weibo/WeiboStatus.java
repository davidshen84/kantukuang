package com.xi.android.kantukuang.weibo;

import com.google.api.client.util.Key;

public class WeiboStatus {


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

    public String getImageUrl() {
        if (imageUrl != null && !imageUrl.equalsIgnoreCase(""))
            return imageUrl;

        if (repostedStatus != null)
            return repostedStatus.getImageUrl();

        return null;
    }

    public String getThumbnailUrl() {
        if (thumbnailUrl != null && !thumbnailUrl.equalsIgnoreCase(""))
            return thumbnailUrl;

        if (repostedStatus != null)
            return repostedStatus.getThumbnailUrl();

        return null;
    }
}