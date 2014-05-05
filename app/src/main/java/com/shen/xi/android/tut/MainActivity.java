package com.shen.xi.android.tut;

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

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.json.JsonFactory;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.shen.xi.android.tut.event.NavigationEvent;
import com.shen.xi.android.tut.event.RefreshCompleteEvent;
import com.shen.xi.android.tut.event.RefreshStatusCompleteEvent;
import com.shen.xi.android.tut.event.SectionAttachEvent;
import com.shen.xi.android.tut.event.SelectItemEvent;
import com.shen.xi.android.tut.sinablog.QingPageDriver;
import com.shen.xi.android.tut.weibo.WeiboClient;
import com.shen.xi.android.tut.weibo.WeiboStatus;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import static com.shen.xi.android.tut.MainActivity.ImageSource.Weibo;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getName();
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
    @Inject
    @Named("qing request factory")
    private HttpRequestFactory mHttpRequestFactory;
    @Inject
    private QingPageDriver mQingPageDriver;
    private ImageSource mSectionType;

    public MainActivity() {
        Injector mInjector = TuTModule.getInjector();
        mInjector.injectMembers(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mCurrentDrawerSelectedId = savedInstanceState.getInt(STATE_DRAWER_SELECTED_ID, 0);
        }

        // Fragment managing the behaviours, interactions and presentation of the navigation drawer.
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

        restoreNavigationDrawerState();
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
            if (mSectionType != Weibo)
                menu.findItem(R.id.action_edit_blacklist).setVisible(false);
            restoreActionBar();

            return true;
        }

        return super.onCreateOptionsMenu(menu);

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
                        getSupportFragmentManager().findFragmentById(
                                R.id.container);
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
    public void itemSelected(SelectItemEvent event) {
        Intent intent = null;
        switch (event.source) {
            case Weibo:
                intent = new Intent(this, WeiboImageViewActivity.class);
                break;

            case QingTag:
                intent = new Intent(this, QingImageViewActivity.class);
                break;

            case QingPage:

                intent = new Intent(this, QingImageViewActivity.class);

                break;
        }

        if (intent != null) {
            intent.putExtras(event.extras);
            startActivity(intent);
        }
    }

    @Subscribe
    public void navigateSection(NavigationEvent event) {
        Fragment itemFragment;

        mCurrentDrawerSelectedId = event.getPosition();
        switch (mCurrentDrawerSelectedId) {

            case 0:
                // Weibo
                itemFragment = WeiboItemFragment.newInstance(getString(
                        R.string.section_name_weibo));

                break;

            case 1:
                // Qing - Mao
                itemFragment = QingItemFragment.newInstance("猫", false);

                break;

            case 2:
                // Qing - Wei Mei
                itemFragment = QingItemFragment.newInstance("唯美", true);

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
        if (statusList == null || statusList.size() == 0) {
            Toast.makeText(this, R.string.message_info_no_update, Toast.LENGTH_SHORT).show();
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
        mSectionType = event.source;
        setTitle(event.sectionName);
    }

    public enum ImageSource {
        Unknown, Weibo, QingTag, QingPage
    }

}
