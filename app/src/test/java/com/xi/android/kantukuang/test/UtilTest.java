package com.xi.android.kantukuang.test;


import com.xi.android.kantukuang.util.Util;
import com.xi.android.kantukuang.weibo.WeiboUserAccount;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
public class UtilTest {


    @Test
    public void testExtractUidFunction() {
        WeiboUserAccount weiboUserAccount = new WeiboUserAccount();
        weiboUserAccount.id = 1;
        Long uid = Util.extractUidFunction.apply(weiboUserAccount);
        assertEquals(1L, uid.longValue());

        uid = Util.extractUidFunction.apply(null);
        assertEquals(-1L, uid.longValue());
    }
}
