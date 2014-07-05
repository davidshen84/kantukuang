package com.shen.xi.android.tut.util

import android.database.DataSetObservable
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{ImageView, ListAdapter}
import com.google.inject.Inject
import com.google.inject.name.Named
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import com.nostra13.universalimageloader.core.{DisplayImageOptions, ImageLoader}
import com.shen.xi.android.tut.{R, TuTModule}

import scala.collection.mutable.ArrayBuffer


final class ArrayBufferAdapter[T <: AnyRef](listener: SimpleImageLoadingListener, convertT: T => String)
  extends ArrayBuffer[T] with ListAdapter {

  private val mDataSetObservable = new DataSetObservable()

  @Inject
  private var mInflater: LayoutInflater = null

  @Inject
  private var mImageLoader: ImageLoader = null

  @Inject
  @Named("low resolution")
  private var displayImageOptions: DisplayImageOptions = null

  TuTModule.getInjector.injectMembers(this)

  override def ++=:(xs: TraversableOnce[T]) = {
    super.++=:(xs)
    mDataSetObservable.notifyChanged()
    this
  }

  // Members declared in android.widget.Adapter
  override def getCount: Int = this.size

  override def getItem(i: Int): T = this(i)

  override def getItemId(i: Int): Long = this(i).hashCode()

  override def getItemViewType(i: Int): Int = 1

  override def getView(i: Int, convertView: View, parent: ViewGroup): View = {
    val newView = if (convertView == null) {
      mInflater.inflate(R.layout.item_image, parent, false)
    } else {
      convertView.asInstanceOf[ImageView].setImageBitmap(null)
      convertView
    }

    mImageLoader.displayImage(convertT(this(i)), newView.asInstanceOf[ImageView],
      displayImageOptions, listener)

    newView
  }

  override def getViewTypeCount: Int = 1

  override def hasStableIds: Boolean = false

  override def isEmpty: Boolean = super.isEmpty

  override def registerDataSetObserver(o: android.database.DataSetObserver): Unit = mDataSetObservable.registerObserver(o)

  override def unregisterDataSetObserver(o: android.database.DataSetObserver): Unit = mDataSetObservable.unregisterObserver(o)

  // Members declared in android.widget.ListAdapter
  override def areAllItemsEnabled(): Boolean = true

  override def isEnabled(i: Int): Boolean = true
}