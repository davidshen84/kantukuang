package com.shen.xi.android.tut;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.inject.Inject;
import com.squareup.otto.Bus;

/**
 * A fragment with a Google +1 button.
 * Use the {@link DisclaimerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisclaimerFragment extends Fragment {

    @Inject
    private Bus mBus;

    public DisclaimerFragment() {
        TuTModule.getInjector().injectMembers(this);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DisclaimerFragment.
     */
    public static DisclaimerFragment newInstance() {
        return new DisclaimerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_disclaimer, container, false);

        view.findViewById(android.R.id.button1).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PreferenceManager.getDefaultSharedPreferences(getActivity())
                                .edit()
                                .putBoolean(MainActivity.PREF_DISCLAIMER_AGREE, true)
                                .commit();

                        getFragmentManager().beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                                .remove(DisclaimerFragment.this)
                                .commit();
                    }
                }
        );

        return view;
    }

}
