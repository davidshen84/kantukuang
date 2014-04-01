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
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.inject.Inject;
import com.nostra13.universalimageloader.cache.disc.DiscCacheAware;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.squareup.otto.Bus;
import com.viewpagerindicator.UnderlinePageIndicator;
import com.xi.android.kantukuang.event.FilterStatusEvent;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboStatus;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class ImageViewActivity extends ActionBarActivity {

    public static final String ITEM_POSITION = "item position";
    public static final String STATUS_JSON = "weibo status in json";
    public static final String PREF_BLACKLIST = "blacklist set";
    private static final String TAG = ImageViewActivity.class.getName();
    private final FilterStatusEvent mFilterStatusEvent = new FilterStatusEvent();
    @Inject
    DiscCacheAware mDiscCache;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    @Inject
    private WeiboClient weiboClient;
    @Inject
    private JsonFactory mJsonFactory;
    private List<WeiboStatus> mStatusList;
    @Inject
    private Bus mBus;
    private ShareActionProvider mActionProvider;

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
        /*
      The {@link android.support.v4.view.PagerAdapter} that will provide
      fragments for each of the sections. We use a
      {@link FragmentPagerAdapter} derivative, which will keep every
      loaded fragment in memory. If this becomes too memory intensive, it
      may be best to switch to a
      {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager(), mStatusList);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(currentPosition);

        // set up pager indicator
        UnderlinePageIndicator indicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mViewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFilterStatusEvent.shouldFilter = false;
        mBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mBus.post(mFilterStatusEvent);
        mBus.unregister(this);
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_view, menu);

        // get current image url
        String imageUrl = mStatusList.get(mViewPager.getCurrentItem()).imageUrl;
        // this logic assume the user loaded the image first
        // so a copy can be found from the disk cache
        File imageFile = DiscCacheUtil.findInCache(imageUrl, mDiscCache);
        if (imageFile.exists()) {
            MenuItem item = menu.findItem(R.id.action_share);
            Intent shareIntent = new Intent();
            mActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
            shareIntent.setType("image/*");

            mActionProvider.setShareIntent(shareIntent);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_settings:
                return true;

            case android.R.id.home:
                this.finish();
                return true;

            case R.id.action_weibo_add_blacklist:
                long uid;
                WeiboStatus status = mStatusList.get(mViewPager.getCurrentItem());
                if (status.repostedStatus != null) {
                    uid = status.repostedStatus.uid;
                } else {
                    uid = status.uid;
                }

                blockAccount(uid);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void blockAccount(long uid) {

        new AsyncTask<Long, Long, Long>() {

            @Override
            protected Long doInBackground(Long... longs) {
                BlacklistSQLiteOpenHelper sqLiteOpenHelper =
                        new BlacklistSQLiteOpenHelper(ImageViewActivity.this);

                Long uid = longs[0];
                sqLiteOpenHelper.insert(uid);
                sqLiteOpenHelper.close();

                return uid;
            }

            @Override
            protected void onPostExecute(Long uid) {
                String text = String.format(getString(R.string.format_info_add_blacklist), uid);
                Toast.makeText(ImageViewActivity.this, text, Toast.LENGTH_SHORT).show();
                mFilterStatusEvent.shouldFilter = true;
            }

        }.execute(uid);

    }

    public String getImageUrlByOrder(int order) {
        return mStatusList.get(order).getImageUrl();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<WeiboStatus> mStatusList;

        public SectionsPagerAdapter(FragmentManager fm, List<WeiboStatus> statusList) {
            super(fm);

            mStatusList = statusList;
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
            return String.format(getString(R.string.format_info_page_order), position);
        }
    }
}
