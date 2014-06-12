package com.shen.xi.android.tut.event;

import android.os.Bundle;

import com.shen.xi.android.tut.ImageSource;
import static com.shen.xi.android.tut.ImageSource.Unknown;


public class SelectItemEvent {
  public Bundle extras;
  public ImageSource source = Unknown;
}
