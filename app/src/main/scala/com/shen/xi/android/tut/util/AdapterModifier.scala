package com.shen.xi.android.tut.util

import java.util.{Collection => JCollection, List => JList}

import android.widget.ArrayAdapter


trait AdapterModifier[T] extends ArrayAdapter[T] {
  val list: JList[T]

  /**
   * **Prepend** @list to the original list
   * @param collection collection to prepend
   */
  def +=(collection: JCollection[T]) = {
    this.list.addAll(0, collection)
    super.notifyDataSetChanged()
  }
}
