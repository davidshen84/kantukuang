package com.xi.android.kantukuang;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.inject.Inject;
import com.xi.android.kantukuang.event.FilterStatusEvent;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboStatus;

import java.io.IOException;
import java.util.List;


public class ImageViewActivity extends AbstractImageViewActivity {

    public static final String ITEM_POSITION = "item position";
    public static final String STATUS_JSON = "weibo status in json";
    private static final String TAG = ImageViewActivity.class.getName();
    private final FilterStatusEvent mFilterStatusEvent = new FilterStatusEvent();
    private List<WeiboStatus> mStatusList;

    @Inject
    private WeiboClient weiboClient;
    @Inject
    private JsonFactory mJsonFactory;

    public ImageViewActivity() {
        super(R.menu.image_view);

        KanTuKuangModule.getInjector().injectMembers(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        Intent intent = getIntent();
        int item = intent.getIntExtra(ITEM_POSITION, 0);
        String json = intent.getStringExtra(STATUS_JSON);

        try {
            JsonParser jsonParser = mJsonFactory.createJsonParser(json);
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
        WeiboPagerAdapter pagerAdapter = new WeiboPagerAdapter(
                getSupportFragmentManager(), mStatusList);
        setupPager(pagerAdapter, item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFilterStatusEvent.shouldFilter = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        mBus.post(mFilterStatusEvent);
    }

    @Override
    public String getImageUrlByOrder(int order) {
        return mStatusList.get(order).getImageUrl();
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
                WeiboStatus status = mStatusList.get(getCurrentItem());
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
}
