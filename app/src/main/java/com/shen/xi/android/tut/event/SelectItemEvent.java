package com.shen.xi.android.tut.event;


import com.shen.xi.android.tut.MainActivity;

public class SelectItemEvent {
    public int position;
    public MainActivity.ImageSource source;

    public SelectItemEvent() {
        position = 0;
    }
}
