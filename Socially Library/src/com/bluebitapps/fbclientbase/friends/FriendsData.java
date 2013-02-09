/*******************************************************************************
 * Copyright 2012 Gunnar Karlsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.bluebitapps.fbclientbase.friends;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bluebitapps.fbclientbase.debug.Logger;

public class FriendsData {

	public static final String DB_NAME = "friends.db";
	public static final int DB_VERSION = 1;
	public static final String TABLE = "friends";
	public static final String C_ID = "_id";
	public static final String C_NAME = "name";

	Context context;

	private static final String GET_ALL_ORDER_BY = C_NAME + " DESC";

	public class FriendsDbHelper extends SQLiteOpenHelper {

		public FriendsDbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Logger.i(FriendsData.class.getSimpleName() + "." + FriendsDbHelper.class.getSimpleName() + "#onCreate");
			String sql = "create table " + TABLE + " (" + C_ID + " text primary key, " + C_NAME + " text)";
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Logger.i(FriendsData.class.getSimpleName() + "." + FriendsDbHelper.class.getSimpleName() + "#onUpgrade");
			db.execSQL("drop table if exists " + TABLE);
			onCreate(db);
		}

		public void deleteAllEvents(SQLiteDatabase db) {
			db.delete(TABLE, null, null);
		}

	}

	final FriendsDbHelper dbHelper;

	public FriendsData(Context context) {
		this.dbHelper = new FriendsDbHelper(context);
	}

	public void close() {
		this.dbHelper.close();
	}

	public void insertOrIgnore(ContentValues values) {
		
		if(values == null){
			return;
		}
		
		Logger.i(FriendsData.class.getSimpleName() + "#insertOrIgnore " + "values: " + values);

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

	public Cursor getFriends() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(TABLE, null, null, null, null, null, GET_ALL_ORDER_BY);
	}
	
	public void deleteAllRows() {
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		db.delete(TABLE, null, null);
	}
}