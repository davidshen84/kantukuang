package com.xi.android.kantukuang.test;


import com.google.api.client.util.Lists;
import com.google.common.base.Predicate;
import com.xi.android.kantukuang.util.Util;
import com.xi.android.kantukuang.weibo.WeiboStatus;
import com.xi.android.kantukuang.weibo.WeiboUserAccount;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class UtilTest {


    @Test
    public void extractUidFunction() {
        WeiboUserAccount weiboUserAccount = new WeiboUserAccount();
        weiboUserAccount.id = 1;
        Long uid = Util.extractUidFunction.apply(weiboUserAccount);
        assertEquals(1L, uid.longValue());

        uid = Util.extractUidFunction.apply(null);
        assertEquals(-1L, uid.longValue());
    }

    @Test
    public void canFilterStatus() {
        Collection<Long> blackList = Lists.newArrayList();
        blackList.add(123L);
        Predicate<WeiboStatus> predictor = Util.createBlacklistPredictor(blackList);

        WeiboStatus status = new WeiboStatus();
        status.uid = 123L;
        boolean apply = predictor.apply(status);

        assertFalse(apply);
    }

    @Test
    public void canFilterRepostStatus() {
        Collection<Long> blackList = Lists.newArrayList();
        blackList.add(123L);
        Predicate<WeiboStatus> predictor = Util.createBlacklistPredictor(blackList);

        WeiboStatus status = new WeiboStatus();
        status.uid = 321L;
        status.repostedStatus = new WeiboStatus();
        status.repostedStatus.uid = 123L;
        boolean apply = predictor.apply(status);

        assertFalse(apply);
    }

    @Test
    public void willNotFilterStatus(){
        Collection<Long> blackList = Lists.newArrayList();
        blackList.add(123L);
        Predicate<WeiboStatus> predictor = Util.createBlacklistPredictor(blackList);

        WeiboStatus status = new WeiboStatus();
        status.uid = 111L;
        boolean apply = predictor.apply(status);

        assertTrue(apply);
    }
}
