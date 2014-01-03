package com.xi.android.kantukuang;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.xi.android.kantukuang.weibo.WeiboClient;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


public class ItemFragment extends Fragment implements AbsListView.OnItemClickListener, OnRefreshListener {

    public static final String ARG_TAG = "tag";
    private static final int FORCE_TOP_PADDING = 256;
    private static final String TAG = ItemFragment.class.getName();
    private static final String ARG_ID = "id";
    private OnFragmentInteractionListener mFragmentInteractionListener;
    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;
    private WeiboClient mWeiboClient;
    private ArrayAdapter<String> mWeiboItemViewArrayAdapter;
    private List<String> mImageUrlList = new ArrayList<String>();
    private View mEmptyView;
    private PullToRefreshLayout mPullToRefreshLayout;
    private MainActivity mActivity;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
        Injector mInjector = KanTuKuangModule.getInjector();
        mWeiboClient = mInjector.getInstance(WeiboClient.class);
    }

    public static ItemFragment newInstance(String tag, int id) {
        ItemFragment fragment = new ItemFragment();
        fragment.setRetainInstance(true);

        Bundle args = new Bundle();
        args.putString(ARG_TAG, tag);
        args.putInt(ARG_ID, id);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (MainActivity) activity;
        try {
            mFragmentInteractionListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle mArguments = getArguments();
        if (mArguments != null) {
            String tag = mArguments.getString(ARG_TAG);
            int id = mArguments.getInt(ARG_ID);
            mActivity.onSectionAttached(id);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        // Set the adapter
        assert view != null;
        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.ptr_layout);
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mEmptyView = view.findViewById(android.R.id.empty);
        // set up data adapter
        mWeiboItemViewArrayAdapter = new WeiboItemViewArrayAdapter(getActivity(),
                                                                   mImageUrlList);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mWeiboItemViewArrayAdapter);

        // update empty view
        if (mImageUrlList.size() > 0) {
            setEmptyText(null);
        }

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO wrong logic :(
        if (!mWeiboClient.IsAuthenticated()) {
            Log.v(TAG, "weibo client is not authenticated.");
            Toast.makeText(mActivity, "error", Toast.LENGTH_SHORT).show();
            mActivity.finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // set up pull to refresh widget
        ActionBarPullToRefresh
                .from(mActivity)
                .theseChildrenArePullable(mListView)
                .listener(this)
                .setup(mPullToRefreshLayout);
    }

    @Override
    public void onResume() {
        super.onResume();


        // trigger refresh
        if (mImageUrlList.size() == 0) {
//            mPullToRefreshLayout.setRefreshing(true);
            mActivity.forceLoad();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // clean ref. to activity
        mFragmentInteractionListener = null;
        mActivity = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mFragmentInteractionListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.

            mFragmentInteractionListener.onItemFragmentInteraction(position);
        }
    }

    private void setEmptyText(CharSequence emptyText) {
        assert mEmptyView != null && mEmptyView instanceof TextView;
        if (emptyText == null) {
            mEmptyView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        } else {
            ((TextView) mEmptyView).setText(emptyText);
            mEmptyView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onRefreshStarted(View view) {
        if (!mPullToRefreshLayout.isRefreshing())
            mPullToRefreshLayout.setRefreshing(true);

        mActivity.refreshLoader();
    }

    public void onRefreshComplete(List<String> itemList) {
        if (itemList != null && itemList.size() > 0) {
            mImageUrlList.addAll(0, itemList);
            mWeiboItemViewArrayAdapter.notifyDataSetChanged();

            setEmptyText(mImageUrlList.size() == 0 ? getResources().getString(
                    R.string.message_info_empty_list) : null);
        }

        mPullToRefreshLayout.setRefreshComplete();
    }

    public ArrayList<String> getItemArrayList() {
        return (ArrayList<String>) mImageUrlList;
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
        public void onItemFragmentInteraction(int position);
    }

    private class WeiboItemViewArrayAdapter extends ArrayAdapter<String> {
        private final DisplayImageOptions displayImageOptions;
        private final LayoutInflater mInflater;
        private final ImageLoader mImageLoader;

        public WeiboItemViewArrayAdapter(Context context, List<String> strings) {
            super(context, R.layout.fragment_item_image_view, strings);

            mInflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mImageLoader = ((MyApplication) getActivity().getApplication()).getImageLoader();
            BitmapFactory.Options bitmapOptions = KanTuKuangModule.getInjector()
                    .getInstance(Key.get(BitmapFactory.Options.class));


            displayImageOptions = new DisplayImageOptions.Builder()
                    .decodingOptions(bitmapOptions)
                    .cacheOnDisc(true)
                    .build();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = mInflater.inflate(
                        R.layout.fragment_item_image_view, container, false);
            } else {
                ((ImageView) convertView).setImageBitmap(null);
            }

            mImageLoader.displayImage(getItem(position), (ImageView) convertView,
                                      displayImageOptions,
                                      new SimpleImageLoadingListener() {
                                          @Override
                                          public void onLoadingComplete(String imageUri,
                                                                        View view,
                                                                        Bitmap loadedImage) {
                                              if (loadedImage.getHeight() / 2 < FORCE_TOP_PADDING) {
                                                  view.setPadding(0, 0, 0, 0);
                                              } else {
                                                  view.setPadding(0, FORCE_TOP_PADDING, 0,
                                                                  0);
                                              }
                                          }
                                      });

            return convertView;
        }
    }
}
