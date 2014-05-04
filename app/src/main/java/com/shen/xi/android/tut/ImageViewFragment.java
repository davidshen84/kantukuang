package com.shen.xi.android.tut;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.inject.Inject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.squareup.otto.Bus;

import uk.co.senab.photoview.PhotoViewAttacher;


public class ImageViewFragment extends Fragment {
    private static final String ARG_ORDER = "weibo status position in context";
    @Inject
    private ImageLoader mImageLoader;
    private PhotoViewAttacher mPhotoViewAttacher;
    private int mOrder;
    @Inject
    private Bus mBus;

    public ImageViewFragment() {
        TuTModule.getInjector().injectMembers(this);
    }

    /**
     * create a new instance of {@link ImageViewFragment}
     *
     * @param order the order of the item in its parent context
     * @return a new instance of {@link ImageViewFragment}
     */
    public static Fragment newInstance(int order) {
        ImageViewFragment fragment = new ImageViewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_ORDER, order);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mOrder = arguments.getInt(ARG_ORDER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.image_view_full, container, false);

        ImageView imageView = (ImageView) view.findViewById(android.R.id.content);

        mImageLoader.displayImage(((AbstractImageViewActivity) getActivity()).getImageUrlByOrder(
                                          mOrder), imageView,
                                  new SimpleImageLoadingListener() {
                                      @Override
                                      public void onLoadingComplete(String imageUri, View view,
                                                                    Bitmap loadedImage) {
                                          ImageView imageView = (ImageView) view;
                                          imageView.setImageBitmap(loadedImage);

                                          mPhotoViewAttacher = new PhotoViewAttacher(imageView);
                                      }
                                  }
        );

        return view;
    }

    @Override
    public void onStop() {
        if (mPhotoViewAttacher != null)
            mPhotoViewAttacher.cleanup();

        super.onStop();
    }

}
