package com.shen.xi.android.tut.weibo

import com.google.api.client.util.Key
import java.lang.{Long => JLong}
import java.util


class WeiboStatus {

  @Key("created_at")
  var createdAt: String = null
  @Key
  var text: String = null
  @Key("thumbnail_pic")
  var thumbnailUrl: String = null
  @Key("original_pic")
  var imageUrl: String = null
  @Key("idstr")
  var id: String = null
  @Key("retweeted_status")
  var repostedStatus: WeiboStatus = null
  @Key("pic_urls")
  var picUrls: util.List[WeiboThumbnail] = null

  /**
   * the user id who authored this status
   */
  @Key
  var uid: JLong = null

  def getImageUrl: String = {
    // get self image url
    if (imageUrl != null && !imageUrl.equalsIgnoreCase(""))
      imageUrl

    // get reposted status image url
    else if (repostedStatus != null)
      repostedStatus.getImageUrl

    // fall back to thumbnail :(
    else
      getThumbnailUrl
  }

  private def getThumbnailUrl: String = {
    if (thumbnailUrl != null && !thumbnailUrl.equalsIgnoreCase(""))
      thumbnailUrl
    else if (repostedStatus != null)
      repostedStatus.getThumbnailUrl
    else
      null
  }

  override def equals(o: Any) = o match {
    case other: WeiboStatus => other.getImageUrl.equalsIgnoreCase(this.getImageUrl)
    case _ => false
  }

  override def hashCode = getImageUrl.hashCode
}
