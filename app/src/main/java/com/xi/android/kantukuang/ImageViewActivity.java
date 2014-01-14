package com.xi.android.kantukuang;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.inject.Inject;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboStatus;

import java.io.IOException;
import java.util.List;

public class ImageViewActivity extends ActionBarActivity implements ImageViewFragment.OnFragmentInteractionListener, WeiboRepostView.WeiboRepostListener {

    public static final String ITEM_POSITION = "item position";
    public static final String STATUS_JSON = "weibo status in json";
    private static final String TAG = ImageViewActivity.class.getName();
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    @Inject
    private WeiboClient weiboClient;
    private String mCurrentStatusId;
    @Inject
    private JsonFactory mJsonFactory;
    private int mSelectedViewPosition;
    private List<WeiboStatus> mStatusList;

    public ImageViewActivity() {
        KanTuKuangModule.getInjector().injectMembers(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        setUpActionBar();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        Intent intent = getIntent();
        int currentPosition = intent.getIntExtra(ITEM_POSITION, 0);

        try {
            String stringExtra = intent.getStringExtra(STATUS_JSON);
            JsonParser jsonParser = mJsonFactory.createJsonParser(stringExtra);
            mStatusList = (List<WeiboStatus>) jsonParser.parseArray(List.class, WeiboStatus.class);
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
        mSectionsPagerAdapter.setStatusList(mStatusList);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(currentPosition);
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_view, menu);

        // set up repost listener
        MenuItem menuItem = menu.findItem(R.id.action_weibo_repost);
        WeiboRepostView weiboRepostView = (WeiboRepostView) MenuItemCompat.getActionView(menuItem);
        weiboRepostView.setOnRepostListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImageViewFragmentInteraction(Uri uri) {
        Log.d(TAG, "nop");
    }

    public String getImageUrlByOrder(int position) {
        return mStatusList.get(position).getImageUrl();
    }

    @Override
    public void post(String text) {
        String id = mStatusList.get(mViewPager.getCurrentItem()).id;

        Log.d(TAG, String.format("%s: %s", id, text));
        new AsyncTask<String, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {
                return weiboClient.repost(strings[0], strings[1]) != null;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    Toast.makeText(ImageViewActivity.this, R.string.message_success,
                                   Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ImageViewActivity.this, R.string.message_fail,
                                   Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(id, text);

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private List<WeiboStatus> mStatusList;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            return ImageViewFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return mStatusList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "title place holder";
        }

        public void setStatusList(List<WeiboStatus> statuses) {
            mStatusList = statuses;
        }
    }
}
