package com.shen.xi.android.tut.test;


import com.google.api.client.util.Lists;
import com.google.common.base.Predicate;
import com.shen.xi.android.tut.util.Util;
import com.shen.xi.android.tut.weibo.WeiboStatus;

import junit.framework.TestCase;

import java.util.Collection;


public class UtilTest extends TestCase {

    public void testFilterStatus() {
        Collection<Long> blackList = Lists.newArrayList();
        blackList.add(123L);
        Predicate<WeiboStatus> predictor = Util.createBlacklistPredictor(blackList);

        WeiboStatus status = new WeiboStatus();
        status.uid_$eq(123L);
        boolean apply = predictor.apply(status);

        assertFalse(apply);
    }

    public void testFilterRepostStatus() {
        Collection<Long> blackList = Lists.newArrayList();
        blackList.add(new Long(123L));
        Predicate<WeiboStatus> predictor = Util.createBlacklistPredictor(blackList);

        WeiboStatus status = new WeiboStatus();
        status.uid_$eq(321L);
        status.repostedStatus_$eq(new WeiboStatus());
        status.repostedStatus().uid_$eq(123L);
        boolean apply = predictor.apply(status);

        assertFalse(apply);
    }

    public void testWillNotFilterStatus(){
        Collection<Long> blackList = Lists.newArrayList();
        blackList.add(123L);
        Predicate<WeiboStatus> predictor = Util.createBlacklistPredictor(blackList);

        WeiboStatus status = new WeiboStatus();
        status.uid_$eq(111L);
        boolean apply = predictor.apply(status);

        assertTrue(apply);
    }
}
