/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.newsfeed;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.utils.StringUtil;

public class NewsFeedData {

	static final int DB_VERSION = 1;

	public static final String DB_NAME_WALL = "wall.db";

	public static final String DB_NAME_WALL_OLDER = "wall_older.db";

	public static final String DB_NAME_NEWSFEED = "newsfeed.db";

	public static final String DB_NAME_NEWSFEED_OLDER = "newsfeed_older.db";

	public static final String DB_TABLE_NAME_NEWSFEED_OLDER = "newsfeed_older";

	public static final String DB_TABLE_NAME_NEWSFEED = "newsfeed";

	public static final String DB_TABLE_NAME_WALL = "wall";

	public static final String DB_TABLE_NAME_WALL_OLDER = "wall_older";

	public static final String C_STORY_TAGS = "story_tags";

	public static final String C_APPLICATION_NAME = "application_name";

	public static final String C_USER_LIKES = "user_likes";

	public static final String C_STATUS_TYPE = "status_type";

	public static final String C_OBJECT_ID = "object_id";

	public static final String C_UPDATED_TIME = "updated_time";

	public static final String C_CREATED_TIME = "created_time";

	public static final String C_SHARES_COUNT = "shares_count";

	public static final String C_LIKES_COUNT = "likes_count";

	public static final String C_COMMENTS_COUNT = "comments_count";

	public static final String C_CAPTION = "caption";

	public static final String C_DESCRIPTION = "description";

	public static final String C_LINK = "link";

	public static final String C_NAME = "name";

	public static final String C_MESSAGE = "message";

	public static final String C_STORY = "story";

	public static final String C_ICON = "icon";

	public static final String C_PICTURE = "picture";

	public static final String C_FROM_CATEGORY = "from_category";

	public static final String C_FROM_NAME = "from_name";

	public static final String C_FROM_UID = "from_uid";

	public static final String C_USER_ID = "user_id";

	public static final String C_TYPE = "type";

	public static final String C_ID = BaseColumns._ID;

	public static final String NEWSFEED_DATA_COLUMNS = " (" + C_ID + " text primary key, " + C_USER_ID + " text, " + C_TYPE + " text, " + C_STATUS_TYPE + " text, " + C_FROM_UID + " text, "
			+ C_FROM_NAME + " text, " + C_FROM_CATEGORY + " text, " + C_PICTURE + " text, " + C_OBJECT_ID + " text, " + C_ICON + " text, " + C_LINK + " text, " + C_CAPTION + " text, " + C_NAME
			+ " text, " + C_DESCRIPTION + " text, " + C_STORY + " text, " + C_MESSAGE + " text, " + C_COMMENTS_COUNT + " text, " + C_LIKES_COUNT + " text, " + C_SHARES_COUNT + " text, "
			+ C_USER_LIKES + " text, " + C_CREATED_TIME + " text, " + C_UPDATED_TIME + " text, " + C_STORY_TAGS + " text, " + C_APPLICATION_NAME + " text)";

	public static final String[] MAX_CREATED_AT_COLUMNS = { "max(" + C_CREATED_TIME + ")" };

	public static final String GET_ALL_ORDER_BY = C_CREATED_TIME + " DESC";

	private String mDbName;
	private String mDbTable;
	Context mContext;

	public class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, mDbName, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// Logger.i(DbHelper.class.getSimpleName() + "#onCreate");
			String sql = "create table " + mDbTable + NewsFeedData.NEWSFEED_DATA_COLUMNS;

			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Logger.i(DbHelper.class.getSimpleName() + "#onUpgrade");
			db.execSQL("drop table if exists " + mDbTable);
			onCreate(db);

		}
	}

	final DbHelper dbHelper;

	public static final String REQUEST_WALL_OLDER_FROM_DB = "request_wall_older_from_db";

	public static final String REQUEST_WALL_FROM_DB = "request_wall_from_db";

	public static final String REQUEST_NEWSFEED_OLDER_FROM_DB = "request_newsfeed_older_from_db";

	// request to db for data
	public static final String REQUEST_NEWSFEED_FROM_DB = "request_newsfeed_from_db";

	public NewsFeedData(Context context, String dbName, String dbTable) {
		// Logger.i(NewsFeedData.class.getSimpleName() + "#constructor");
		mDbName = dbName;
		mDbTable = dbTable;
		this.dbHelper = new DbHelper(context);
	}

	public void close() {
		this.dbHelper.close();
	}

	public void insertOrIgnore(ContentValues values) {
		
		if(values == null){
			return;
		}
		
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		try {
			db.insertWithOnConflict(mDbTable, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		} catch (Exception e) {
			Logger.i(e.toString());
		} finally {
			// See:
			// http://stackoverflow.com/questions/7999075/sqlitedatabase-close-function-causing-nullpointerexception-when-multiple-threads
			// db.close();
		}
	}

	/*
	 * public Cursor getNewsFeed() { SQLiteDatabase db =
	 * this.dbHelper.getReadableDatabase(); return db.query(mDbTable, null,
	 * null, null, null, null, NewsFeedConstants.GET_ALL_ORDER_BY); }
	 */

	public Cursor getNewsFeedItemById(String id) {
		if (!StringUtil.notEmpty(id)) {
			return null;
		}
		
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(mDbTable, null, NewsFeedData.C_ID + "=" + id, null, null, null, null);
	}

	/**
	 * @return Timestamp of the latest notification we have in the database
	 */
	public long getLatestNewsFeedItemCreatedTime() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(mDbTable, NewsFeedData.MAX_CREATED_AT_COLUMNS, null, null, null, null, null);
			try {
				return cursor.moveToNext() ? cursor.getLong(0) : Long.MIN_VALUE;
			} finally {
				cursor.close();
			}
		} finally {
			// db.close();
		}
	}

	public Cursor getPostsByUserId(String userId) {

		if (!StringUtil.notEmpty(userId)) {
			return null;
		}

		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		// TODO: userId is null when app is loaded first time, causing it to
		// crash.

		return db.query(mDbTable, null, NewsFeedData.C_USER_ID + "=" + userId, null, null, null, NewsFeedData.GET_ALL_ORDER_BY);
	}

	public void deleteRowsForUser(String userId) {
		if (!StringUtil.notEmpty(userId)) {
			return;
		}

		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		db.delete(mDbTable, (NewsFeedData.C_USER_ID + "=?"), new String[] { userId });
		// Reference:question
		// 9600749
		// equivalent to DELETE FROM table WHERE user_id IN (userId);
	}

	/**
	 * Delete all rows, but don't delete the table.
	 */

	public void deleteAllRows() {
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		db.delete(mDbTable, null, null);
	}

	/**
	 * @RETURN int num of rows in table.
	 */

	public long getRowCount() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		String sql = "SELECT COUNT(*) FROM " + mDbTable;
		SQLiteStatement statement = db.compileStatement(sql);
		long count = statement.simpleQueryForLong();
		return count;
	}
	/*
	 * -- works but never used public void deleteTables(){ SQLiteDatabase db =
	 * this.dbHelper.getWritableDatabase(); String query =
	 * "DROP TABLE IF EXISTS "+ NewsFeedData.DB_TABLE_NAME_NEWSFEED;
	 * db.execSQL(query); query = "DROP TABLE IF EXISTS " +
	 * NewsFeedData.DB_TABLE_NAME_NEWSFEED_OLDER; db.execSQL(query); query =
	 * "DROP TABLE IF EXISTS " + NewsFeedData.DB_TABLE_NAME_WALL;
	 * db.execSQL(query); query = "DROP TABLE IF EXISTS " +
	 * NewsFeedData.DB_TABLE_NAME_WALL_OLDER; db.execSQL(query); }
	 */

}
