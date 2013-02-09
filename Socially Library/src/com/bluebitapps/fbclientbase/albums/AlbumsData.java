/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.albums;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.utils.StringUtil;

public class AlbumsData {

	public static final String DB_NAME = "albums.db";
	public static final int DB_VERSION = 1;
	public static final String TABLE = "albums";
	public static final String C_ID = "_id";
	public static final String C_USER_ID = "user_id";
	public static final String C_FROM_NAME = "from_name";
	public static final String C_FROM_UID = "from_uid";
	public static final String C_FROM_CATEGORY = "from_category";
	public static final String C_NAME = "name";
	public static final String C_LINK = "link";
	public static final String C_COVER_PHOTO = "cover_photo";
	public static final String C_PRIVACY = "friends";
	public static final String C_COUNT = "count";
	public static final String C_CREATED_TIME = "created_time";
	public static final String C_UPDATED_TIME = "updated_time";
	public static final String C_CAN_UPLOAD = "can_upload";

	Context context;

	private static final String GET_ALL_ORDER_BY = C_CREATED_TIME + " DESC";

	private static final String[] MAX_CREATED_AT_COLUMNS = { "max(" + C_CREATED_TIME + ")" };

	private static final String[] DB_NAME_COLUMNS = { C_NAME };

	// dbHelper implementation
	public class AlbumsDbHelper extends SQLiteOpenHelper {

		public AlbumsDbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Logger.i(AlbumsDbHelper.class.getSimpleName() + "#onCreate");
			String sql = "create table " + TABLE + " (" + C_ID + " text primary key, " + C_USER_ID + " text, " + C_FROM_NAME + " text, " + C_FROM_UID + " text, " + C_FROM_CATEGORY + " text, "
					+ C_NAME + " text, " + C_LINK + " text, " + C_COVER_PHOTO + " text, " + C_PRIVACY + " text, " + C_COUNT + " text, " + C_CREATED_TIME + " text, " + C_UPDATED_TIME + " text, "
					+ C_CAN_UPLOAD + " text)";
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Logger.i(AlbumsDbHelper.class.getSimpleName() + "#onUpgrade");
			db.execSQL("drop table if exists " + TABLE);
			onCreate(db);

		}

		public void deleteAllAlbums(SQLiteDatabase db) {
			db.delete(TABLE, null, null);
		}

	}

	final AlbumsDbHelper dbHelper;

	public AlbumsData(Context context) {
		Logger.i(AlbumsData.class.getSimpleName() + "#constructor");
		this.dbHelper = new AlbumsDbHelper(context);
	}

	public void close() {
		this.dbHelper.close();
	}

	public void insertOrIgnore(ContentValues values) {
		
		if(values == null){
			return;
		}
		
		Logger.i(AlbumsData.class.getSimpleName() + "#insertOrIgnore " + "values: " + values);

		SQLiteDatabase db = this.dbHelper.getWritableDatabase();

		try {
			if (values != null && db != null) {
				db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			}
		} catch (SQLiteException e) {
			// TODO: understand why NullPointerException thrown here.
		} finally {
			if (db != null) {

				// See:
				// http://stackoverflow.com/questions/7999075/sqlitedatabase-close-function-causing-nullpointerexception-when-multiple-threads

				// db.close();
			}
		}
	}

	/**
	 * @return Cursor for all columns
	 */

	public Cursor getAlbums() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(TABLE, null, null, null, null, null, GET_ALL_ORDER_BY);
	}

	public Cursor getAlbumsByUserId(String userId) {
		if (StringUtil.notEmpty(userId)) {
			SQLiteDatabase db = this.dbHelper.getReadableDatabase();
			return db.query(TABLE, null, C_USER_ID + "=" + userId, null, null, null, GET_ALL_ORDER_BY);
		} else {
			return null;
		}
	}

	public void deleteRowsForUser(String userId) {
		if (StringUtil.notEmpty(userId)) {

			SQLiteDatabase db = this.dbHelper.getWritableDatabase();
			db.delete(TABLE, (C_USER_ID + "=?"), new String[] { userId });// Reference:
																			// SO
																			// question
																			// 9600749
			// equivalent to DELETE FROM table WHERE user_id IN (userId);
		}
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
			db.close();
		}
	}

	/**
	 * 
	 * @param id
	 *            of the status we are looking for
	 * @return Title of the status
	 */

	public String getNotificationById(String id) {
		
		if(!StringUtil.notEmpty(id)){
			return null;
		}
		
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(TABLE, DB_NAME_COLUMNS, C_ID + "=" + C_ID, null, null, null, null);
			try {
				return cursor.moveToNext() ? cursor.getString(0) : null;

			} finally {
				cursor.close();
			}
		} finally {
			db.close();
		}
	}

	public void deleteAllRows() {
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		db.delete(TABLE, null, null);
	}
}