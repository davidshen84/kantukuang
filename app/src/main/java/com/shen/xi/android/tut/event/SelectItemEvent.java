package com.shen.xi.android.tut.event;


import android.os.Bundle;

import static com.shen.xi.android.tut.MainActivity.ImageSource;
import static com.shen.xi.android.tut.MainActivity.ImageSource.Unknown;

public class SelectItemEvent {
    public Bundle extras;
    public ImageSource source = Unknown;
}
