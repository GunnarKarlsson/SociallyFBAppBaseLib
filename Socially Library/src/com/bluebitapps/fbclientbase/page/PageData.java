/* Copyright 2012 Gunnar Karlsson.
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

package com.bluebitapps.fbclientbase.page;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.utils.StringUtil;

public class PageData {

	static final String DB_NAME = "pagedata.db";
	static final int DB_VERSION = 1;
	public static final String TABLE = "pagedata";
	public static final String C_ID = "_id";
	public static final String C_NAME = "name";
	public static final String C_CATEGORY = "category";
	public static final String C_DESCRIPTION = "description";
	public static final String C_CITY = "city";
	public static final String C_COUNTRY = "country";
	public static final String C_LATITUDE = "latitude";
	public static final String C_LONGITUDE = "longitude";
	public static final String C_PROFILE_PIC = "profile_pic";
	public static final String C_FAN_COUNT = "fan_count";
	public static final String C_TALKING_ABOUT = "talking_about";
	public static final String C_COVER_PHOTO = "cover_photo";
	public static final String C_GENERAL_INFO = "general_info";

	Context context;

	public class PageDbHelper extends SQLiteOpenHelper {

		public PageDbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Logger.i(PageData.class.getSimpleName() + PageDbHelper.class.getSimpleName() + "#onCreate");
			String sql = "create table " 
		+ TABLE + " (" + C_ID + " text primary key, " 
		+ C_NAME + " text, " 
		+ C_CATEGORY + " text, "
		+ C_DESCRIPTION + " text,"
		+ C_CITY + " text, "
		+ C_COUNTRY + " text, "
		+ C_LATITUDE + " text, "
		+ C_LONGITUDE + " text, "
		+ C_PROFILE_PIC + " text, "
		+ C_FAN_COUNT + " text, " 
		+ C_TALKING_ABOUT + " text, " 
		+ C_COVER_PHOTO + " text, " 
		+ C_GENERAL_INFO + " text)";
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Logger.i(PageData.class.getSimpleName() + "." + PageDbHelper.class.getSimpleName() + "#onUpgrade");
			db.execSQL("drop table if exists " + TABLE);
			onCreate(db);

		}

		public void deleteAllEvents(SQLiteDatabase db) {
			db.delete(TABLE, null, null);
		}

	}

	final PageDbHelper dbHelper;

	public PageData(Context context) {
		this.dbHelper = new PageDbHelper(context);
	}

	public void close() {
		this.dbHelper.close();
	}

	public void insertOrIgnore(ContentValues values) {
		
		if(values == null){
			return;
		}

		Logger.i(PageData.class.getSimpleName() + "#insertOrIgnore " + "values: " + values);

		SQLiteDatabase db = this.dbHelper.getWritableDatabase();

		try {
			db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		} finally {
			
			// See:
			// http://stackoverflow.com/questions/7999075/sqlitedatabase-close-function-causing-nullpointerexception-when-multiple-threads
			
			//db.close();
		}
	}

	public Cursor getPageById(String id) {
		
		if(!StringUtil.notEmpty(id)){
			return null;
		}
		
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();

		Cursor c = null;

		try {
			c = db.query(TABLE, null, C_ID + "=" + id, null, null, null, null);
		} catch (Exception e) {
			//TODO: use precise Exception
			Logger.i(PageData.class.getSimpleName() + "#getPageById: " + e.toString());
		}

		return c;
	}
	
	public void deleteAllRows() {
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		db.delete(TABLE, null, null);
	}

}