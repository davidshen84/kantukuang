package com.shen.xi.android.tut.test


import android.test.suitebuilder.TestSuiteBuilder


object AndroidTestSuit {

  def suite() =
    new TestSuiteBuilder(AndroidTestSuit.getClass)
      .includeAllPackagesUnderHere()
      .build()

}
