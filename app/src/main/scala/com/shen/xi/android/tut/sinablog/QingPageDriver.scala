package com.shen.xi.android.tut.sinablog

import java.io.{IOException, InputStream}
import java.util

import android.util.Log
import com.google.inject.Inject
import com.shen.xi.android.tut.util.ExtensionMethods
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.JavaConversions._

object QingPageDriver {
  val TAG = classOf[QingPageDriver].getName
}

class QingPageDriver @Inject()() {

  import ExtensionMethods._
  import com.shen.xi.android.tut.sinablog.QingPageDriver.TAG


  private val mImageUrlList: util.List[String] = new util.ArrayList[String]

  def getImageUrlList: util.List[String] = mImageUrlList

  def load(url: String): Boolean = try {
    Jsoup.connect(url).get() |> { l => if (l!=null) parse(l) }
    true
  } catch {
    case e: IOException => Log.w(TAG, s"cannot load page $url%s"); false
  }

  def load(inputStream: InputStream): Boolean = try {
    Jsoup.parse(inputStream, "utf-8", "qing.blog.sina.com.cn") |> { l => if (l != null) parse(l) }
    true
  } catch {
    case e: IOException => Log.w(TAG, "cannot parse page"); false
  }

  private def parse(document: Document): Unit = {
    mImageUrlList.clear()

    val urls = document select ".feedInfo .imgArea img" map (e => {
      if (e.hasAttr("real_src")) {
        e.attr("real_src")
      } else if (e.hasAttr("src")) {
        e.attr("src")
      } else {
        Log.w(TAG, "img tag without src attribute")
        null
      }
    }) filterNot (_ == null)

    mImageUrlList.addAll(urls)
  }

}
