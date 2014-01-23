package com.xi.android.kantukuang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.client.util.Sets;
import com.google.inject.Inject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.viewpagerindicator.UnderlinePageIndicator;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboStatus;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.xi.android.kantukuang.ImageViewFragment.TapImageEvent;
import static com.xi.android.kantukuang.ItemFragment.FilterStatusEvent;
import static com.xi.android.kantukuang.RepostStatusFragment.RepostStatusEvent;

public class ImageViewActivity extends ActionBarActivity {

    public static final String ITEM_POSITION = "item position";
    public static final String STATUS_JSON = "weibo status in json";
    public static final String PREF_BLACKLIST = "blacklist set";
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
    @Inject
    private JsonFactory mJsonFactory;
    private List<WeiboStatus> mStatusList;
    @Inject
    private Bus mBus;
    private FilterStatusEvent mFilterStatusEvent = new FilterStatusEvent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        KanTuKuangModule.getInjector().injectMembers(this);
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
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), mStatusList);

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
            case R.id.action_weibo_add_blacklist:
                mFilterStatusEvent.shouldFilter = true;
                WeiboStatus status = mStatusList.get(mViewPager.getCurrentItem());
                long uid;
                if (status.repostedStatus != null)
                    uid = status.repostedStatus.uid;
                else
                    uid = status.uid;

                addUidToBlackList(uid);
                Toast.makeText(this, R.string.message_info_add_blacklist,
                               Toast.LENGTH_SHORT).show();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void addUidToBlackList(long uid) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Set<Long> blackList = Sets.newHashSet();
        String blackListJson;

        blackListJson = sp.getString(PREF_BLACKLIST, "");
        try {
            if (!Strings.isNullOrEmpty(blackListJson)) {
                JsonParser jsonParser = mJsonFactory.createJsonParser(blackListJson);
                jsonParser.parseArray(blackList, Long.class);
            }

            blackList.add(uid);
            sp.edit()
                    .putString(PREF_BLACKLIST, mJsonFactory.toString(blackList))
                    .commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getImageUrlByOrder(int order) {
        return mStatusList.get(order).getImageUrl();
    }

    @Subscribe
    public void tapImage(TapImageEvent event) {
        WeiboStatus status = mStatusList.get(event.order);
        boolean hasFragment =
                getSupportFragmentManager().findFragmentById(R.id.fragment_repost) != null;

        if (hasFragment) {
            getSupportFragmentManager().popBackStack();
        } else {
            RepostStatusFragment fragment = RepostStatusFragment
                    .newInstance(status.id, status.text);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_repost, fragment)
                    .addToBackStack("REPOST")
                    .commit();
        }
    }

    @Subscribe
    public void repostStatus(RepostStatusEvent event) {

        Log.d(TAG, String.format("%s: %s", event.statusId, event.text));
        new AsyncTask<String, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {
                return weiboClient.repost(strings[0], strings[1]) != null;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    Toast.makeText(ImageViewActivity.this, R.string.message_info_success,
                                   Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ImageViewActivity.this, R.string.message_error_fail,
                                   Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(event.statusId, event.text);

    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private List<WeiboStatus> mStatusList;

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
