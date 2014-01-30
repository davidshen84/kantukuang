package com.xi.android.kantukuang;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.inject.Inject;
import com.squareup.otto.Bus;
import com.xi.android.kantukuang.event.RepostStatusEvent;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link RepostStatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RepostStatusFragment extends Fragment implements View.OnClickListener {
    private static final String ARG_STATUS_ID = "status id";
    private static final String ARG_TEXT = "status text";
    private static final LinearLayout.LayoutParams layoutParams =
            new LinearLayout.LayoutParams(0, 0);
    private static final float SCALE_FACTOR = 0.8F;
    private static final RepostStatusEvent event = new RepostStatusEvent();
    private String mStatusId;
    private String mText;
    @Inject
    private Bus mBus;

    public RepostStatusFragment() {
        KanTuKuangModule.getInjector().injectMembers(this);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param statusId status id.
     * @param text     status text.
     * @return A new instance of fragment RepostStatusFragment.
     */
    public static RepostStatusFragment newInstance(String statusId, String text) {
        RepostStatusFragment fragment = new RepostStatusFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS_ID, statusId);
        args.putString(ARG_TEXT, text);
        fragment.setArguments(args);
        fragment.setRetainInstance(false);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStatusId = getArguments().getString(ARG_STATUS_ID);
            mText = getArguments().getString(ARG_TEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_repost_status, container, false);
        int width = (int) (container.getWidth() * SCALE_FACTOR);
        int height = (int) (container.getHeight() * SCALE_FACTOR);

        assert view != null;
        layoutParams.width = width;
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);

        EditText editText = (EditText) view.findViewById(android.R.id.text1);
        Button button = (Button) view.findViewById(android.R.id.button1);

        editText.setText(mText);
        button.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        event.text = mText;
        event.statusId = mStatusId;
        mBus.post(event);
        getActivity().getSupportFragmentManager().popBackStack();
    }

}
