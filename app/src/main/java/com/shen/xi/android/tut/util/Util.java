package com.shen.xi.android.tut.util;

import com.google.common.base.Predicate;
import com.shen.xi.android.tut.weibo.WeiboStatus;

import java.util.Collection;

import javax.annotation.Nullable;


public final class Util {
    public static final Predicate<WeiboStatus> ImageUrlPredictor = new Predicate<WeiboStatus>() {
        @Override
        public boolean apply(@Nullable WeiboStatus status) {
            return status != null && status.getImageUrl() != null;
        }
    };

    public static Predicate<WeiboStatus> createBlacklistPredictor(Collection<Long> blackList) {
        return new BlackListPredicate(blackList);
    }

    private static class BlackListPredicate implements Predicate<WeiboStatus> {
        private final Collection<Long> mBlackList;

        public BlackListPredicate(Collection<Long> blackList) {
            mBlackList = blackList;
        }

        @Override
        public boolean apply(@Nullable WeiboStatus status) {
            if (status == null)
                return false;

            if (status.repostedStatus() != null) {
                return !mBlackList.contains(status.repostedStatus().uid());
            } else {
                return !mBlackList.contains(status.uid());
            }
        }
    }

}
