package com.xi.android.kantukuang;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.xi.android.kantukuang.weibo.WeiboClient;


/**
 * An action view. Should be used to embed into the action bar
 * It inflates the {@code abc_weibo_repost.xml}
 */
public class WeiboRepostView extends LinearLayout implements View.OnClickListener, TextView.OnEditorActionListener {
    private final EditText mEditText;
    @Inject
    private LayoutInflater mInflater;
    @Inject
    private WeiboClient mWeiboClient;
    private WeiboRepostView.WeiboRepostListener mRepostListener;
    @Inject
    private InputMethodManager mInputMethodService;

    public WeiboRepostView(Context context) {
        super(context);

        KanTuKuangModule.getInjector().injectMembers(this);

        mInflater.inflate(R.layout.abc_weibo_repost, this, true);

        mEditText = (EditText) findViewById(android.R.id.edit);
        mEditText.setOnEditorActionListener(this);
        Button mButton = (Button) findViewById(android.R.id.button1);
        mButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String text = String.valueOf(mEditText.getText());

        assert mRepostListener != null;
        mRepostListener.post(text);

        mEditText.setText("");
    }

    public void setOnRepostListener(WeiboRepostListener listener) {
        mRepostListener = listener;
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            mInputMethodService.hideSoftInputFromWindow(textView.getWindowToken(), 0);
            onClick(null);

            return true;
        }

        return false;
    }

    public interface WeiboRepostListener {
        void post(String text);
    }
}
