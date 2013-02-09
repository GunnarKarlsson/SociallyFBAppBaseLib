/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.albums;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.photos.Photo;

public class Album {

	private String mId;
	private String mFromUserName;
	private String mFromUserId;
	private String mName;
	private String mLink;
	private String mCoverPhoto;
	private String mCount;
	private String mCreatedTime;
	private String mUpdatedTime;
	private String mCanUpload;
	private ArrayList<Photo> mPhotos;
	private String mUserId;

	public Album() {
	}

	public static final String ALBUM_ID_KEY = "album id key";
	public static final String ALBUM_NAME_KEY = "album name key";
	public static final String ALBUM_PHOTO_COUNT = "album photo count";
	
	/*
	 * uid is passed into this method since it should not be possible to create an albums without an album owner id reference.
	 */
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return mName;
	}
	
	public static Album fromJson(JSONObject obj, Context context, String uid) {
		Album album = new Album();

		try {
			album.setId(obj.getString("id"));
			JSONObject from = obj.getJSONObject("from");
			String userName = from.getString("name");
			String userId = from.getString("id");
			album.setFromUserName(userName);
			album.setFromUserId(userId);
			album.setName(obj.getString("name"));
			album.setLink(obj.getString("link"));
			album.setCoverPhoto(obj.getString("cover_photo"));
			album.setCount(obj.getString("count"));
			Log.i("jan9", Logger.getClassAndMethod() + obj.getString("count"));
			album.setCreatedTime(obj.getString("created_time"));
			album.setUpdatedTime(obj.getString("updated_time"));
			album.setCanUpload(obj.getString("can_upload"));
			album.setUserId(uid);

		} catch (JSONException e) {
			// TODO: handle exception
		}

		album.setCoverPhoto("https://graph.facebook.com/"
				+ album.getId().toString()
				+ "/picture?type=album&access_token="
				+ ((FBClientApplication) context.getApplicationContext())
						.getFBConnection().getFacebook().getAccessToken());
		return album;
	}

	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		values.clear();
		values.put(AlbumsData.C_ID, getId());
		values.put(AlbumsData.C_FROM_NAME, getFromUserName());
		values.put(AlbumsData.C_FROM_UID, getFromUserId());
		values.put(AlbumsData.C_NAME, getName());
		values.put(AlbumsData.C_LINK, getLink());
		values.put(AlbumsData.C_COVER_PHOTO, getCoverPhoto());
		values.put(AlbumsData.C_COUNT, getCount());
		values.put(AlbumsData.C_CREATED_TIME, getCreatedTime());
		values.put(AlbumsData.C_UPDATED_TIME, getUpdatedTime());
		values.put(AlbumsData.C_CAN_UPLOAD, getCanUpload());
		values.put(AlbumsData.C_USER_ID, getUserId());
		return values;
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		this.mId = id;
	}

	public String getFromUserName() {
		return mFromUserName;
	}

	public void setFromUserName(String fromUserName) {
		this.mFromUserName = fromUserName;
	}

	public String getFromUserId() {
		return mFromUserId;
	}

	public void setFromUserId(String fromUserId) {
		this.mFromUserId = fromUserId;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getLink() {
		return mLink;
	}

	public void setLink(String link) {
		this.mLink = link;
	}

	public String getCoverPhoto() {
		return mCoverPhoto;
	}

	public void setCoverPhoto(String coverPhoto) {
		this.mCoverPhoto = coverPhoto;
	}

	public String getCount() {
		return mCount;
	}

	public void setCount(String count) {
		this.mCount = count;
	}

	public String getCreatedTime() {
		return mCreatedTime;
	}

	public void setCreatedTime(String createdTime) {
		this.mCreatedTime = createdTime;
	}

	public String getUpdatedTime() {
		return mUpdatedTime;
	}

	public void setUpdatedTime(String updatedTime) {
		this.mUpdatedTime = updatedTime;
	}

	public String getCanUpload() {
		return mCanUpload;
	}

	public void setCanUpload(String canUpload) {
		this.mCanUpload = canUpload;
	}

	public ArrayList<Photo> getPhotos() {
		return mPhotos;
	}

	public void setPhotos(ArrayList<Photo> photos) {
		this.mPhotos = photos;
	}
	
	public String getUserId(){
		return mUserId;
	}
	
	public void setUserId(String userId){
		this.mUserId = userId;
	}
}
