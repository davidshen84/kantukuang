package com.shen.xi.android.tut


import java.util.{ArrayList => JArrayList}

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.{ActionBarDrawerToggle, Fragment}
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.{ActionBar, ActionBarActivity}
import android.view.{LayoutInflater, Menu, MenuInflater, MenuItem, View, ViewGroup}
import android.widget.{AdapterView, ArrayAdapter, ListView}
import com.google.inject.Inject
import com.shen.xi.android.tut.event.NavigationEvent
import com.squareup.otto.Bus

import scala.collection.JavaConversions._


object NavigationDrawerFragment {
  private val PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned"
}

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */

class NavigationDrawerFragment extends Fragment {

  import com.shen.xi.android.tut.NavigationDrawerFragment.PREF_USER_LEARNED_DRAWER

  /**
   * Per the design guidelines, you should show the drawer on launch until the user manually
   * expands it. This shared preference tracks this.
   */

  private val mEvent = new NavigationEvent()
  private val mTitleSectionList = new JArrayList[String]()
  /**
   * Helper component that ties the action bar to the navigation drawer.
   */
  private var mDrawerToggle: ActionBarDrawerToggle = null
  private var mDrawerLayout: DrawerLayout = null
  private var mDrawerListView: ListView = null
  private var mFragmentContainerView: View = null
  private var mUserLearnedDrawer: Boolean = false
  private var mTitleSectionListAdapter: ArrayAdapter[String] = null

  @Inject
  private var mBus: Bus = null

  TuTModule.getInjector.injectMembers(this)

  override def onAttach(activity: Activity) = {
    super.onAttach(activity)

    // Read in the flag indicating whether or not the user has demonstrated awareness of the
    // drawer. See PREF_USER_LEARNED_DRAWER for details.
    val sp = PreferenceManager.getDefaultSharedPreferences(activity)
    mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false)
  }

  override def onActivityCreated(savedInstanceState: Bundle) = {
    super.onActivityCreated(savedInstanceState)
    // Indicate that this fragment would like to influence the set of actions in the action bar.
    setHasOptionsMenu(true)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    mDrawerListView =
      inflater.inflate(R.layout.fragment_navigation_drawer, container, false).asInstanceOf[ListView]

    mTitleSectionListAdapter = new ArrayAdapter[String](
      getActionBar.getThemedContext, android.R.layout.simple_list_item_1,
      android.R.id.text1, mTitleSectionList)
    mDrawerListView.setAdapter(mTitleSectionListAdapter)

    mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) = selectItem(position)
    })

    mDrawerListView
  }

  def isDrawerOpen = mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView)

  /**
   * Users of this fragment must call this method to set up the navigation drawer interactions.
   *
   * @param fragmentId   The android:id of this fragment in its activity's layout.
   * @param drawerLayout The DrawerLayout containing this fragment's UI.
   */
  def setUp(fragmentId: Int, drawerLayout: DrawerLayout) = {
    mFragmentContainerView = getActivity.findViewById(fragmentId)
    mDrawerLayout = drawerLayout

    // set a custom shadow that overlays the main content when the drawer opens
    mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)
    // set up the drawer's list view with items and click listener

    val actionBar = getActionBar
    actionBar.setDisplayHomeAsUpEnabled(true)
    actionBar.setHomeButtonEnabled(true)

    // ActionBarDrawerToggle ties together the the proper interactions
    // between the navigation drawer and the action bar app icon.
    mDrawerToggle = new ActionBarDrawerToggle(
      getActivity, /* host Activity */
      mDrawerLayout, /* DrawerLayout object */
      R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
      R.string.navigation_drawer_open, /* "open drawer" description for accessibility */
      R.string.navigation_drawer_close /* "close drawer" description for accessibility */
    ) {

      override def onDrawerClosed(drawerView: View) = {
        super.onDrawerClosed(drawerView)
        if (isAdded) {
          getActivity.supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
        }
      }

      override def onDrawerOpened(drawerView: View) = {
        super.onDrawerOpened(drawerView)
        if (isAdded) {
          if (!mUserLearnedDrawer) {
            // The user manually opened the drawer; store this flag to prevent auto-showing
            // the navigation drawer automatically in the future.
            mUserLearnedDrawer = true
            val sp = PreferenceManager
              .getDefaultSharedPreferences(getActivity)
            sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).commit()
          }

          getActivity.supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
        }
      }
    }

    // Defer code dependent on restoration of previous instance state.
    mDrawerLayout.post(new Runnable() {
      override def run() = mDrawerToggle.syncState()
    })

    mDrawerLayout.setDrawerListener(mDrawerToggle)
  }

  def selectItem(position: Int) = {
    if (mDrawerListView != null)
      mDrawerListView.setItemChecked(position, true)

    if (mDrawerLayout != null)
      mDrawerLayout.closeDrawer(mFragmentContainerView)

    mEvent.setPosition(position)
    mBus.post(mEvent)
  }


  override def onDetach() = super.onDetach()

  override def onConfigurationChanged(newConfig: Configuration) = {
    super.onConfigurationChanged(newConfig)
    // Forward the new configuration the drawer toggle component.
    mDrawerToggle.onConfigurationChanged(newConfig)
  }

  override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = {
    // If the drawer is open, show the global app actions in the action bar. See also
    // showGlobalContextActionBar, which controls the top-left area of the action bar.
    if (mDrawerLayout != null && isDrawerOpen) {
      inflater.inflate(R.menu.global, menu)
      showGlobalContextActionBar()
    }
    super.onCreateOptionsMenu(menu, inflater)
  }

  override def onOptionsItemSelected(item: MenuItem) = mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

  /**
   * Per the navigation drawer design guidelines, updates the action bar to show the global app
   * 'context', rather than just what's in the current screen.
   */
  private def showGlobalContextActionBar() = {
    val actionBar = getActionBar
    actionBar.setDisplayShowTitleEnabled(true)
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD)
    actionBar.setTitle(R.string.app_name)
  }

  private def getActionBar = getActivity.asInstanceOf[ActionBarActivity].getSupportActionBar

  def initItems() = {
    val defaultSections = getResources.getStringArray(R.array.default_sections)

    mTitleSectionList.clear()

    mTitleSectionList.addAll(defaultSections.toSeq)
    mTitleSectionListAdapter.notifyDataSetChanged()
  }

}
