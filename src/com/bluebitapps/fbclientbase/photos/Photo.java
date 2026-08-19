/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.photos;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.bluebitapps.fbclientbase.actions.Likeable;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.friends.Friend;

public class Photo implements Parcelable, Likeable {

	private String mId;
	private String mName;
	private String mPicture;// thumb nail image
	private String mSource;// original image
	private ArrayList<PhotoComment> mComments;
	private ArrayList<Friend> mUsersWhoLike;
	private String mLikesCount;
	private String mUserLikes;

	public Photo() {
		mComments = new ArrayList<PhotoComment>();
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getPicture() {
		
	
		Log.i("jan22", Logger.getClassAndMethod());
		return mPicture;
	}

	public void setPicture(String picture) {
		
		// Notifications of type Photo picture url is type small. Change it to
		// type normal full screen to allow display in ImagePager.
		
		if (picture != null) {
			if (picture.contains("_s")) {
				picture = picture.replace("_s","_n");
			}
		}
		
		this.mPicture = picture;
	}

	// original

	public void setSource(String source) {
		mSource = source;
	}

	public String getSource() {
		return mSource;
	}

	public static Photo fromJson(JSONObject obj) {

		Photo photo = new Photo();
		try {

			if (obj.has("id"))
				photo.setId(obj.getString("id"));
			if (obj.has("name"))
				photo.setName(obj.getString("name"));
			if (obj.has("source"))
				photo.setSource(obj.getString("source"));
			if (obj.has("picture"))
				photo.setPicture(obj.getString("picture"));
		} catch (JSONException e) {
			// TODO: Handle exception.
		}

		return photo;
	}

	public Photo(Parcel in) {
		setId(in.readString());
		setName(in.readString());
		setSource(in.readString());
		setPicture(in.readString());
		setUserLikes(in.readString());
	}

	public static final Parcelable.Creator<Photo> CREATOR = new Parcelable.Creator<Photo>() {
		@Override
		public Photo createFromParcel(Parcel in) {
			return new Photo(in);
		}

		@Override
		public Photo[] newArray(int size) {
			return new Photo[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getId());
		dest.writeString(getName());
		dest.writeString(getPicture());
		dest.writeString(getSource());
		dest.writeString(getUserLikes());
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ArrayList<PhotoComment> getComments() {
		if (mComments == null) {
			mComments = new ArrayList<PhotoComment>();
		}
		return mComments;
	}

	public void setComments(ArrayList<PhotoComment> comments) {
		mComments = comments;
	}

	public void addComment(PhotoComment comment) {
		if (mComments == null) {
			mComments = new ArrayList<PhotoComment>();
		}
		mComments.add(comment);
	}

	public ArrayList<Friend> getUsersWhoLike() {
		if (mUsersWhoLike == null) {
			mUsersWhoLike = new ArrayList<Friend>();
		}
		return mUsersWhoLike;
	}

	public void setUsersWhoLike(ArrayList<Friend> usersWhoLike) {
		mUsersWhoLike = usersWhoLike;
	}

	public void addUserWhoLikes(Friend user) {
		if (mUsersWhoLike != null) {
			mUsersWhoLike = new ArrayList<Friend>();
		}
		mUsersWhoLike.add(user);
	}

	public String getLikesCount() {
		return mLikesCount;
	}

	public void setLikesCount(String value) {
		mLikesCount = value;
	}

	public String getCommentsCount() {
		if (mComments == null) {
			mComments = new ArrayList<PhotoComment>();
		}
		return Integer.toString(mComments.size());
	}

	public void clearComments() {
		mComments.clear();
	}

	@Override
	public String getUserLikes() {
		return mUserLikes;
	}

	@Override
	public void setUserLikes(String value) {
		mUserLikes = value;

	}

	@Override
	public String toString() {
		return "Photo.toString(): id: " + getId() + ", source" + getSource();

	}
}