package com.shen.xi.android.tut.test;


import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;

public class AndroidTestSuit {
    public static Test suite() {
        return new TestSuiteBuilder(AndroidTestSuit.class)
                .includeAllPackagesUnderHere()
                .build();
    }
}
