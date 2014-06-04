package com.shen.xi.android.tut.weibo

import com.google.api.client.util.Key
import java.util

final class WeiboTimeline {
  @Key("total_number")
  var totalNumber: Int = -1
  @Key
  var statuses: util.List[WeiboStatus] = null
}


