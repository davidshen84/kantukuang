package com.shen.xi.android.tut

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.ImageView
import com.google.inject.Inject
import com.nostra13.universalimageloader.core.ImageLoader
import com.shen.xi.android.tut.util.MySimpleImageLoadingListener
import com.squareup.otto.Bus
import uk.co.senab.photoview.PhotoViewAttacher

object ImageViewFragment {
  private val ARG_ORDER = "weibo status position in context"

  /**
   * create a new instance of 'ImageViewFragment'
   *
   * @param order the order of the item in its parent context
   * @return a new instance of { @link ImageViewFragment}
   */
  def newInstance(order: Int) = {
    val fragment = new ImageViewFragment()
    val bundle = new Bundle()
    bundle.putInt(ARG_ORDER, order)

    fragment.setArguments(bundle)

    fragment
  }
}

class ImageViewFragment extends Fragment {

  import com.shen.xi.android.tut.ImageViewFragment.ARG_ORDER

  @Inject
  private var mImageLoader: ImageLoader = null
  private var mPhotoViewAttacher: PhotoViewAttacher = null
  private var mOrder: Int = -1
  @Inject
  private var mBus: Bus = null


  TuTModule.getInjector.injectMembers(this)


  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)

    val arguments = getArguments
    if (arguments != null) {
      mOrder = arguments.getInt(ARG_ORDER)
    }
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    // Inflate the layout for this fragment
    val view = inflater.inflate(R.layout.image_view_full, container, false)
    val imageView = view.findViewById(android.R.id.content).asInstanceOf[ImageView]

    mImageLoader.displayImage(
      getActivity.asInstanceOf[AbstractImageViewActivity].getImageUrlByOrder(mOrder),
      imageView,
      new MySimpleImageLoadingListener(
        container.getMeasuredWidth,
        container.getMeasuredHeight,
        (_, view: View, _) => mPhotoViewAttacher = new PhotoViewAttacher(view.asInstanceOf[ImageView])
      )
    )

    view
  }

  override def onStop() = {
    if (mPhotoViewAttacher != null)
      mPhotoViewAttacher.cleanup()

    super.onStop()
  }

}
