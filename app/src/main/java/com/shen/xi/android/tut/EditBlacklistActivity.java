package com.shen.xi.android.tut;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shen.xi.android.tut.event.UnblockAccountEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class EditBlacklistActivity extends ActionBarActivity {

    private static final String TAG = EditBlacklistActivity.class.getName();
    private ListView mListView;
    private BlacklistSQLiteOpenHelper mSqLiteOpenHelper;
    @Inject
    private Bus mBus;
    private EditBlacklistActivity.BlacklistViewAdapter mBlacklistViewAdapter;

    public EditBlacklistActivity() {
        TuTModule.getInjector().injectMembers(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_blacklist);

        mListView = (ListView) findViewById(android.R.id.list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBus.register(this);

        mSqLiteOpenHelper = new BlacklistSQLiteOpenHelper(this);
        Cursor cursor = mSqLiteOpenHelper.getBlockedAccountCursor();
        mBlacklistViewAdapter = new BlacklistViewAdapter(this, cursor);
        mListView.setAdapter(mBlacklistViewAdapter);
    }

    @Override
    protected void onPause() {
        mSqLiteOpenHelper.close();

        mBus.unregister(this);
        super.onPause();
    }

    @Subscribe
    public void unblockAccount(UnblockAccountEvent event) {
        mSqLiteOpenHelper.remove(event.uid);
        // swap cursor and close old cursor
        mBlacklistViewAdapter
                .swapCursor(mSqLiteOpenHelper.getBlockedAccountCursor())
                .close();
        mBlacklistViewAdapter.notifyDataSetChanged();
    }

    private class BlacklistViewAdapter extends CursorAdapter {
        private final UnblockAccountEvent event = new UnblockAccountEvent();
        @Inject
        LayoutInflater layoutInflater;
        @Inject
        ImageLoader imageLoader;

        public BlacklistViewAdapter(Context context, Cursor c) {
            super(context, c, false);

            TuTModule.getInjector().injectMembers(this);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            View view = layoutInflater.inflate(R.layout.item_blacklist, viewGroup, false);
            ViewHolder holder = new ViewHolder();
            view.setTag(holder);

            holder.ImageView = (ImageView) view.findViewById(android.R.id.icon);
            holder.TextView = (TextView) view.findViewById(android.R.id.text1);
            holder.Button = (Button) view.findViewById(android.R.id.button1);

            holder.Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    event.uid = view.getTag().toString();
                    mBus.post(event);
                }
            });

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();

            holder.TextView.setText(cursor.getString(1));
            imageLoader.displayImage(cursor.getString(2), holder.ImageView);
            holder.Button.setTag(cursor.getLong(0));
        }
    }

    private class ViewHolder {
        public TextView TextView;
        public ImageView ImageView;
        public Button Button;
    }

}
