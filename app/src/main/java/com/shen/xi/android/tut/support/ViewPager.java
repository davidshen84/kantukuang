package com.shen.xi.android.tut.support;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Fix to issue https://github.com/chrisbanes/PhotoView/issues/31
 * <p/>
 * intercept the exception and ignore it
 */
public class ViewPager extends android.support.v4.view.ViewPager {
  private static final String TAG = ViewPager.class.getCanonicalName();

  public ViewPager(Context context) {
    super(context);
  }

  public ViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    try {
      return super.onInterceptTouchEvent(ev);
    } catch (IllegalArgumentException ignored) {
      Log.v(TAG, "oops...ignored");
    }

    return false;
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    try {
      return super.onTouchEvent(ev);
    } catch (IllegalArgumentException ignored) {
      Log.v(TAG, "oops...ignored");
    }

    return false;
  }
}
