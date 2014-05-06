package com.shen.xi.android.tut;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;


public class DisclaimerActivity extends ActionBarActivity {

    public static final String PREF_DISCLAIMER_AGREE = "agree to disclaimer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);

        Button button = (Button) findViewById(android.R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferenceManager.getDefaultSharedPreferences(DisclaimerActivity.this)
                        .edit()
                        .putBoolean(PREF_DISCLAIMER_AGREE, true)
                        .commit();

                finish();
            }
        });

    }

}
