package com.xi.android.kantukuang.util;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.xi.android.kantukuang.weibo.WeiboStatus;
import com.xi.android.kantukuang.weibo.WeiboUserAccount;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;


public final class Util {
    public static final Predicate<WeiboStatus> ImageUrlPredictor = new Predicate<WeiboStatus>() {
        @Override
        public boolean apply(@Nullable WeiboStatus status) {
            return status != null && status.getImageUrl() != null;
        }
    };
    public static final Function<WeiboUserAccount, Long> extractUidFunction =
            new Function<WeiboUserAccount, Long>() {
                @Nullable
                @Override
                public Long apply(@Nullable WeiboUserAccount weiboUserAccount) {
                    return weiboUserAccount != null ? weiboUserAccount.id : -1L;
                }
            };

    public static WeiboFriendPredicate createFriendPredictor(List<WeiboUserAccount> friends) {
        return new WeiboFriendPredicate(friends);
    }

    public static class WeiboFriendPredicate implements Predicate<WeiboStatus> {
        private final Collection<Long> mFriendIds;

        private WeiboFriendPredicate(Collection<WeiboUserAccount> friends) {
            mFriendIds = Collections2.transform(friends, extractUidFunction);
        }

        @Override
        public boolean apply(@Nullable WeiboStatus status) {
            return status != null && mFriendIds.contains(status.uid);
        }
    }
}
