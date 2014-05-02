package com.xi.android.kantukuang;

import android.content.Intent;
import android.net.Uri;
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

import com.google.api.client.json.JsonFactory;
import com.google.inject.Inject;
import com.nostra13.universalimageloader.cache.disc.DiscCacheAware;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.squareup.otto.Bus;
import com.viewpagerindicator.UnderlinePageIndicator;

import java.io.File;


public abstract class AbstractImageViewActivity extends ActionBarActivity {
    public static final String ITEM_POSITION = "item position";
    public static final String JSON_LIST = "json list";
    @Inject
    protected Bus mBus;
    @Inject
    protected JsonFactory mJsonFactory;
    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private int mMenuId;
    private ShareActionProvider mActionProvider;
    @Inject
    private DiscCacheAware mDiscCache;

    public AbstractImageViewActivity(int menuId) {
        mMenuId = menuId;
        KanTuKuangModule.getInjector().injectMembers(this);
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    protected void setupPager(FragmentPagerAdapter pagerAdapter, int item) {
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(item);

        // set up pager indicator
        UnderlinePageIndicator indicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mViewPager);
        indicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                doEasyShare(position);
            }
        });
    }

    protected int getCurrentItem() {
        return mViewPager.getCurrentItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        setUpActionBar();
    }


    @Override
    protected void onResume() {
        super.onResume();

        mBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mBus.unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(mMenuId, menu);
        setupShareActionProvider(menu);
        // initialize easy share on load
        doEasyShare(getCurrentItem());

        return true;
    }

    /**
     * Delegate to derivative class to implement how to retrieve image url
     */
    protected abstract String getImageUrlByOrder(int order);

    private void doEasyShare(int itemOrder) {
        String imageUrl = getImageUrlByOrder(itemOrder);
        // this logic assume the user loaded the image first
        // so a copy can be found from the disk cache
        File imageFile = DiscCacheUtil.findInCache(imageUrl, mDiscCache);
        if (imageFile != null && imageFile.exists()) {

            Intent shareIntent = new Intent();

            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
            shareIntent.setType("image/*");

            mActionProvider.setShareIntent(shareIntent);
        }
    }

    private void setupShareActionProvider(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_share);

        if (item == null)
            throw new IllegalStateException();

        mActionProvider = (ShareActionProvider)
                MenuItemCompat.getActionProvider(item);
    }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class ImagePagerAdapter extends FragmentPagerAdapter {

        private final int mPageCount;

        public ImagePagerAdapter(FragmentManager fm, int pageCount) {
            super(fm);
            mPageCount = pageCount;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            return WeiboImageViewFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return mPageCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return String.format(getString(R.string.format_info_page_order), position);
        }
    }
}
