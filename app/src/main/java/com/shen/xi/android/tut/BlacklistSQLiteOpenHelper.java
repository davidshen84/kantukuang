package com.shen.xi.android.tut;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.api.client.util.Lists;

import java.util.Collection;
import java.util.List;

@Deprecated
class BlacklistSQLiteOpenHelper extends SQLiteOpenHelper {

  private static final String TABLE_NAME = "blacklist";
  private static final int VERSION = 1;
  /**
   * this field name is required by {@link android.support.v4.widget.CursorAdapter}
   */
  private static final String FIELD_UID = "_id";
  private static final String SQL_QUERY_ALL_UID =
    String.format("SELECT %s FROM %s", FIELD_UID, TABLE_NAME);
  private static final String SQL_CREATE =
    String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY);", TABLE_NAME, FIELD_UID);
  private static final String SQL_QUERY_ALL =
    String.format("SELECT %s FROM %s", FIELD_UID, TABLE_NAME);
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

  public void insert(long uid) {
    ContentValues values = new ContentValues();
    values.put(FIELD_UID, uid);

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

    mDatabase.delete(TABLE_NAME, FIELD_UID + "=?", new String[]{uid});
  }
}
