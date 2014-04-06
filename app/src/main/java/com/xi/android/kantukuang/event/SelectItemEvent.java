package com.xi.android.kantukuang.event;


import com.xi.android.kantukuang.MainActivity;

public class SelectItemEvent {
    public int position;
    public MainActivity.SelectEventSource source;

    public SelectItemEvent() {
        position = 0;
    }
}
