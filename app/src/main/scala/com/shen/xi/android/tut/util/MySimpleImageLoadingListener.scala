package com.shen.xi.android.tut.util

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener

class MySimpleImageLoadingListener(maxWidth: Int, maxHeight: Int, loadingComplete: (String, View, Bitmap) => Unit = null)
  extends SimpleImageLoadingListener {

  override def onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap): Unit = {
    val imageWidth = loadedImage.getWidth
    val imageHeight = loadedImage.getHeight

    val overWidth = imageWidth > maxWidth
    val overHeight = imageHeight > maxHeight

    if (overHeight || overWidth) {
      val modifiedImage = Bitmap.createBitmap(loadedImage, 0, 0,
        if (overWidth && maxWidth > 0) maxWidth else imageWidth,
        if (overHeight && maxHeight > 0) maxHeight else imageHeight)
      view.asInstanceOf[ImageView].setImageBitmap(modifiedImage)
    } else {
      view.asInstanceOf[ImageView].setImageBitmap(loadedImage)
    }

    if (loadingComplete != null) loadingComplete(imageUri, view, loadedImage)
  }

}
