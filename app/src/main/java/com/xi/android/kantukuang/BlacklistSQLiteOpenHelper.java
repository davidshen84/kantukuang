package com.xi.android.kantukuang;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.api.client.util.Lists;
import com.google.inject.internal.util.$Nullable;

import java.util.Collection;
import java.util.List;

public class BlacklistSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "blacklist";
    private static final int VERSION = 1;
    private static final String FIELD_UID = "uid";
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
                          TABLE_NAME, FIELD_UID, FIELD_SCREEN_NAME, FIELD_PROFILE_IMAGE_URL,
                          FIELD_REASON_STATUS_ID);

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

        SQLiteDatabase database = getWritableDatabase();
        database.insert(TABLE_NAME, null, values);
        database.close();
    }

    public Collection<Long> getBlockedUIDs() {
        List<Long> list;
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database
                .rawQuery(SQL_QUERY_ALL_UID, null);

        if (!cursor.moveToFirst()) {
            return null;
        }

        list = Lists.newArrayList();
        do {
            list.add(cursor.getLong(0));
        } while (cursor.moveToNext());
        cursor.close();
        database.close();

        return list;
    }
}
