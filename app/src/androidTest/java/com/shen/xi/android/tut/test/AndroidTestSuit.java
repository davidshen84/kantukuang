package com.shen.xi.android.tut.test;


import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AndroidTestSuit extends TestSuite {
    public static Test suite() {
        return new TestSuiteBuilder(AndroidTestSuit.class)
                .includeAllPackagesUnderHere()
                .build();
    }
}
