package com.shen.xi.android.tut


object ImageSource {
  def fromName(name: String) = name match {
    case Unknown.name => Unknown
    case Weibo.name => Weibo
    case QingPage.name => QingPage
    case QingTag.name => QingTag
    case _ => throw new IllegalArgumentException()
  }
}

abstract class ImageSource{
  val name = this.getClass.getName
  override def toString = name
}

case object Unknown extends ImageSource

case object Weibo extends ImageSource

case object QingTag extends ImageSource

case object QingPage extends ImageSource