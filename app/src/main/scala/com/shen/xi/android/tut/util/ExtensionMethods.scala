package com.shen.xi.android.tut.util


object ExtensionMethods {

  implicit class Pipe[L](val l: L) extends AnyVal {
    def |>[R](f: L => R) = f(l)
  }

}
