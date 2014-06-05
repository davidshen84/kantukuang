package com.shen.xi.android.tut.event;

import com.shen.xi.android.tut.weibo.WeiboStatus;

import java.util.List;


public class RefreshCompleteEvent {
  private List<WeiboStatus> mStatusList;
  private String mLastId;

  public List<WeiboStatus> getStatusList() {
    return mStatusList;
  }

  public RefreshCompleteEvent setStatusList(List<WeiboStatus> statusList) {
    this.mStatusList = statusList;

    return this;
  }

  public String getLastId() {
    return mLastId;
  }

  public RefreshCompleteEvent setLastId(String lastId) {
    this.mLastId = lastId;

    return this;
  }
}
