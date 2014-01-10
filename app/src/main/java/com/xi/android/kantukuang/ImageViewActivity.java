package com.xi.android.kantukuang;

import android.net.Uri;
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

import com.google.inject.Inject;
import com.xi.android.kantukuang.weibo.WeiboClient;

import java.util.ArrayList;
import java.util.List;

public class ImageViewActivity extends ActionBarActivity implements ImageViewFragment.OnFragmentInteractionListener, WeiboRepostView.WeiboRepostListener {

    public static final String URL_LIST = "URL_LIST";
    public static final String ITEM_POSITION = "ITEM_POSITION";
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
        ArrayList<String> urlList = getIntent().getStringArrayListExtra(URL_LIST);
        int currentPosition = getIntent().getIntExtra(ITEM_POSITION, 0);
        assert urlList != null;
        mSectionsPagerAdapter.setUrlList(urlList);

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

    @Override
    public void post(long id, String text) {
        Log.d(TAG, text);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private List<String> mUrlList;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.

            return ImageViewFragment.newInstance(mUrlList.get(position));
        }

        @Override
        public int getCount() {
            return mUrlList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "title place holder";
        }

        public void setUrlList(ArrayList<String> urlList) {
            mUrlList = urlList;
        }
    }

}
