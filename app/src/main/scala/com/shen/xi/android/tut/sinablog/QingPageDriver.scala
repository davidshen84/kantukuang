package com.shen.xi.android.tut.sinablog

import android.util.Log

import com.google.inject.Inject

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.io.{IOException, InputStream}

import scala.collection.JavaConversions._
import java.util

object QingPageDriver {
  val TAG = classOf[QingPageDriver].getName
}

class QingPageDriver @Inject()() {

  private val mImageUrlList: util.List[String] = new util.ArrayList[String]

  def getImageUrlList: util.List[String] = mImageUrlList

  def load(url: String): Boolean = {
    var document: Document = null

    try {
      document = Jsoup.connect(url).get()
    } catch {
      case e: IOException => Log.w(QingPageDriver.TAG, s"cannot load page $url%s"); false
    }

    parse(document)

    true
  }

  def load(inputStream: InputStream): Boolean = {
    var document: Document = null

    try {
      document = Jsoup.parse(inputStream, "utf-8", "qing.blog.sina.com.cn")
    } catch {
      case e: IOException => Log.w(QingPageDriver.TAG, "cannot parse page"); false
    }

    parse(document)

    true
  }

  private def parse(document: Document): Unit = {
    mImageUrlList.clear()

    val urls = document select ".feedInfo .imgArea img" map (e => {
      if (e.hasAttr("real_src")) {
        e.attr("real_src")
      } else if (e.hasAttr("src")) {
        e.attr("src")
      } else {
        Log.w(QingPageDriver.TAG, "img tag without src attribute")
        null
      }
    }) filterNot (_ == null)

    mImageUrlList.addAll(urls)
  }

}
