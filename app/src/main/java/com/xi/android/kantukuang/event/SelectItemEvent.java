package com.xi.android.kantukuang.event;


import static com.xi.android.kantukuang.MainActivity.ImageSource;

public class SelectItemEvent {
    public int position;
    public ImageSource source;

    public SelectItemEvent() {
        position = 0;
    }
}
