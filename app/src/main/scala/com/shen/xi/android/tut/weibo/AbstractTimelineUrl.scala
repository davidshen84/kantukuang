package com.shen.xi.android.tut.weibo

import com.google.api.client.http.GenericUrl
import com.google.api.client.util.Key


class AbstractTimelineUrl(encodedUrl: String) extends GenericUrl(encodedUrl) {
  @Key("trim_user")
  val trimUser = "1"
  @Key("count")
  var count = 10
  @Key("feature")
  val feature = "2"
  @Key("since_id")
  var sinceId: String = null
}
