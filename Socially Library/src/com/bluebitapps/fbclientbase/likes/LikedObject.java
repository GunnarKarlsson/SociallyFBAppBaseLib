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

package com.bluebitapps.fbclientbase.likes;

import org.json.JSONException;
import org.json.JSONObject;

import com.bluebitapps.fbclientbase.FBClientApplication;

import android.content.ContentValues;

public class LikedObject {
	
	private String userId;
	private String objectId;
	private String objectName;
	private String category;
	private String createdTime;
	
	public static LikedObject fromJSON(JSONObject obj, String personWhoLiked){
		LikedObject likedObject= new LikedObject();

		try {
			likedObject.setUserId(personWhoLiked);
			likedObject.setObjectName(obj.getString("name"));
			likedObject.setObjectId(obj.getString("id"));
			likedObject.setCategory(obj.getString("category"));
			likedObject.setCreatedTime(obj.getString("created_time"));

		} catch (JSONException e) {
			// TODO: handle exception
		}

		return likedObject;
	}
	
	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		values.clear();
		values.put(LikesData.C_ID, getObjectId());
		values.put(LikesData.C_USER_ID, getUserId());
		values.put(LikesData.C_OBJECT_NAME, getObjectName());
		values.put(LikesData.C_CATEGORY, getCategory());
		values.put(LikesData.C_CREATED_TIME, getCreatedTime());
		return values;
	}
	
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}
	
	public String getPicture(FBClientApplication app) {
		String token = app.getFBConnection().getFacebook().getAccessToken();

		return "https://graph.facebook.com/" + getObjectId()
				+ "/picture?access_token=" + token;
	}
}
