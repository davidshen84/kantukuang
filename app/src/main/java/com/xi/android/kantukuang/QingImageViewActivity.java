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

import static com.xi.android.kantukuang.MainActivity.ImageSource.Qing;
import static com.xi.android.kantukuang.MainActivity.ImageSource.QingPage;
import static com.xi.android.kantukuang.MainActivity.ImageSource.Unknown;


public class QingImageViewActivity extends AbstractImageViewActivity {

    public static final String QING_SOURCE = "qing mSource";
    private List<ArticleInfo> mArticleInfoList;
    private MainActivity.ImageSource mSource = Unknown;
    private List<String> mImageUrlList;

    public QingImageViewActivity() {
        super(R.menu.qing_image_view);
        KanTuKuangModule.getInjector().injectMembers(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        int item = intent.getIntExtra(ITEM_POSITION, 0);
        String sourceString = intent.getStringExtra(QING_SOURCE);
        String jsonList = intent.getStringExtra(JSON_LIST);

        if (!Strings.isNullOrEmpty(sourceString)) {
            try {
                mSource = MainActivity.ImageSource.valueOf(sourceString);
            } catch (IllegalArgumentException e) {
                mSource = Unknown;
            }
        }

        // TODO create different adapter, depending on the source
        ImagePagerAdapter imagePagerAdapter = null;
        if (mSource == Qing) {
            try {
                JsonParser jsonParser = mJsonFactory.createJsonParser(jsonList);
                mArticleInfoList = (List<ArticleInfo>) jsonParser.parseArray(List.class, ArticleInfo.class);
                imagePagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), mArticleInfoList.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (mSource == QingPage) {
            try {
                JsonParser jsonParser = mJsonFactory.createJsonParser(jsonList);
                mImageUrlList = (List<String>) jsonParser.parseArray(List.class, String.class);
                imagePagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), mImageUrlList.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (imagePagerAdapter != null)
            setupPager(imagePagerAdapter, item);
        else {
            finish();
            return;
        }

        // set up ads
        AdView adView = (AdView) findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder()
                .addTestDevice("3D3B40496EA6FF9FDA8215AEE90C0808")
                .build());
    }

    @Override
    protected String getImageUrlByOrder(int order) {
        return mSource == Qing ? mArticleInfoList.get(order).imageSrc.replace("mw205", "mw600") : mImageUrlList.get(order);
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
