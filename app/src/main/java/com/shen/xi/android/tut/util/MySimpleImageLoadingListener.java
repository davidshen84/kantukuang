package com.shen.xi.android.tut.util;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


public class MySimpleImageLoadingListener extends SimpleImageLoadingListener {

  private final int mMaxWidth;
  private final int mMaxHeight;

  public MySimpleImageLoadingListener(int maxWidth, int maxHeight) {
    mMaxWidth = maxWidth;
    mMaxHeight = maxHeight;
  }

  @Override
  public void onLoadingComplete(String imageUri, View view,
                                Bitmap loadedImage) {

    int imageWidth = loadedImage.getWidth();
    int imageHeight = loadedImage.getHeight();

    boolean overWidth = imageWidth > mMaxWidth;
    boolean overHeight = imageHeight > mMaxHeight;
    if (overHeight || overWidth) {
      loadedImage = Bitmap.createBitmap(loadedImage, 0, 0,
                                        overWidth ? mMaxWidth : imageWidth,
                                        overHeight ? mMaxHeight : imageHeight);
    }
    ((ImageView) view).setImageBitmap(loadedImage);

  }
}
