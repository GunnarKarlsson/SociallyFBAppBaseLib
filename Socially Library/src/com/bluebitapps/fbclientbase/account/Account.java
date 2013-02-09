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
import android.database.Cursor;

import com.bluebitapps.fbclientbase.debug.Logger;

public class Account {

	private String accessToken;
	private String expires;
	private String isPrimary;
	private String profilePicture;
	private String isCurrentUser;
	private String createdTime;
	private String name;
	private String userId;

	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		values.clear();

		values.put(AccountData.C_NAME, getName());
		values.put(AccountData.C_USER_ID, getUserId());
		values.put(AccountData.C_PROFILE_PICTURE, getProfilePicture());

		values.put(AccountData.C_IS_PRIMARY, getIsPrimary());
		values.put(AccountData.C_IS_CURRENT_USER, getIsCurrentUser());

		values.put(AccountData.C_ACCESS_TOKEN, getAccessToken());
		values.put(AccountData.C_EXPIRES, getExpires());

		values.put(AccountData.C_CREATED_TIME, getCreatedTime());

		return values;
	}

	public void set(Cursor c) {

		try {

			setName(c.getString(c.getColumnIndex(AccountData.C_NAME)));
			setUserId(c.getString(c.getColumnIndex(AccountData.C_USER_ID)));
			setProfilePicture(c.getString(c.getColumnIndex(AccountData.C_PROFILE_PICTURE)));
			setIsPrimary(c.getString(c.getColumnIndex(AccountData.C_IS_PRIMARY)));
			setIsCurrentUser(c.getString(c.getColumnIndex(AccountData.C_IS_CURRENT_USER)));
			setAccessToken(c.getString(c.getColumnIndex(AccountData.C_ACCESS_TOKEN)));
			setExpires(c.getString(c.getColumnIndex(AccountData.C_EXPIRES)));
			setCreatedTime(c.getString(c.getColumnIndex(AccountData.C_CREATED_TIME)));

		} catch (Exception e) {
			Logger.i(Account.class.getSimpleName() + e.toString());
		}
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getExpires() {
		return expires;
	}

	public void setExpires(String expires) {
		this.expires = expires;
	}

	public String getIsPrimary() {
		return isPrimary;
	}

	public void setIsPrimary(String isPrimary) {
		this.isPrimary = isPrimary;
	}

	public String getProfilePicture() {
		return profilePicture;
	}

	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}

	public String getIsCurrentUser() {
		return isCurrentUser;
	}

	public void setIsCurrentUser(String isCurrentUser) {
		this.isCurrentUser = isCurrentUser;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
