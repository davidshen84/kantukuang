package com.xi.android.kantukuang;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboTimelineAsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


public class ItemFragment extends Fragment implements AbsListView.OnItemClickListener, LoaderManager.LoaderCallbacks<List<String>>, OnRefreshListener {

    private static final int FORCE_TOP_PADDING = 256;
    private static final String ARG_TAG = "tag";
    private static final String ARG_ACCESS_TOKEN = "access token";
    private static final String TAG = ItemFragment.class.getName();
    private OnFragmentInteractionListener mFragmentInteractionListener;
    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;
    private WeiboClient mWeiboClient;
    private ArrayAdapter<String> mWeiboItemViewArrayAdapter;
    private List<String> mImageUrlArrayList = new ArrayList<String>();
    private String mTag;
    private View mEmptyView;
    private PullToRefreshLayout mPullToRefreshLayout;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
        Injector mInjector = KanTuKuangModule.getInjector();
        mWeiboClient = mInjector.getInstance(WeiboClient.class);
    }

    public static ItemFragment newInstance(String tag, String accessToken) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, tag);
        args.putString(ARG_ACCESS_TOKEN, accessToken);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Loader<Object> loader = getLoaderManager().getLoader(0);
        if (loader != null && loader.isStarted())
            loader.abandon();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();

        if (!mWeiboClient.IsAuthenticated()) {
            Log.v(TAG, "weibo client is not authenticated.");
            Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStackImmediate();

            return;
        }

        if (arguments != null) {
            mTag = arguments.getString(ARG_TAG);
            ((MainActivity) getActivity()).onSectionAttached(mTag);
        }

        getLoaderManager().initLoader(0, arguments, this);
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
                                                                   mImageUrlArrayList);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mWeiboItemViewArrayAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        ActionBarPullToRefresh
                .from(getActivity())
                .theseChildrenArePullable(mListView)
                .listener(this)
                .setup(mPullToRefreshLayout);

        if (mImageUrlArrayList.size() > 0)
            setEmptyText(null);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            ((RefreshEventDispatcher) activity).registerOnRefreshListener(this);
            mFragmentInteractionListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mFragmentInteractionListener = null;
        ((RefreshEventDispatcher) getActivity()).unregisterOnRefreshListener();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mFragmentInteractionListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.

            String url = (String) parent.getItemAtPosition(position);
            mFragmentInteractionListener.onItemFragmentInteraction(url);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        if (emptyText == null && mEmptyView != null) {
            mEmptyView.setVisibility(View.GONE);
        } else if (mEmptyView instanceof TextView) {
            ((TextView) mEmptyView).setText(emptyText);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Loader<List<String>> onCreateLoader(int i, Bundle bundle) {
        Log.v(TAG, "created new loader");
        String tag = bundle.getString(ARG_TAG);

        return new WeiboTimelineAsyncTaskLoader(getActivity(), mWeiboClient, tag);
    }

    @Override
    public void onLoadFinished(Loader<List<String>> listLoader, List<String> strings) {

        WeiboTimelineAsyncTaskLoader loader = (WeiboTimelineAsyncTaskLoader) listLoader;
        if (loader.takeHasError()) {
            // display error message
            Toast.makeText(getActivity(), R.string.loadErrorMessage, Toast.LENGTH_SHORT).show();
        } else {
            int newDataCount = loader.takeNewDataCount();
            if (newDataCount > 0) {
                Toast.makeText(getActivity(),
                               getResources().getString(R.string.newDataFormat, newDataCount),
                               Toast.LENGTH_SHORT)
                        .show();

                // insert data to top
                mImageUrlArrayList.addAll(0, strings);
                mWeiboItemViewArrayAdapter.notifyDataSetChanged();

                // update view state
                mListView.setVisibility(View.VISIBLE);
                setEmptyText(null);
            }
        }

        if (mListView.getCount() == 0) {
            // display empty view
            setEmptyText(getResources().getString(R.string.emptyListMessage));
            mListView.setVisibility(View.INVISIBLE);
        }

        // stop loading animation in action bar
        mPullToRefreshLayout.setRefreshComplete();

    }

    @Override
    public void onLoaderReset(Loader<List<String>> listLoader) {
        Log.v(TAG, "reset loader");
        listLoader.reset();
    }

    @Override
    public void onRefreshStarted(View view) {
        if (!mPullToRefreshLayout.isRefreshing())
            mPullToRefreshLayout.setRefreshing(true);

        getLoaderManager().getLoader(0).onContentChanged();
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
        public void onItemFragmentInteraction(String position);
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
            displayImageOptions = new DisplayImageOptions.Builder()
                    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
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
