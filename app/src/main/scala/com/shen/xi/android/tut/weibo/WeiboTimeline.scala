package com.shen.xi.android.tut.weibo

import com.google.api.client.util.Key

import java.util.List

final class WeiboTimeline {
  @Key("total_number")
  var totalNumber: Int = -1
  @Key
  var statuses: List[WeiboStatus] = null
}


