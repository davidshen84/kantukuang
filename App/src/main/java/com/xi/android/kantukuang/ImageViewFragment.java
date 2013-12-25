package com.xi.android.kantukuang;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import uk.co.senab.photoview.PhotoViewAttacher;


public class ImageViewFragment extends Fragment {
    private static final String ARG_IMAGE_URL = "image url";
    private OnFragmentInteractionListener mListener;
    private ImageLoader mImageLoader;
    private String mImageUrl;
    private PhotoViewAttacher mPhotoViewAttacher;

    public ImageViewFragment() {
        // Required empty public constructor
    }

    /**
     * @param imageUrl The url for the image
     * @return A new instance
     */
    public static Fragment newInstance(String imageUrl) {
        ImageViewFragment fragment = new ImageViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_IMAGE_URL, imageUrl);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mImageUrl = arguments.getString(ARG_IMAGE_URL);
        }

        mImageLoader = ((MyApplication) getActivity().getApplication()).getImageLoader();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mPhotoViewAttacher.cleanup();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image_view, container, false);

        assert view != null;
        ImageView imageView = (ImageView) view.findViewById(android.R.id.content);

        mImageLoader.displayImage(mImageUrl, imageView, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                ImageView imageView = (ImageView) view;
                imageView.setImageBitmap(loadedImage);

                mPhotoViewAttacher = new PhotoViewAttacher(imageView);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                                                 + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
