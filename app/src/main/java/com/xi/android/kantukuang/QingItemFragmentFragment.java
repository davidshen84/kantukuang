package com.xi.android.kantukuang;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.inject.Inject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.xi.android.kantukuang.sinablog.ArticleInfo;
import com.xi.android.kantukuang.sinablog.QingClient;

import java.util.ArrayList;
import java.util.List;

public class QingItemFragmentFragment extends Fragment implements AbsListView.OnItemClickListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TAG = "Qing Tag";

    private String mQingTag;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter mAdapter;
    private QingClient qingClient;
    private List<ArticleInfo> articleInfoList = new ArrayList<ArticleInfo>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public QingItemFragmentFragment() {
    }

    public static QingItemFragmentFragment newInstance(String tag) {
        QingItemFragmentFragment fragment = new QingItemFragmentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, tag);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mQingTag = getArguments().getString(ARG_TAG);
        }

        qingClient = QingClient.createForTag(mQingTag);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qingitemfragment, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mAdapter = new ArticleInfoArrayAdapter(getActivity(), articleInfoList);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // TODO: display waite message
        new AsyncTask<String, String, List<ArticleInfo>>() {
            @Override
            protected List<ArticleInfo> doInBackground(String... strings) {
                qingClient.load();

                return qingClient.hasLoaded() ? qingClient.getArticleInfoList() : null;
            }

            @Override
            protected void onPostExecute(List<ArticleInfo> articleInfos) {
                if (articleInfos != null && articleInfos.size() > 0) {
                    articleInfoList.addAll(articleInfos);
                    mAdapter.notifyDataSetChanged();
                } else {
                    throw new IllegalStateException();
                }
            }
        }.execute();

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    private class ArticleInfoArrayAdapter extends ArrayAdapter<ArticleInfo> {
        private final List<ArticleInfo> list;
        private final DisplayMetrics mDisplayMetrics;
        @Inject
        private LayoutInflater mInflater;
        @Inject
        private ImageLoader mImageLoader;

        public ArticleInfoArrayAdapter(Context context, List<ArticleInfo> list) {
            super(context, R.layout.fragment_qingitemfragment, list);
            this.list = list;

            KanTuKuangModule.getInjector().injectMembers(this);
            mDisplayMetrics = context.getResources().getDisplayMetrics();
        }

        @Override
        public ArticleInfo getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_image, parent, false);
            }

            mImageLoader.displayImage(getItem(position).imageSrc, (ImageView) convertView);

            return convertView;
        }
    }
}
