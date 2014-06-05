package com.shen.xi.android.tut.sinablog


import android.util.Log

import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.HttpResponse
import com.google.inject.Inject
import com.google.inject.name.Named

import org.jsoup.Jsoup

import java.io.IOException

import scala.collection.JavaConversions._
import java.util


object QingTagDriver {
  val BASE_URI = "qing.blog.sina.com.cn"
  val TAG = classOf[QingTagDriver].getName
}

class QingTagDriver @Inject()(@Named("qing request factory") requestFactory: HttpRequestFactory) {

  import QingTagDriver._

  var mHttpRequestFactory: HttpRequestFactory = requestFactory
  var mTagResult: TagResult = null
  var mArticleInfoList: util.ArrayList[ArticleInfo] = null

  /**
   * Helper method to build the URL object
   *
   * @param tag  the tag string
   * @param page the page number, based on 1
   * @return the URL object
   */
  def buildTagRequest(tag: String, page: Int): HttpRequest = {
    val url: TagResultUrl = new TagResultUrl()
    url.tag = tag
    url.page = page

    try {
      mHttpRequestFactory.buildGetRequest(url)
    } catch {
      case e: IOException => Log.d(TAG, e.getMessage); null;
    }
  }

  def hasLoaded: Boolean = mTagResult != null

  def isLast: Boolean = hasLoaded && mTagResult.isLastPage

  def getArticleInfoList: util.ArrayList[ArticleInfo] = mArticleInfoList

  /**
   * Execute the given request and pares the result
   *
   * @param request a HTTP request
   * @return true for successful loading
   */
  def load(request: HttpRequest): Boolean = {
    var response: HttpResponse = null

    try {
      response = request.execute()
      if (response.isSuccessStatusCode) {
        val tagResponse: TagResponse = response.parseAs(classOf[TagResponse])
        mTagResult = tagResponse.data
      } else {
        Log.w(TAG, s"request to ${request.getUrl} failed")
      }

      if (mTagResult != null && mTagResult.cnt > 0) {
        parseList(mTagResult.list)
      } else {
        Log.w(TAG, s"request to $mTagResult returns no result")
      }
    } catch {
      case e: IOException => e.printStackTrace(); false
    } finally {
      if (response != null) {
        try {
          response.disconnect()
        } catch {
          case _: Exception => return false
        }
      }
    }

    true
  }

  private def parseList(list: util.Collection[String]): Unit = {
    mArticleInfoList = new util.ArrayList[ArticleInfo]()

    val articles = for (i <- list) yield Jsoup.parse(i, BASE_URI)
    for (article <- articles) {
      mArticleInfoList.addAll(article.select(".itemArticle > .itemInfo > :first-child")
        .map(e => (e.attr("href"), e.select("img").headOption)).filterNot(_._2 == None)
        .map(e => new ArticleInfo() {
        href = e._1; imageSrc = e._2.get.attr("src")
      }))
    }
  }

}
