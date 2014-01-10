package com.xi.android.kantukuang;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.inject.Inject;
import com.xi.android.kantukuang.weibo.WeiboClient;


/**
 * An action view. Should be used to embed into the action bar
 * It inflates the {@code abc_weibo_repost.xml}
 */
public class WeiboRepostView extends LinearLayout implements View.OnFocusChangeListener, View.OnClickListener {
    private static final String TAG = WeiboRepostView.class.getName();
    private final EditText mText;
    private final CharSequence mPlaceHolderText;
    @Inject
    private LayoutInflater mInflater;
    @Inject
    private WeiboClient mWeiboClient;
    private WeiboRepostView.WeiboRepostListener mRepostListener;

    public WeiboRepostView(Context context) {
        super(context);

        mPlaceHolderText = getResources().getText(
                R.string.abc_weibo_repost_placeholder);

        KanTuKuangModule.getInjector().injectMembers(this);

        mInflater.inflate(R.layout.abc_weibo_repost, this, true);

        mText = (EditText) findViewById(android.R.id.edit);
        mText.setOnFocusChangeListener(this);
        Button mButton = (Button) findViewById(android.R.id.button1);
        mButton.setOnClickListener(this);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            mText.setText("");
        } else {
            String text = String.valueOf(mText.getText());
            if (Strings.isNullOrEmpty(text)) {
                mText.setText(mPlaceHolderText);
            }
        }
    }

    @Override
    public void onClick(View view) {
        String text = String.valueOf(mText.getText());

        if (!Strings.isNullOrEmpty(text)) {
            Log.d(TAG, text);


        } else
            Log.d(TAG, "no thing ;)");

        assert mRepostListener != null;
        // TODO fix id
        mRepostListener.post(0, text);
    }

    public void setOnRepostListener(WeiboRepostListener listener) {
        mRepostListener = listener;
    }

    public interface WeiboRepostListener {
        void post(long id, String text);
    }
}
