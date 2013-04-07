/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.notifications;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.utils.StringUtil;

public class NotificationsData {

	public static final String DB_NAME = "notifications.db";
	public static final int DB_VERSION = 1;
	public static final String TABLE = "notifications";
	public static final String C_ID = BaseColumns._ID;
	public static final String C_SENDER_ID = "sender_id";
	public static final String C_SENDER_NAME = "from_name";
	public static final String C_CREATED_TIME = "created_time";
	public static final String C_TITLE_TEXT = "title_text";
	public static final String C_BODY_TEXT = "body_text";
	public static final String C_APP_ID = "app_id";
	public static final String C_APP_NAME = "app_name";
	public static final String C_IS_UNREAD = "is_unread";
	public static final String C_IS_HIDDEN = "is_hidden";
	public static final String C_OBJECT_ID = "object_id";
	public static final String C_OBJECT_TYPE = "object_type";

	Context context;

	private static final String GET_ALL_ORDER_BY = C_CREATED_TIME + " DESC";

	private static final String[] MAX_CREATED_AT_COLUMNS = { "max(" + NotificationsData.C_CREATED_TIME + ")" };

	// dbHelper implementation
	public class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Logger.i(NotificationsData.class.getSimpleName() + "." + DbHelper.class.getSimpleName() + "#onCreate");
			String sql = "create table " + TABLE + " (" + C_ID + " text primary key, " + C_SENDER_ID + " text, " + C_SENDER_NAME + " text, " + C_CREATED_TIME + " text, " + C_TITLE_TEXT + " text, "
					+ C_BODY_TEXT + " text, " + C_APP_ID + " text, " + C_APP_NAME + " text, " + C_IS_UNREAD + " text, " + C_IS_HIDDEN + " text, " + C_OBJECT_ID + " text, " + C_OBJECT_TYPE + " text)";
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop table if exists " + TABLE);
			onCreate(db);

		}

	}

	final DbHelper dbHelper;

	public NotificationsData(Context context) {
		this.dbHelper = new DbHelper(context);
	}

	public void close() {
		this.dbHelper.close();
	}

	public void insertOrIgnore(ContentValues values) {
		
		if(values == null){
			return;
		}
		
		Logger.i(NotificationsData.class.getSimpleName() + "#insertOrIgnore " + "values: " + values);
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		try {
			db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		} catch (SQLException e) {
			Logger.i(e.toString());
		} finally {
			// db.close();
		}
	}

	/**
	 * @return Cursor where the colums are _id, created_at, _fromUserName,
	 *         _fromUserId, _title
	 */

	public Cursor getNotifications() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(TABLE, null, null, null, null, null, GET_ALL_ORDER_BY);
	}
/*
	public Cursor getReadNotifications() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		String readValue = "0";
		return db.query(TABLE, null, C_IS_UNREAD + "=" + readValue, null, null, null, GET_ALL_ORDER_BY);
	}
*/
	public Cursor getUnreadNotifications() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		String readValue = "1";
		return db.query(TABLE, null, C_IS_UNREAD + "=" + readValue, null, null, null, GET_ALL_ORDER_BY);
	}

	public long getUnreadCount() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		String readValue = "1";
		return DatabaseUtils.queryNumEntries(db, TABLE, C_IS_UNREAD + "+" + readValue);
	}

	/**
	 * @return Timestamp of the latest notification we have in the database
	 */
	public long getLatestNotificationCreatedTime() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(TABLE, MAX_CREATED_AT_COLUMNS, null, null, null, null, null);
			try {
				return cursor.moveToNext() ? cursor.getLong(0) : Long.MIN_VALUE;
			} finally {
				cursor.close();
			}
		} finally {

			// See:
			// http://stackoverflow.com/questions/7999075/sqlitedatabase-close-function-causing-nullpointerexception-when-multiple-threads
			// db.close();
		}
	}

	// http://stackoverflow.com/questions/7510219/deleting-row-in-sqlite-in-android
	public boolean deleteTitle(String notificationId) {
		
		if(!StringUtil.notEmpty(notificationId)){
			return false;
		}

		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		return db.delete(TABLE, C_ID + "=" + notificationId, null) > 0;

	}
	
	public void deleteAllRows() {
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		db.delete(TABLE, null, null);
	}

}