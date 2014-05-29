package com.shen.xi.android.tut.util;

import com.google.common.base.Predicate;
import com.shen.xi.android.tut.weibo.WeiboStatus;

import javax.annotation.Nullable;


public final class Util {

    public static final Predicate<WeiboStatus> ImageUrlPredictor = new Predicate<WeiboStatus>() {
        @Override
        public boolean apply(@Nullable WeiboStatus status) {
            return status != null && status.getImageUrl() != null;
        }
    };

}
