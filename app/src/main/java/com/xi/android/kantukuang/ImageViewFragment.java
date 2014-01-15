package com.xi.android.kantukuang;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import uk.co.senab.photoview.PhotoViewAttacher;


public class ImageViewFragment extends Fragment implements PhotoViewAttacher.OnViewTapListener {
    private static final String ARG_ORDER = "weibo status position in context";
    private OnFragmentInteractionListener mListener;
    @Inject
    private ImageLoader mImageLoader;
    private PhotoViewAttacher mPhotoViewAttacher;
    private ImageViewActivity mImageViewActivity;
    private int mOrder;

    public ImageViewFragment() {
        KanTuKuangModule.getInjector().injectMembers(this);
    }

    /**
     * create a new instance of {@link com.xi.android.kantukuang.ImageViewFragment}
     *
     * @param order the order of the item in its parent context
     * @return a new instance of {@link com.xi.android.kantukuang.ImageViewFragment}
     */
    public static Fragment newInstance(int order) {
        ImageViewFragment fragment = new ImageViewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_ORDER, order);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mImageViewActivity = (ImageViewActivity) activity;
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                                                 + " must implement OnFragmentInteractionListener");
        }
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

        assert view != null;
        ImageView imageView = (ImageView) view.findViewById(android.R.id.content);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);

        mImageLoader.displayImage(mImageViewActivity.getImageUrlByOrder(mOrder), imageView,
                                  new SimpleImageLoadingListener() {
                                      @Override
                                      public void onLoadingComplete(String imageUri, View view,
                                                                    Bitmap loadedImage) {
                                          ImageView imageView = (ImageView) view;
                                          imageView.setImageBitmap(loadedImage);

                                          mPhotoViewAttacher = new PhotoViewAttacher(imageView);
                                          mPhotoViewAttacher.setOnViewTapListener(
                                                  ImageViewFragment.this);

                                      }
                                  });

        textView.setText(mImageViewActivity.getTextByOrder(mOrder));

        return view;
    }

    @Override
    public void onStop() {
        if (mPhotoViewAttacher != null)
            mPhotoViewAttacher.cleanup();

        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
        mImageViewActivity = null;
    }

    @Override
    public void onViewTap(View view, float v, float v2) {
        mListener.onImageViewFragmentInteraction(null);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onImageViewFragmentInteraction(Uri uri);
    }
}
