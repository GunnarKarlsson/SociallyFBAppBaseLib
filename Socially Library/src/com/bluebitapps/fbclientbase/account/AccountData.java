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

package com.bluebitapps.fbclientbase.account;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.utils.StringUtil;

public class AccountData {

	static final String DB_NAME = "accounts.db";
	static final int DB_VERSION = 1;

	private static String TABLE = "accounts";

	public static final String C_ID = BaseColumns._ID;

	public static final String C_NAME = "name";
	public static final String C_USER_ID = "user_id";
	public static final String C_PROFILE_PICTURE = "picture";

	public static final String C_IS_PRIMARY = "is_primary";
	public static final String C_IS_CURRENT_USER = "is_current_user";

	public static final String C_ACCESS_TOKEN = "access_token";
	public static final String C_EXPIRES = "expires";

	public static final String C_CREATED_TIME = "created_time";

	Context context;

	private static final String GET_ALL_ORDER_BY = C_CREATED_TIME + " DESC";

	// dbHelper implementation
	public class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = "create table " + TABLE + " (" + C_ID + " text primary key, " + C_NAME + " text, " + C_USER_ID + " text, " + C_PROFILE_PICTURE + " text, " + C_IS_PRIMARY + " text, "
					+ C_IS_CURRENT_USER + " text, " + C_ACCESS_TOKEN + " text, " + C_EXPIRES + " text, " + C_CREATED_TIME + " text)";

			db.execSQL(sql);
				Logger.i(DbHelper.class.getSimpleName() + "#onCreate");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Logger.i(AccountData.class.getSimpleName() + "." + DbHelper.class.getSimpleName() + "#onUpgrade");
			db.execSQL("drop table if exists " + TABLE);
			onCreate(db);

		}
	}

	final DbHelper dbHelper;

	public AccountData(Context context) {
		this.dbHelper = new DbHelper(context);
	}

	public void close() {
		this.dbHelper.close();
	}

	public void insertOrIgnore(ContentValues values) {
		
		if(values == null){
			return;
		}
		
		Logger.i(AccountData.class.getSimpleName() + "#insertOrIgnore " + "values: " + values);
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		try {
			db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		} catch (Exception e) {
			Logger.i(e.toString());
		} finally {
			db.close();
		}
	}

	public Cursor getAccounts() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(TABLE, null, null, null, null, null, GET_ALL_ORDER_BY);
	}

	public Cursor getAccountsById(String id) {
		
		if(!StringUtil.notEmpty(id)){
			return null;
		}
		
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(TABLE, null, C_ID + "=" + id, null, null, null, null);
	}

	/**
	 * @RETURN int num of rows in table.
	 */

	public long getRowCount() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		String sql = "SELECT COUNT(*) FROM " + TABLE;
		SQLiteStatement statement = db.compileStatement(sql);
		long count = statement.simpleQueryForLong();
		return count;
	}
}