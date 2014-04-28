package com.xi.android.kantukuang;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.api.client.json.JsonParser;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.xi.android.kantukuang.sinablog.ArticleInfo;

import java.io.IOException;
import java.util.List;

import static com.xi.android.kantukuang.MainActivity.SelectEventSource;
import static com.xi.android.kantukuang.MainActivity.SelectEventSource.Qing;
import static com.xi.android.kantukuang.MainActivity.SelectEventSource.Unknown;


public class QingImageViewActivity extends AbstractImageViewActivity {

    private static final String QING_SOURCE = "qing source";
    private List<ArticleInfo> mArticleInfos;

    public QingImageViewActivity() {
        super(R.menu.qing_image_view);
        KanTuKuangModule.getInjector().injectMembers(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SelectEventSource selectEventSource = Unknown;
        Intent intent = getIntent();

        int item = intent.getIntExtra(ITEM_POSITION, 0);
        String sourceString = intent.getStringExtra(QING_SOURCE);
        String jsonList = intent.getStringExtra(JSON_LIST);

        if (!Strings.isNullOrEmpty(sourceString)) {
            try {
                selectEventSource = SelectEventSource.valueOf(sourceString);
            } catch (IllegalArgumentException e) {
                selectEventSource = Unknown;
            }
        }

        // TODO create different adapter, depending on the source
        ImagePagerAdapter imagePagerAdapter = null;
        if (selectEventSource == Unknown || selectEventSource == Qing) {
            try {
                JsonParser jsonParser = mJsonFactory.createJsonParser(jsonList);
                mArticleInfos = (List<ArticleInfo>) jsonParser.parseArray(List.class, ArticleInfo.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            imagePagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), mArticleInfos.size());
        }
        setupPager(imagePagerAdapter, item);

        // set up ads
        AdView adView = (AdView) findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder()
                              .addTestDevice("3D3B40496EA6FF9FDA8215AEE90C0808")
                              .build());
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
