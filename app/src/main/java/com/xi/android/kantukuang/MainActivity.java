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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboTimelineAsyncTaskLoader;

import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class MainActivity extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        ItemFragment.OnFragmentInteractionListener,
        LoaderManager.LoaderCallbacks<List<String>> {

    private static final int ACTIVITY_REQUEST_CODE_BIND_WEIBO = 0x000A;
    private static final String TAG = MainActivity.class.getName();
    private static final String PREF_USER_WEIBO_ACCESS_TOKEN = "weibo access token";
    private static final String STATE_DRAWER_SELECTED_ID = "selected navigation drawer position";
    private final Injector mInjector;
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
        super.onPause();

        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(PREF_USER_WEIBO_ACCESS_TOKEN, mAccessToken)
                .commit();
    }

    @Override
    public void onNavigationDrawerItemSelected(int selectedId) {
        ItemFragment itemFragment = null;

        mCurrentDrawerSelectedId = selectedId;
        switch (selectedId) {
            case 0:
                // Public Home
                itemFragment = ItemFragment.newInstance("public", selectedId);

                break;
            case 1:
                // Private Home
                itemFragment = ItemFragment.newInstance("home", selectedId);

                break;
            default:
                Log.d(TAG, "default not ready");

                break;
        }
        // update the main content by replacing fragments
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, itemFragment)
                .commit();
    }

    private String mapSectionIdToString(int sectionId) {
        switch (sectionId) {
            case 0:
                return "public";
            case 1:
                return "home";
            default:
                Log.d(TAG, "not ready");
                return "";
        }
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
                onNavigationDrawerItemSelected(0);
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

        // TODO restore user added sections;
        String[] userSections = {};
        String sectionPrivate = getString(R.string.default_section_private);
        mNavigationDrawerFragment.setItems(Lists.asList(sectionPrivate, userSections));

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

    @Override
    public void onItemFragmentInteraction(int position) {
        // start image view activity
        ItemFragment itemFragment = (ItemFragment)
                getSupportFragmentManager().findFragmentById(R.id.container);
        Intent intent = new Intent(this, ImageViewActivity.class);

        intent.putStringArrayListExtra(ImageViewActivity.URL_LIST,
                                       itemFragment.getItemArrayList());
        intent.putExtra(ImageViewActivity.ITEM_POSITION, position);

        startActivity(intent);
    }

    @Override
    public Loader<List<String>> onCreateLoader(int i, Bundle bundle) {
        Log.v(TAG, "create new loader");

        return new WeiboTimelineAsyncTaskLoader(
                this, mWeiboClient);
    }

    @Override
    public void onLoadFinished(Loader<List<String>> listLoader, List<String> strings) {
        ItemFragment itemFragment = (ItemFragment) getSupportFragmentManager().findFragmentById(
                R.id.container);

        WeiboTimelineAsyncTaskLoader loader = (WeiboTimelineAsyncTaskLoader) listLoader;
        if (loader.takeHasError()) {
            // display error message
            Toast.makeText(this, R.string.message_error_load, Toast.LENGTH_SHORT).show();
            itemFragment.onRefreshComplete(null, loader.getLastId());
        } else {
            int newDataCount = loader.takeNewDataCount();
            if (newDataCount > 0) {
                String toastMessage = getResources()
                        .getString(R.string.format_new_data_count, newDataCount);
                Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();

                // update view
                itemFragment.onRefreshComplete(strings, loader.getLastId());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<String>> listLoader) {
        Log.d(TAG, "reset loader");
    }

    public void forceLoad() {
        getSupportLoaderManager().getLoader(0).forceLoad();
    }

    public void refreshLoader() {
        getSupportLoaderManager().getLoader(0).onContentChanged();
    }

    public void onSectionAttached(int sectionId) {
        setTitle(mapSectionIdToString(sectionId));
        mCurrentDrawerSelectedId = sectionId;
        mHasAttachedSection = true;
    }
}
