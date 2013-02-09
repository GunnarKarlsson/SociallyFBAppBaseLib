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

package com.bluebitapps.fbclientbase.events;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bluebitapps.fbclientbase.debug.Logger;

public class BirthdaysData {

	public static final String DB_NAME = "birthdays.db";
	public static final int DB_VERSION = 1;
	public static final String TABLE = "birthdays";
	public static final String C_ID = "_id";
	public static final String C_NAME = "name";
	public static final String C_BIRTHDAY_DATE = "birthdaydate";

	Context context;

	public class BirthdaysDbHelper extends SQLiteOpenHelper {

		public BirthdaysDbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = "create table " + TABLE + " (" + C_ID + " text primary key, " + C_NAME + " text, " + C_BIRTHDAY_DATE + " text)";
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Logger.i(BirthdaysData.class.getSimpleName() + "." + BirthdaysDbHelper.class.getSimpleName() + "#onUpgrade");
			db.execSQL("drop table if exists " + TABLE);
			onCreate(db);

		}

		public void deleteAllEvents(SQLiteDatabase db) {
			db.delete(TABLE, null, null);
		}

	}

	final BirthdaysDbHelper dbHelper;

	public BirthdaysData(Context context) {
		this.dbHelper = new BirthdaysDbHelper(context);
	}

	public void close() {
		this.dbHelper.close();
	}

	public void insertOrIgnore(ContentValues values) {
		
		if(values == null){
			return;
		}
		
		Logger.i(BirthdaysData.class.getSimpleName() + "#insertOrIgnore " + "values: " + values);

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

	public Cursor getBirthdays() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(TABLE, new String[] { C_ID, C_NAME, C_BIRTHDAY_DATE }, null, null, null, null, null);
	}
	
	public void deleteAllRows() {
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		db.delete(TABLE, null, null);
	}
}