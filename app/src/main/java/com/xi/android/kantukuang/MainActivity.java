package com.xi.android.kantukuang;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.xi.android.kantukuang.event.NavigationEvent;
import com.xi.android.kantukuang.event.RefreshCompleteEvent;
import com.xi.android.kantukuang.event.RefreshStatusCompleteEvent;
import com.xi.android.kantukuang.event.SectionAttachEvent;
import com.xi.android.kantukuang.event.SelectItemEvent;
import com.xi.android.kantukuang.weibo.WeiboClient;
import com.xi.android.kantukuang.weibo.WeiboStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getName();
    private static final String PREF_USER_WEIBO_ACCESS_TOKEN = "weibo access token";
    private static final String STATE_DRAWER_SELECTED_ID = "selected navigation drawer position";
    private final RefreshCompleteEvent mRefreshCompleteEvent = new RefreshCompleteEvent();
    @Inject
    private Bus mBus;
    @Inject
    private JsonFactory mJsonFactory;
    @Inject
    private WeiboClient mWeiboClient;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private int mCurrentDrawerSelectedId;
    private boolean mHasAttachedSection = false;

    public MainActivity() {
        Injector mInjector = KanTuKuangModule.getInjector();
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
        mHasAttachedSection = getSupportFragmentManager().findFragmentById(R.id.container) != null;

        // set up action bar title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getTitle());

        setUpWeiboClientAndLoader();
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
        mBus.unregister(this);

        super.onPause();
    }

    private void restoreActionBar() {
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

    /**
     * initialize the loader
     * then restore the drawer state
     */
    private void setUpWeiboClientAndLoader() {
        restoreNavigationDrawerState();
    }

    private void restoreNavigationDrawerState() {
        mNavigationDrawerFragment.initItems();

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
            case R.id.menu_settings:
                Log.v(TAG, getString(R.string.menu_settings));

                return true;

            case R.id.action_refresh:

                OnRefreshListener refreshListener = (OnRefreshListener)
                        getSupportFragmentManager().findFragmentById(R.id.container);
                refreshListener.onRefreshStarted(null);

                return true;

            case R.id.action_edit_blacklist: {
                Intent intent = new Intent(this, EditBlacklistActivity.class);
                startActivity(intent);
            }
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void selectedItem(SelectItemEvent event) {
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
    public void navigateSection(NavigationEvent event) {
        Fragment itemFragment;

        mCurrentDrawerSelectedId = event.getPosition();
        switch (mCurrentDrawerSelectedId) {

            case 0:
                // Weibo
                itemFragment = ItemFragment.newInstance(getString(R.string.section_name_public));

                break;

            case 1:
                // Qing
                itemFragment = QingItemFragment.newInstance("猫");

                break;

            default:
                Log.d(TAG, String.format("%d not ready", mCurrentDrawerSelectedId));

                return;
        }

        // update the main content by replacing fragments
        if (itemFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, itemFragment, "Section")
                    .commit();
        }
    }

    @Subscribe
    public void refreshStatusComplete(RefreshStatusCompleteEvent event) {
        List<WeiboStatus> statusList = event.getStatus();
        String lastId = null;
        if (statusList == null) {
            Toast.makeText(this, R.string.message_error_load, Toast.LENGTH_SHORT).show();
        } else if (statusList.size() == 0) {
            Toast.makeText(this, R.string.message_info_no_new, Toast.LENGTH_SHORT).show();
        } else {
            String message = getResources()
                    .getString(R.string.format_info_new_data, statusList.size());
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            lastId = statusList.get(0).id;
        }
        // update view
        mBus.post(mRefreshCompleteEvent
                          .setStatusList(statusList)
                          .setLastId(lastId));
    }

    @Subscribe
    public void sectionAttach(SectionAttachEvent event) {
        setTitle(event.sectionName);
        mCurrentDrawerSelectedId = event.sectionId;
    }

}
