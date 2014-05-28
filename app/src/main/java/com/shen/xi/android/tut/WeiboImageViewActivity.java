package com.shen.xi.android.tut;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.api.client.json.JsonParser;
import com.google.inject.Inject;
import com.shen.xi.android.tut.weibo.WeiboClient;

import java.io.IOException;
import java.util.List;


public class WeiboImageViewActivity extends AbstractImageViewActivity {
    private static final String TAG = WeiboImageViewActivity.class.getName();
    private List<String> mStatusList;
    @Inject
    private WeiboClient weiboClient;

    public WeiboImageViewActivity() {
        super(R.menu.weibo_image_view);
        TuTModule.getInjector().injectMembers(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        Intent intent = getIntent();
        int item = intent.getIntExtra(ITEM_POSITION, 0);
        String jsonList = intent.getStringExtra(JSON_LIST);

        try {
            JsonParser jsonParser = mJsonFactory.createJsonParser(jsonList);
            mStatusList = (List<String>) jsonParser.parseArray(List.class, String.class);
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
        ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(
                getSupportFragmentManager(), mStatusList.size());
        setupPager(pagerAdapter, item);
    }

    @Override
    public String getImageUrlByOrder(int order) {
        return mStatusList.get(order);
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

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
