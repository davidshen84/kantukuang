package com.shen.xi.android.tut;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.api.client.json.JsonParser;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.shen.xi.android.tut.sinablog.ArticleInfo;

import java.io.IOException;
import java.util.List;

import static com.shen.xi.android.tut.MainActivity.ImageSource.QingTag;
import static com.shen.xi.android.tut.MainActivity.ImageSource.Unknown;


public class QingImageViewActivity extends AbstractImageViewActivity {

    public static final String QING_SOURCE = "qing source";
    private List<ArticleInfo> mArticleInfoList;
    private MainActivity.ImageSource mSource = Unknown;
    private List<String> mImageUrlList;

    public QingImageViewActivity() {
        super(R.menu.qing_image_view);
        TuTModule.getInjector().injectMembers(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        int item = extras.getInt(ITEM_POSITION, 0);
        String sourceString = extras.getString(QING_SOURCE);
        String jsonList = extras.getString(JSON_LIST);

        if (!Strings.isNullOrEmpty(sourceString)) {
            try {
                mSource = MainActivity.ImageSource.valueOf(sourceString);
            } catch (IllegalArgumentException e) {
                mSource = Unknown;
            }
        }

        ImagePagerAdapter imagePagerAdapter;
        int size = 0;
        try {
            JsonParser jsonParser = mJsonFactory.createJsonParser(jsonList);
            switch (mSource) {
                case QingTag:
                    mArticleInfoList = (List<ArticleInfo>)
                            jsonParser.parseArray(List.class, ArticleInfo.class);
                    size = mArticleInfoList.size();
                    break;

                case QingPage:
                    mImageUrlList = (List<String>) jsonParser.parseArray(List.class, String.class);
                    size = mImageUrlList.size();
                    break;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (size != 0) {
            imagePagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), size);
            setupPager(imagePagerAdapter, item);
        } else {
            finish();
        }
    }

    @Override
    protected String getImageUrlByOrder(int order) {
        return mSource == QingTag
                // switch to the high-def version
                ? mArticleInfoList.get(order).imageSrc.replace("mw205", "mw600")
                : mImageUrlList.get(order);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }


}
