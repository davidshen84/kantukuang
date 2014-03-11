package com.xi.android.kantukuang;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.api.client.util.Lists;

import java.util.Collection;
import java.util.List;

class BlacklistSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "blacklist";
    private static final int VERSION = 1;
    /**
     * this field name is required by {@link android.support.v4.widget.CursorAdapter}
     */
    private static final String FIELD_UID = "_id";
    private static final String SQL_QUERY_ALL_UID =
            String.format("SELECT %s FROM %s", FIELD_UID, TABLE_NAME);
    private static final String FIELD_SCREEN_NAME = "screen_name";
    private static final String FIELD_PROFILE_IMAGE_URL = "profile_image_url";
    private static final String FIELD_REASON_STATUS_ID = "reason_status_id";
    private static final String SQL_CREATE =
            String.format("CREATE TABLE %s (" +
                                  "%s INTEGER PRIMARY KEY," +
                                  "%s TEXT NOT NULL," +
                                  "%s TEXT," +
                                  "%s TEXT);",
                          TABLE_NAME, FIELD_UID, FIELD_SCREEN_NAME,
                          FIELD_PROFILE_IMAGE_URL,
                          FIELD_REASON_STATUS_ID);
    private static final String SQL_QUERY_ALL =
            String.format("SELECT %s, %s, %s, %s FROM %s",
                          FIELD_UID, FIELD_SCREEN_NAME, FIELD_PROFILE_IMAGE_URL,
                          FIELD_REASON_STATUS_ID, TABLE_NAME);
    private SQLiteDatabase mDatabase;

    public BlacklistSQLiteOpenHelper(Context context) {
        super(context, TABLE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }

    public void insert(long uid, String screenName, String profileImage, String statusId) {
        ContentValues values = new ContentValues();
        values.put(FIELD_UID, uid);
        values.put(FIELD_SCREEN_NAME, screenName);
        values.put(FIELD_PROFILE_IMAGE_URL, profileImage);
        values.put(FIELD_REASON_STATUS_ID, statusId);

        ensureDatabase(true);
        mDatabase.insert(TABLE_NAME, null, values);
    }

    public Collection<Long> getBlockedUIDs() {
        List<Long> list;

        ensureDatabase(false);
        Cursor cursor = mDatabase.rawQuery(SQL_QUERY_ALL_UID, null);

        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        list = Lists.newArrayList();
        do {
            list.add(cursor.getLong(0));
        } while (cursor.moveToNext());
        cursor.close();

        return list;
    }

    public Cursor getBlockedAccountCursor() {
        ensureDatabase(false);

        return mDatabase.rawQuery(SQL_QUERY_ALL, null);
    }

    private void ensureDatabase(boolean writable) {
        if (writable) {
            if (mDatabase == null) {
                mDatabase = getWritableDatabase();
            } else if (mDatabase.isReadOnly()) {
                mDatabase.close();
                mDatabase = getWritableDatabase();
            }
        } else {
            if (mDatabase == null) mDatabase = getReadableDatabase();
        }
    }

    public void remove(String uid) {
        ensureDatabase(true);

        mDatabase.delete(TABLE_NAME, "_id=?", new String[]{uid});
    }
}
