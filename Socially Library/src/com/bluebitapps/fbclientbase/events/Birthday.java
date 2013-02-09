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

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

import com.bluebitapps.fbclientbase.debug.Logger;

public class Birthday {
	
	private String mUid;
	private String mName;
	private String mBirthdayDate;
	
	public static Birthday fromJSON(JSONObject obj){
		Birthday bd= new Birthday();

		try {
			bd.setUid(obj.getString("uid"));
			bd.setName(obj.getString("name"));
			bd.setBirthdayDate(obj.getString("birthday_date"));
		} catch (JSONException e) {
			Logger.i(Birthday.class.getSimpleName() + e.toString());
		}

		return bd;
	}
	
	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		values.clear();
		values.put(BirthdaysData.C_ID, getUid());
		values.put(BirthdaysData.C_NAME, getName());
		values.put(BirthdaysData.C_BIRTHDAY_DATE, getBirthdayDate());
		return values;
	}
	
	public void set(Cursor c) {

		try {

			setUid(c.getString(c.getColumnIndex(BirthdaysData.C_ID)));
			Logger.i(Birthday.class.getSimpleName() + "#getUid: " + getUid());
			setName(c.getString(c.getColumnIndex(BirthdaysData.C_NAME)));
			Logger.i(Birthday.class.getSimpleName() + "#getName: " + getName());
			setBirthdayDate(c.getString(c.getColumnIndex(BirthdaysData.C_BIRTHDAY_DATE)));
			Logger.i(Birthday.class.getSimpleName() + "#getBirthdayDate: " + getBirthdayDate());

		} catch (Exception e) {
			//TODO: use more precise exception.
			Logger.i(Birthday.class.getSimpleName() + "#set" + e.toString());	
		}
	}
	
	public String getUid() {
		return mUid;
	}
	public void setUid(String uid) {
		this.mUid = uid;
	}
	public String getName() {
		return mName;
	}
	public void setName(String name) {
		this.mName = name;
	}
	public String getBirthdayDate() {
		return mBirthdayDate;
	}
	public void setBirthdayDate(String birthdayDate) {
		this.mBirthdayDate = birthdayDate;
	}

}