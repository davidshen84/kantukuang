package com.xi.android.kantukuang;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public interface IRefreshEventDispatcher {
    void registerOnRefreshListener(OnRefreshListener listener);

    void unregisterOnRefreshListener();
}
