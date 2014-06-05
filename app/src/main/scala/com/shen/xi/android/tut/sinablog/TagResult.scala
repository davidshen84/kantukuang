package com.shen.xi.android.tut.sinablog


import com.google.api.client.util.Key
import java.util

class TagResult {
  @Key
  var qjson: String = ""
  @Key
  var cnt: Int = 0
  @Key
  var list: util.ArrayList[String] = null
  @Key
  var isLastPage: Boolean = false
}
