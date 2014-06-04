package com.shen.xi.android.tut.weibo


import android.util.Log

import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpResponseException
import com.google.inject.Inject
import com.google.inject.name.Named

import java.io.IOException


final class WeiboClient @Inject() (@Named("weibo request factory") requestFactory: HttpRequestFactory) {
  val TAG = classOf[WeiboClient].getName
  var mRequestFactory: HttpRequestFactory = requestFactory

  @throws[WeiboTimelineException]
  private def getWeiboTimelineByUrl(url: AbstractTimelineUrl):  WeiboTimeline = {
    var weiboTimeline: WeiboTimeline = null
    var httpResponse: HttpResponse = null

    var hasException = false
    try {
      val httpRequest = mRequestFactory.buildGetRequest(url)

      httpResponse = httpRequest.execute()
      Log.d(TAG, s"status: ${httpResponse.getStatusCode}")

      if (!httpResponse.isSuccessStatusCode) {
        Log.d(TAG, s"there's an error when getting timeline (${httpResponse.getStatusMessage}): ${httpResponse.parseAsString}")
      }

      weiboTimeline = httpResponse.parseAs(classOf[WeiboTimeline])

      if (weiboTimeline == null || weiboTimeline.statuses.size() == 0)
        Log.v(TAG, "json parse result is empty or no status is returned")
      else
        Log.i(TAG, s"weibo returned ${weiboTimeline.statuses.size()} statuses")

      weiboTimeline
    } catch {
      case e: HttpResponseException => {
        Log.d(TAG, "there's an error in response when getting timeline")
        hasException = true
        null
      }
      case e: IOException => {
        e.printStackTrace()
        Log.d(TAG, "there's an error when getting timeline.")
        hasException = true
        null
      }
    } finally {
      if (httpResponse != null) {
        try {
          httpResponse.disconnect()
        } catch {
          case e: IOException => e.printStackTrace()
        }
      }

      if (hasException) throw new WeiboTimelineException()
    }
  }

  /**
    * get public timeline with all ids greater than {@code sinceId}
    *
    * @param sinceId the "since_id" parameter
    * @return {@link com.shen.xi.android.tut.weibo.WeiboTimeline}
    * @throws WeiboTimelineException
    */
  @throws[WeiboTimelineException]
  def getPublicTimeline(sinceId: String): WeiboTimeline = {
    val url = new PublicTimelineUrl()
    if (sinceId != null)
      url.sinceId = sinceId

    getWeiboTimelineByUrl(url)
  }

  /**
    * get public timeline with all ids greater than {@code sinceId}
    *
    * @param sinceId the "since_id" parameter
    * @return {@link com.shen.xi.android.tut.weibo.WeiboTimeline}
    * @throws WeiboTimelineException
    */
  @throws[WeiboTimelineException]
  def getHomeTimeline(sinceId: String): WeiboTimeline = {
    val url = new HomeTimelineUrl()
    if (sinceId != null)
      url.sinceId = sinceId

    getWeiboTimelineByUrl(url)
  }

}
