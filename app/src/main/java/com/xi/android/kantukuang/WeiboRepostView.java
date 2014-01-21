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
import com.squareup.otto.Bus;
import com.xi.android.kantukuang.weibo.WeiboClient;


/**
 * An action view. Should be used to embed into the action bar
 * It inflates the {@code abc_weibo_repost.xml}
 */
public class WeiboRepostView extends LinearLayout implements View.OnClickListener, TextView.OnEditorActionListener {
    private final EditText mEditText;
    private final WeiboRepostView.RepostStatusEvent mRepostStatusEvent = new RepostStatusEvent();
    @Inject
    private Bus mBus;
    @Inject
    private LayoutInflater mInflater;
    @Inject
    private WeiboClient mWeiboClient;
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
        postStatus();
    }

    private void postStatus() {
        mRepostStatusEvent.text = getText();
        mBus.post(mRepostStatusEvent);
        mInputMethodService.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    public String getText() {
        return String.valueOf(mEditText.getText());
    }

    public void setText(CharSequence text) {
        mEditText.setText(text, TextView.BufferType.EDITABLE);
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            postStatus();

            return true;
        }

        return false;
    }

    public static class RepostStatusEvent {
        public String text;
    }
}
