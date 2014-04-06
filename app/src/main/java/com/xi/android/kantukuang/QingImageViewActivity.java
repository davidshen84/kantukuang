package com.xi.android.kantukuang;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.inject.Inject;
import com.xi.android.kantukuang.sinablog.ArticleInfo;

import java.io.IOException;
import java.util.List;


public class QingImageViewActivity extends AbstractImageViewActivity {

    @Inject
    private JsonFactory mJsonFactory;
    private List<ArticleInfo> mArticleInfos;

    public QingImageViewActivity() {
        super(R.menu.qing_image_view);
        KanTuKuangModule.getInjector().injectMembers(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int item = intent.getIntExtra(ITEM_POSITION, 0);
        String jsonList = intent.getStringExtra(JSON_LIST);

        try {
            JsonParser jsonParser = mJsonFactory.createJsonParser(jsonList);
            mArticleInfos = (List<ArticleInfo>) jsonParser.parseArray(List.class, ArticleInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), mArticleInfos.size());

        setupPager(imagePagerAdapter, item);
    }

    @Override
    protected String getImageUrlByOrder(int order) {
        return mArticleInfos.get(order).imageSrc;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
