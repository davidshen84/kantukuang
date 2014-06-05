package com.shen.xi.android.tut.event;

import com.shen.xi.android.tut.weibo.WeiboStatus;

import java.util.List;


public class RefreshStatusCompleteEvent {
  private List<WeiboStatus> mStatusList;

  public List<WeiboStatus> getStatus() {
    return mStatusList;
  }

  public RefreshStatusCompleteEvent setStatus(List<WeiboStatus> status) {
    this.mStatusList = status;

    return this;
  }
}
