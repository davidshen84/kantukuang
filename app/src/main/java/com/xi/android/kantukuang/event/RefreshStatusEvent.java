package com.xi.android.kantukuang.event;


public class RefreshStatusEvent {
    private String mAccountId;
    private String mSinceId;

    public String getAccountId() {
        return mAccountId;
    }

    public void setAccountId(String accountId) {
        mAccountId = accountId;
    }

    public String getSinceId() {
        return mSinceId;
    }

    public void setSinceId(String sinceId) {
        mSinceId = sinceId;
    }
}
