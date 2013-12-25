package com.xi.android.kantukuang;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebActivity extends ActionBarActivity {

    public static final String LANDING_URL = "com.xi.android.kantukuang.LANDING_URL";
    public static final String AUTHORIZE_CODE = "com.xi.android.kantukuang.AUTHORIZE_CODE";
    private static final String TAG = WebActivity.class.getName();

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        // get landing url
        String authorizeUrl = getIntent().getStringExtra(LANDING_URL);

        // set up web view
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                Log.v(TAG, host);
                assert host != null;
                if (host.equals("kantukuang.com")) {
                    // return action result
                    view.stopLoading();

                    Intent resultIntent = new Intent(url);
                    resultIntent.putExtra(WebActivity.AUTHORIZE_CODE, uri.getQueryParameter(
                            "code"));
                    WebActivity.this.setResult(RESULT_OK, resultIntent);
                    WebActivity.this.finish();
                }

                super.onPageStarted(view, url, favicon);
            }
        });

        webView.loadUrl(authorizeUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_bind_weibo:

                Toast.makeText(this, R.string.title_bind_weibo, Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
