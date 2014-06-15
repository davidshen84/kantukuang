package com.shen.xi.android.tut.event


import com.shen.xi.android.tut.weibo.WeiboStatus


class RefreshCompleteEvent {
  var statusList: List[WeiboStatus] = null
  var lastId: String = null
}
