package com.xi.android.kantukuang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.api.client.json.JsonFactory;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboStatus;
import com.xi.android.kantukuang.weibo.WeiboTimelineAsyncTaskLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<List<WeiboStatus>> {

    private static final int ACTIVITY_REQUEST_CODE_BIND_WEIBO = 0x000A;
    private static final String TAG = MainActivity.class.getName();
    private static final String PREF_USER_WEIBO_ACCESS_TOKEN = "weibo access token";
    private static final String STATE_DRAWER_SELECTED_ID = "selected navigation drawer position";
    private final Injector mInjector;
    private final RefreshCompleteEvent mRefreshCompleteEvent = new RefreshCompleteEvent();
    @Inject
    private Bus mBus;
    @Inject
    private JsonFactory mJsonFactory;
    @Inject
    private WeiboClient mWeiboClient;
    private String mAccessToken;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private int mCurrentDrawerSelectedId;
    private boolean mHasAttachedSection = false;


    public MainActivity() {
        mInjector = KanTuKuangModule.getInjector();
        mInjector.injectMembers(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mCurrentDrawerSelectedId = savedInstanceState.getInt(STATE_DRAWER_SELECTED_ID, 0);
        }

//      Fragment managing the behaviors, interactions and presentation of the navigation drawer.
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                supportFragmentManager.findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBus.register(this);

        // set up action bar title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getTitle());

        // restore weibo access token
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mAccessToken = sp.getString(PREF_USER_WEIBO_ACCESS_TOKEN, "");

        setUpWeiboClientAndLoader(mAccessToken);
        if (mHasAttachedSection)
            findViewById(R.id.main_activity_message).setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_DRAWER_SELECTED_ID, mCurrentDrawerSelectedId);
    }

    @Override
    protected void onPause() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(PREF_USER_WEIBO_ACCESS_TOKEN, mAccessToken)
                .commit();

        mBus.unregister(this);

        super.onPause();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getTitle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();

            return true;
        }

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ACTIVITY_REQUEST_CODE_BIND_WEIBO) {
            if (resultCode == RESULT_OK) {
                // get authorize code
                String code = data.getStringExtra(WebActivity.AUTHORIZE_CODE);
                Log.d(TAG, String.format("authorize code: %s", code));

                // request access token
                String accessToken = mWeiboClient.requestAccessToken(code);
                mCurrentDrawerSelectedId = 0;
                setUpWeiboClientAndLoader(accessToken);

                // select public by default
                mNavigationDrawerFragment.selectItem(0);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    /**
     * set the access token and initialize the loader
     * then restore the drawer state
     *
     * @param accessToken: weibo client access token
     */
    private void setUpWeiboClientAndLoader(String accessToken) {
        assert getSupportLoaderManager().getLoader(mCurrentDrawerSelectedId) != null;

        mWeiboClient.setAccessToken(accessToken);
        if (mWeiboClient.IsAuthenticated()) {
            restoreNavigationDrawerState();
        }
    }

    private void restoreNavigationDrawerState() {
        assert mWeiboClient.IsAuthenticated();

        // add default private section
        // this section is only available when the weibo client is authenticated
        String sectionPrivate = getString(R.string.default_section_private);
        mNavigationDrawerFragment.setItems(Arrays.asList(sectionPrivate));

        if (!mHasAttachedSection) {
            mNavigationDrawerFragment.selectItem(mCurrentDrawerSelectedId);
        }

        findViewById(R.id.main_activity_message).setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                // TODO dev code
                mAccessToken = mInjector.getInstance(
                        Key.get(String.class,
                                Names.named("access token")));
                mCurrentDrawerSelectedId = 0;
                setUpWeiboClientAndLoader(mAccessToken);

                return true;
            case R.id.action_bind_weibo:
                Intent intent = new Intent(this, WebActivity.class);
                intent.putExtra(WebActivity.LANDING_URL, mWeiboClient.getAuthorizeUrl());

                startActivityForResult(intent, ACTIVITY_REQUEST_CODE_BIND_WEIBO);

                return true;
            case R.id.action_refresh:
                if (mWeiboClient.IsAuthenticated()) {
                    OnRefreshListener refreshListener = (OnRefreshListener)
                            getSupportFragmentManager().findFragmentById(R.id.container);
                    if (refreshListener != null)
                        refreshListener.onRefreshStarted(null);
                    else
                        Log.v(TAG, "refresh action has no listener");
                } else {
                    Toast.makeText(this, getString(R.string.message_warning_bind_weibo),
                                   Toast.LENGTH_SHORT).show();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void selectedItem(ItemFragment.SelectItemEvent event) {
        // start image view activity
        ItemFragment itemFragment = (ItemFragment)
                getSupportFragmentManager().findFragmentById(R.id.container);
        Intent intent = new Intent(this, ImageViewActivity.class);

        ArrayList<WeiboStatus> statuses = itemFragment.getStatuses();

        intent.putExtra(ImageViewActivity.ITEM_POSITION, event.getPosition());

        try {
            intent.putExtra(ImageViewActivity.STATUS_JSON, mJsonFactory.toString(statuses));
        } catch (IOException e) {
            e.printStackTrace();

            intent.putExtra(ImageViewActivity.STATUS_JSON, "[]");
        }

        startActivity(intent);
    }

    @Subscribe
    public void navigateSection(NavigationDrawerFragment.NavigationEvent event) {
        ItemFragment itemFragment = null;

        mCurrentDrawerSelectedId = event.getPosition();
        switch (mCurrentDrawerSelectedId) {
            case 0:
                // Public Home
                itemFragment = ItemFragment.newInstance(
                        getString(R.string.section_name_public),
                        mCurrentDrawerSelectedId);

                break;
            case 1:
                // Private Home
                itemFragment = ItemFragment.newInstance(
                        getString(R.string.section_name_home),
                        mCurrentDrawerSelectedId);

                break;
            default:
                Log.d(TAG, "default not ready");

                break;
        }

        // update the main content by replacing fragments
        if (itemFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, itemFragment)
                    .commit();
        }
    }

    @Override
    public Loader<List<WeiboStatus>> onCreateLoader(int i, Bundle bundle) {
        Log.v(TAG, "create new loader");

        return new WeiboTimelineAsyncTaskLoader(
                this, mWeiboClient);
    }

    @Override
    public void onLoadFinished(Loader<List<WeiboStatus>> listLoader, List<WeiboStatus> statuses) {
        WeiboTimelineAsyncTaskLoader loader = (WeiboTimelineAsyncTaskLoader) listLoader;

        if (loader.takeHasError()) {
            // display error message
            Toast.makeText(this, R.string.message_error_load, Toast.LENGTH_SHORT).show();
            mBus.post(mRefreshCompleteEvent
                              .setStatusList(null)
                              .setLastId(loader.getLastId()));
        } else {
            int newDataCount = loader.takeNewDataCount();
            if (newDataCount > 0) {
                String toastMessage = getResources()
                        .getString(R.string.format_new_data_count, newDataCount);
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();

                // update view
                mBus.post(mRefreshCompleteEvent
                                  .setStatusList(statuses)
                                  .setLastId(loader.getLastId()));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<WeiboStatus>> listLoader) {
        Log.d(TAG, "reset loader");
    }

    public void forceLoad() {
        getSupportLoaderManager().getLoader(0).forceLoad();
    }

    public void refreshLoader() {
        getSupportLoaderManager().getLoader(0).onContentChanged();
    }

    @Subscribe
    public void sectionAttach(ItemFragment.SectionAttachEvent event) {
        setTitle(event.getSectionName());
        mCurrentDrawerSelectedId = event.getSectionId();
        mHasAttachedSection = true;
    }

    public class RefreshCompleteEvent {
        private List<WeiboStatus> mStatusList;
        private String mLastId;

        public List<WeiboStatus> getStatusList() {
            return mStatusList;
        }

        public RefreshCompleteEvent setStatusList(List<WeiboStatus> statusList) {
            this.mStatusList = statusList;

            return this;
        }

        public String getLastId() {
            return mLastId;
        }

        public RefreshCompleteEvent setLastId(String lastId) {
            this.mLastId = lastId;

            return this;
        }
    }

}
