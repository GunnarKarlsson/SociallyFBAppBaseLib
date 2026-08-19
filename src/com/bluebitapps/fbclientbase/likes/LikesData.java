/*
 *  Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.likes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.utils.StringUtil;

public class LikesData {

	public static final String DB_NAME = "likes.db";
	public static final int DB_VERSION = 1;
	public static final String TABLE = "likes";
	public static final String C_ID = "_id";
	public static final String C_USER_ID = "user_id";
	public static final String C_OBJECT_ID = "object_id";
	public static final String C_OBJECT_NAME = "object_name";
	public static final String C_CATEGORY = "category";
	public static final String C_CREATED_TIME = "created_time";

	Context context;

	// private static final String GET_ALL_ORDER_BY = C_CREATED_TIME + " DESC";

	// private static final String[] MAX_CREATED_AT_COLUMNS = { "max("
	// + C_CREATED_TIME + ")" };

	// private static final String[] DB_NAME_COLUMNS = { C_NAME };

	// dbHelper implementation
	public class LikesDbHelper extends SQLiteOpenHelper {

		public LikesDbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Logger.i(LikesDbHelper.class.getSimpleName() + "#onCreate");
			String sql = "create table " + TABLE + " (" + C_ID + " text primary key, " + C_USER_ID + " text, " + C_OBJECT_ID + " text, " + C_OBJECT_NAME + " text, " + C_CATEGORY + " text, "
					+ C_CREATED_TIME + " text)";
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Logger.i(LikesDbHelper.class.getSimpleName() + "#onUpgrade");
			db.execSQL("drop table if exists " + TABLE);
			onCreate(db);

		}

		public void deleteAllAlbums(SQLiteDatabase db) {
			db.delete(TABLE, null, null);
		}

	}

	final LikesDbHelper dbHelper;

	public LikesData(Context context) {
		Logger.i(LikesData.class.getSimpleName() + "#constructor");
		this.dbHelper = new LikesDbHelper(context);
	}

	public void close() {
		this.dbHelper.close();
	}

	public void insertOrIgnore(ContentValues values) {
		
		if(values == null){
			return;
		}
		
		Logger.i(LikesData.class.getSimpleName() + "#insertOrIgnore " + "values: " + values);

		SQLiteDatabase db = this.dbHelper.getWritableDatabase();

		try {
			db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

		} finally {
			
			// See:
			// http://stackoverflow.com/questions/7999075/sqlitedatabase-close-function-causing-nullpointerexception-when-multiple-threads
			
			//db.close();
		}
	}

	/**
	 * @return Cursor for all columns
	 */

	public Cursor getLikesByUserId(String userId) {
		if(!StringUtil.notEmpty(userId)){
			return null;
		}
		
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(TABLE, null, C_USER_ID + "=" + userId, null, null, null, null);
	}

	public void deleteRowsForUser(String userId) {
		
		if(!StringUtil.notEmpty(userId)){
			return;
		}
		
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		db.delete(TABLE, (C_USER_ID + "=?"), new String[] { userId });
		/*
		 * Reference:SO question 9600749 equivalent to DELETE FROM table WHERE
		 * user_id IN (userId);
		 */
	}
	
	public void deleteAllRows() {
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		db.delete(TABLE, null, null);
	}

}