package com.shen.xi.android.tut


import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.{Fragment, FragmentTransaction}
import android.view.{LayoutInflater, View, ViewGroup}
import com.google.inject.Inject
import com.squareup.otto.Bus


object DisclaimerFragment {
  def newInstance() = new DisclaimerFragment()
}

/**
 * A fragment with a Google +1 button.
 * Use the 'DisclaimerFragment#newInstance' factory method to
 * create an instance of this fragment.
 */
class DisclaimerFragment extends Fragment {

  @Inject
  private var mBus: Bus = null

  TuTModule.getInjector.injectMembers(this)

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment DisclaimerFragment.
   */
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle) = {
    // Inflate the layout for this fragment
    val view = inflater.inflate(R.layout.fragment_disclaimer, container, false)

    view.findViewById(android.R.id.button1).setOnClickListener(
      new View.OnClickListener() {
        override def onClick(view: View) = {
          PreferenceManager.getDefaultSharedPreferences(getActivity)
            .edit()
            .putBoolean(MainActivity.PREF_DISCLAIMER_AGREE, true)
            .commit()

          getFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            .remove(DisclaimerFragment.this)
            .commit()
        }
      }
    )

    view
  }

}
