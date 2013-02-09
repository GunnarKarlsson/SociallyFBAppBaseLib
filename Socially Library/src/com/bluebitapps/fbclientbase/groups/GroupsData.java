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

package com.bluebitapps.fbclientbase.groups;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.utils.StringUtil;

public class GroupsData {

	public static final String DB_NAME = "groups.db";
	public static final int DB_VERSION = 1;
	public static final String TABLE = "groups";

	public static final String C_ID = "_id";
	public static final String C_NAME = "name";
	public static final String C_VERSION = "version";
	public static final String C_UNREAD = "unread";
	public static final String C_BOOKMARK_ORDER = "bookmark_order";
	public static final String C_DESCRIPTION = "description";

	Context context;

	private static final String GET_ALL_ORDER_BY = C_BOOKMARK_ORDER + " DESC";

	public class GroupsDbHelper extends SQLiteOpenHelper {

		public GroupsDbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = "create table " + TABLE + " (" + C_ID + " text primary key, " + C_NAME + " text, " + C_VERSION + " text, " + C_UNREAD + " text, " + C_BOOKMARK_ORDER + " text, " + C_DESCRIPTION + " text)";
			db.execSQL(sql);
			Logger.i(GroupsDbHelper.class.getSimpleName() + "#onCreate");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop table if exists " + TABLE);
			Logger.i(GroupsDbHelper.class.getSimpleName() + "#onUpgrade");
			onCreate(db);

		}

		public void deleteAllGroups(SQLiteDatabase db) {
			db.delete(TABLE, null, null);
		}

	}

	final GroupsDbHelper dbHelper;

	public GroupsData(Context context) {
		this.dbHelper = new GroupsDbHelper(context);
		Logger.i(GroupsData.class.getSimpleName() + "#constructor");
	}

	public void close() {
		this.dbHelper.close();
	}

	public void insertOrIgnore(ContentValues values) {
		
		if(values == null){
			return;
		}

		Logger.i(GroupsData.class.getSimpleName() + "#insertOrIgnore " + "values: " + values);

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

	public Cursor getGroups() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(TABLE, null, null, null, null, null, GET_ALL_ORDER_BY);
	}
	
	public Cursor getGroupById(String id){
		
		if(!StringUtil.notEmpty(id)){
			return null;
		}
		
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(TABLE, null, C_ID + "=" + id, null, null, null, null);
	}
	
	public void deleteAllRows() {
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		db.delete(TABLE, null, null);
	}
}