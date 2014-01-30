package com.xi.android.kantukuang.event;


public class SelectItemEvent {
    private int mPosition;

    public SelectItemEvent() {
        mPosition = 0;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }
}
