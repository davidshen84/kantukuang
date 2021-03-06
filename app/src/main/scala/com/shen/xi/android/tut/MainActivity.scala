package com.shen.xi.android.tut

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.{ActionBar, ActionBarActivity}
import android.util.Log
import android.view.{Menu, MenuItem, View}
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.json.JsonFactory
import com.google.inject.Inject
import com.google.inject.name.Named
import com.shen.xi.android.tut.event.{NavigationEvent, SectionAttachEvent}
import com.shen.xi.android.tut.sinablog.QingPageDriver
import com.shen.xi.android.tut.weibo.WeiboClient
import com.squareup.otto.{Bus, Subscribe}
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener


object MainActivity {

  val PREF_DISCLAIMER_AGREE = "agree to disclaimer"
  val TAG = classOf[MainActivity].getName
  val STATE_DRAWER_SELECTED_ID = "selected navigation drawer position"
  val sectionMatcher = """(qingPage|qingTag|weibo)\((([^\|]+)\|?([^\|]*))\)""".r

}

class MainActivity extends ActionBarActivity {

  import com.shen.xi.android.tut.MainActivity._

  @Inject
  private var mBus: Bus = null
  @Inject
  private var mJsonFactory: JsonFactory = null
  @Inject
  private var mWeiboClient: WeiboClient = null
  private var mNavigationDrawerFragment: NavigationDrawerFragment = null
  private var mCurrentDrawerSelectedId: Int = 0
  private var mHasAttachedSection = false
  @Inject
  @Named("qing request factory")
  private var mHttpRequestFactory: HttpRequestFactory = null
  @Inject
  private var mQingPageDriver: QingPageDriver = null
  private var mSectionType: ImageSource = null

  val mInjector = TuTModule.getInjector
  mInjector.injectMembers(this)

  override protected def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main)

    if (savedInstanceState != null) {
      mCurrentDrawerSelectedId = savedInstanceState.getInt(STATE_DRAWER_SELECTED_ID, 0)
    }

    // Fragment managing the behaviours, interactions and presentation of the navigation drawer.
    mNavigationDrawerFragment = getSupportFragmentManager.findFragmentById(R.id.navigation_drawer).asInstanceOf[NavigationDrawerFragment]

    // Set up the drawer.
    mNavigationDrawerFragment.setUp(
      R.id.navigation_drawer,
      findViewById(R.id.drawer_layout).asInstanceOf[DrawerLayout])
  }


  override protected def onResume() = {
    super.onResume()
    mBus.register(this)

    mHasAttachedSection = getSupportFragmentManager
      .findFragmentById(R.id.container) != null

    // set up action bar title
    val actionBar = getSupportActionBar
    actionBar.setTitle(getTitle)

    restoreNavigationDrawerState()
    if (mHasAttachedSection)
      findViewById(R.id.main_activity_message).setVisibility(View.GONE)
    // switch to disclaimer
    if (!PreferenceManager.getDefaultSharedPreferences(this)
      .getBoolean(PREF_DISCLAIMER_AGREE, false)) {
      val fragment = DisclaimerFragment.newInstance()
      getSupportFragmentManager
        .beginTransaction()
        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        .add(R.id.container, fragment)
        .commit()
    }
  }

  override protected def onSaveInstanceState(outState: Bundle) = {
    super.onSaveInstanceState(outState)

    outState.putInt(STATE_DRAWER_SELECTED_ID, mCurrentDrawerSelectedId)
  }

  override protected def onPause() = {
    mBus.unregister(this)

    super.onPause()
  }

  private def restoreActionBar() = {
    val actionBar = getSupportActionBar
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD)
    actionBar.setDisplayShowTitleEnabled(true)
    actionBar.setTitle(getTitle)
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    if (!mNavigationDrawerFragment.isDrawerOpen) {
      // Only show items in the action bar relevant to this screen
      // if the drawer is not showing. Otherwise, let the drawer
      // decide what to show in the action bar.
      getMenuInflater.inflate(R.menu.main, menu)
      restoreActionBar()

      true
    } else {
      super.onCreateOptionsMenu(menu)
    }
  }

  private def restoreNavigationDrawerState() = {
    mNavigationDrawerFragment.initItems()

    if (!mHasAttachedSection) {
      mNavigationDrawerFragment.selectItem(mCurrentDrawerSelectedId)
    }

    findViewById(R.id.main_activity_message).setVisibility(View.GONE)
  }

  override def onOptionsItemSelected(item: MenuItem) =
  // Handle action bar item clicks here. The action bar will
  // automatically handle clicks on the Home/Up button, so long
  // as you specify a parent activity in AndroidManifest.xml.
    item.getItemId match {
      case R.id.menu_settings => Log.v(TAG, getString(R.string.menu_settings))

        true

      case R.id.action_refresh =>
        getSupportFragmentManager
          .findFragmentById(R.id.container)
          .asInstanceOf[OnRefreshListener]
          .onRefreshStarted(null)

        true

      case _ => super.onOptionsItemSelected(item)

    }

  @Subscribe
  def navigateSection(event: NavigationEvent) = {
    mCurrentDrawerSelectedId = event.position
    val sections = getResources.getStringArray(R.array.sections)
    val itemFragment = sectionMatcher.findFirstMatchIn(sections(mCurrentDrawerSelectedId)) match {
      case Some(m) => m.subgroups match {
        case List("weibo", p, _*) => WeiboItemFragment.newInstance(p)
        case List("qingPage", _, p, q) => QingItemFragment.newInstance(p, q, parsePage = true)
        case List("qingTag", _, p, q) => QingItemFragment.newInstance(p, q, parsePage = false)
        case _ => throw new IllegalArgumentException(m.matched)
      }

      case None => null
    }

    // update the main content by replacing fragments
    if (itemFragment != null) {
      getSupportFragmentManager
        .beginTransaction()
        .replace(R.id.container, itemFragment, "Section")
        .commit()
    }
  }

  @Subscribe
  def sectionAttach(event: SectionAttachEvent) = {
    mSectionType = event.source
    setTitle(event.sectionName)
  }

}
