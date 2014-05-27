package com.shen.xi.android.tut;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.api.client.json.JsonFactory;
import com.google.inject.Inject;
import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.squareup.otto.Bus;
import com.viewpagerindicator.UnderlinePageIndicator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public abstract class AbstractImageViewActivity extends ActionBarActivity {
    public static final String ITEM_POSITION = "item position";
    public static final String JSON_LIST = "json list";
    private static final String TAG = AbstractImageViewActivity.class.getName();
    private final ImageSaver mImageSaver = new ImageSaver();
    private final WallpaperSaver mWallpaperSaver = new WallpaperSaver();
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
    private DiskCache mDiscCache;
    @Inject
    private ImageLoader mImageLoader;

    public AbstractImageViewActivity(int menuId) {
        mMenuId = menuId;
        TuTModule.getInjector().injectMembers(this);
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

        // set up ads
        AdView adView = (AdView) findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder()
                              .addTestDevice("3D3B40496EA6FF9FDA8215AEE90C0808")
                              .build());
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save_image:
                mImageLoader.loadImage(getImageUrlByOrder(getCurrentItem()), mImageSaver);
                return true;

            case R.id.action_set_wallpaper:
                mImageLoader.loadImage(getImageUrlByOrder(getCurrentItem()),
                                       mWallpaperSaver);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Delegate to derivative class to implement how to retrieve image url
     */
    protected abstract String getImageUrlByOrder(int order);

    private void doEasyShare(int itemOrder) {
        String imageUrl = getImageUrlByOrder(itemOrder);
        // this logic assume the user loaded the image first
        // so a copy can be found from the disk cache
        File imageFile = DiskCacheUtils.findInCache(imageUrl, mDiscCache);
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

    protected File getImageDirectory() {
        File tutDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "TuT");

        if (!tutDir.isDirectory()) {
            if (!tutDir.delete()) {
                Log.w(TAG, String.format("cannot remove %s", tutDir.toString()));
                return null;
            }
            if (!tutDir.mkdir()) {
                Log.w(TAG, String.format("cannot create %s", tutDir.toString()));
                return null;
            }
        }

        return tutDir;
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
            return ImageViewFragment.newInstance(position);
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

    /**
     * A {@link com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener}
     * that saves the image to external storage
     */
    private class ImageSaver extends SimpleImageLoadingListener {

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            saveImage(imageUri, loadedImage);
            Toast.makeText(AbstractImageViewActivity.this, R.string.message_info_success,
                           Toast.LENGTH_SHORT).show();
        }

        private void saveImage(String imageUri, Bitmap loadedImage) {
            File tutDir = getImageDirectory();
            if (tutDir != null) {
                try {
                    File file = new File(tutDir, String.format("%s.png", Integer.toHexString(
                            imageUri.hashCode())));
                    FileOutputStream stream = new FileOutputStream(file);
                    loadedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    Toast.makeText(AbstractImageViewActivity.this, R.string.message_info_success, Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(AbstractImageViewActivity.this, R.string.message_error_save_image,
                               Toast.LENGTH_LONG).show();
            }
        }

    }

    /**
     * A {@link com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener}
     * that set the image as wallpaper
     */
    private class WallpaperSaver extends SimpleImageLoadingListener {
        @Override
        public void onLoadingComplete(String imageUri, View view,
                                      Bitmap loadedImage) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            loadedImage.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            try {
                WallpaperManager.getInstance(AbstractImageViewActivity.this).setStream(
                        new ByteArrayInputStream(outputStream.toByteArray())
                );
                Toast.makeText(AbstractImageViewActivity.this, R.string.message_info_success, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
