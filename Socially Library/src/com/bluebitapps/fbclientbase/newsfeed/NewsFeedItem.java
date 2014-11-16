/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.newsfeed;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.actions.Likeable;

public class NewsFeedItem implements Parcelable, Likeable {

	// read & write in this order
	private String mId;
	private String mName;
	private String mType;
	private String mFromName;
	private String mFromId;
	private String mFromCategory;

	private String mPicture;
	private String mLink;
	private String mCaption;
	private String mMessage;
	private String mDescription;
	private String mStory;
	private String mIcon;
	private String mCreatedTime;
	private String mUpdatedTime;
	private String mSharesCount;
	private String mLikesCount;
	private String mCommentsCount;
	private String mObjectId;
	private String mStatusType;
	private String mUserLikes;
	private String mApplicationName;
	private String mStoryTags;
	private String mUserId;
	public static final String APPLICATION_NAME_LIKES = "Likes";
	public static final String APPLICATION_NAME_INSTAGRAM = "Instagram";
	public final static String NEWSFEED_ITEM_STATUS_TYPE_ADDED_PHOTOS = "added_photos";
	public final static String NEWSFEED_ITEM_STATUS_TYPE_TAGGED_IN_PHOTO = "tagged_in_photo";
	public final static String NEWSFEED_ITEM_STATUS_TYPE_SHARED_STORY = "shared_story";
	public final static String NEWSFEED_ITEM_STATUS_TYPE_MOBILE_STATUS_UPDATE = "mobile_status_update";
	public final static String NEWSFEED_ITEM_TYPE_VIDEO = "video";
	public final static String NEWSFEED_ITEM_TYPE_STATUS = "status";
	public final static String NEWSFEED_ITEM_TYPE_LINK = "link";
	public final static String NEWSFEED_ITEM_TYPE_CHECKIN = "checkin";
	// item types
	public final static String NEWSFEED_ITEM_TYPE_PHOTO = "photo";

	private static final String TAG = "globalTag";

	public NewsFeedItem() {
	}

	public NewsFeedItem(Parcel in) {

		setId(in.readString());
		setName(in.readString());
		setType(in.readString());
		setFromName(in.readString());
		setFromId(in.readString());
		setFromCategory(in.readString());
		setPicture(in.readString());
		setLink(in.readString());
		setCaption(in.readString());
		setMessage(in.readString());
		setDescription(in.readString());
		setStory(in.readString());
		setIcon(in.readString());
		setCreatedTime(in.readString());
		setUpdatedTime(in.readString());
		setSharesCount(in.readString());
		setLikesCount(in.readString());
		setCommentsCount(in.readString());
		setObjectId(in.readString());
		setStatusType(in.readString());
		setUserLikes(in.readString());
		setApplicationName(in.readString());
		setStoryTags(in.readString());
		setUserId(in.readString());

	}

	public static final Parcelable.Creator<NewsFeedItem> CREATOR = new Parcelable.Creator<NewsFeedItem>() {
		@Override
		public NewsFeedItem createFromParcel(Parcel in) {
			return new NewsFeedItem(in);
		}

		@Override
		public NewsFeedItem[] newArray(int size) {
			return new NewsFeedItem[size];
		}
	};

	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(getId());
		dest.writeString(getName());
		dest.writeString(getType());
		dest.writeString(getFromName());
		dest.writeString(getFromId());
		dest.writeString(getFromCategory());
		dest.writeString(getPicture());
		dest.writeString(getLink());
		dest.writeString(getCaption());
		dest.writeString(getMessage());
		dest.writeString(getDescription());
		dest.writeString(getStory());
		dest.writeString(getIcon());
		dest.writeString(getCreatedTime());
		dest.writeString(getUpdatedTime());
		dest.writeString(getSharesCount());
		dest.writeString(getLikesCount());
		dest.writeString(getCommentsCount());
		dest.writeString(getObjectId());
		dest.writeString(getStatusType());
		dest.writeString(getUserLikes());
		dest.writeString(getApplicationName());
		dest.writeString(getStoryTags());
		dest.writeString(getUserId());
	}

	public static NewsFeedItem fromJson(JSONObject obj, String userId) {

		if (obj == null)
			return null;

		NewsFeedItem item = new NewsFeedItem();

		item.setUserId(userId);

		try {

			if (obj.has("id")) {
				item.setId(obj.getString("id"));
			}

			if (obj.has("type")) {
				item.setType(obj.getString("type"));
			}

			if (obj.has("status_type")) {
				item.setStatusType(obj.getString("status_type"));
			}
			
			if(obj.has("type")){
				if("checkin".equalsIgnoreCase(obj.getString("type"))){
					item.setStatusType(obj.getString("type"));//Checkin Post has type but not status_type
				}
			}
			
			//TODO: if there is no status_type but type == "status" it should be classified as status update to allows full from text description in item renderer

			if (obj.has("from")) {
				JSONObject from = obj.getJSONObject("from");

				if (from.has("id")) {
					item.setFromId(from.getString("id"));
				}

				if (from.has("name")) {
					item.setFromName(from.getString("name"));
				}

				if (from.has("category")) {
					item.setFromCategory(from.getString("category"));
				}
			}

			if (obj.has("object_id")) {
				item.setObjectId(obj.getString("object_id"));
			}

			if (obj.has("picture")) {
				item.setPicture(obj.getString("picture"));
			}

			if (obj.has("icon")) {
				item.setIcon(obj.getString("icon"));
			}

			if (obj.has("link")) {
				item.setLink(obj.getString("link"));
			}

			if (obj.has("name")) {
				item.setName(obj.getString("name"));
			}

			if (obj.has("caption")) {
				item.setCaption(obj.getString("caption"));
			}

			if (obj.has("description")) {
				item.setDescription(obj.getString("description"));
			}

			if (obj.has("message")) {
				item.setMessage(obj.getString("message"));
			}

			if (obj.has("story")) {
				item.setStory(obj.getString("story"));
			}

			if (obj.has("story_tags")) {
				// Log.i(TAG, "setting story tags: " +
				// obj.getString("story_tags"));
				item.setStoryTags(obj.getString("story_tags"));
			}

			if (obj.has("comments")) {

				JSONObject comments = obj.getJSONObject("comments");

				if (comments.has("count")) {
					item.setCommentsCount(comments.getString("count"));
				}
				
				if (comments.has("summary")) {
					JSONObject summary = comments.getJSONObject("summary");
					if (summary.has("total_count")) {
						item.setCommentsCount(summary.getString("total_count"));
					}
				}
			}

			if (obj.has("likes")) {

				JSONObject likes = obj.getJSONObject("likes");
				
				if (likes.has("count")) {
					item.setLikesCount(likes.getString("count"));
				}
				
				if (likes.has("summary")) {
					JSONObject summary = likes.getJSONObject("summary");
					if (summary.has("total_count")) {
						item.setLikesCount(summary.getString("total_count"));
					}
				}
			}
			/*
			 * if (obj.has("shares_count")) {
			 * item.setSharesCount(obj.getString("shares_count")); }
			 */

			if (obj.has("created_time")) {
				item.setCreatedTime(obj.getString("created_time"));
				Log.i("nftest", obj.getString("created_time"));
			}
			if (obj.has("updated_time")) {
				item.setUpdatedTime(obj.getString("updated_time"));
			}
			if (obj.has("application")) {
				JSONObject app = obj.getJSONObject("application");
				if (app.has("name")) {
					item.setApplicationName(app.getString("name"));
				}
			}

		} catch (JSONException e) {

			Log.i(TAG, NewsFeedItem.class.getSimpleName() + "JSONException() " + e.toString());
		}

		return item;
	}

	public static ArrayList<StoryTag> getStoryTagsFromJSON(String storyTagsString) {
		ArrayList<StoryTag> storyTags = new ArrayList<StoryTag>();
		try {
			JSONObject obj = new JSONObject(storyTagsString);

			Iterator<?> keys = obj.keys();

			while (keys.hasNext()) {
				Log.i(TAG, "while keys.hasNext()");
				String key = (String) keys.next();

				if (obj.get(key) instanceof JSONArray) {
					// Log.i(TAG, "storytag key: " + obj.get(key).toString());

					StoryTag storyTag = new StoryTag();
					JSONArray jsonArr = (JSONArray) obj.get(key);
					JSONObject tagObj = jsonArr.getJSONObject(0);
					storyTag.setUserId(tagObj.getString("id"));
					storyTag.setName(tagObj.getString("name"));
					storyTag.setLength(tagObj.getInt("length"));
					storyTag.setOffset(tagObj.getInt("offset"));
					storyTag.setType(tagObj.getString("type"));
					storyTags.add(storyTag);
				}
			}

		} catch (JSONException e) {
			Log.i(TAG, "JSONEXception: " + e.toString());
		}

		return storyTags;
	}

	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		values.clear();

		values.put(NewsFeedData.C_ID, getId());
		values.put(NewsFeedData.C_TYPE, getType());
		values.put(NewsFeedData.C_STATUS_TYPE, getStatusType());

		values.put(NewsFeedData.C_FROM_UID, getFromId());
		values.put(NewsFeedData.C_FROM_NAME, getFromName());
		values.put(NewsFeedData.C_FROM_CATEGORY, getFromCategory());

		values.put(NewsFeedData.C_PICTURE, getPicture());
		values.put(NewsFeedData.C_ICON, getIcon());
		values.put(NewsFeedData.C_OBJECT_ID, getObjectId());

		values.put(NewsFeedData.C_LINK, getLink());
		values.put(NewsFeedData.C_CAPTION, getCaption());
		values.put(NewsFeedData.C_DESCRIPTION, getDescription());
		values.put(NewsFeedData.C_STORY, getStory());
		values.put(NewsFeedData.C_STORY_TAGS, getStoryTags());
		values.put(NewsFeedData.C_MESSAGE, getMessage());
		values.put(NewsFeedData.C_NAME, getName());
		// Log.i(TAG, "getCommentsCount():" +getCommentsCount());
		values.put(NewsFeedData.C_COMMENTS_COUNT, getCommentsCount());
		// Log.i(TAG, "getLikesCount(): "+getLikesCount());
		values.put(NewsFeedData.C_LIKES_COUNT, getLikesCount());
		values.put(NewsFeedData.C_SHARES_COUNT, getSharesCount());

		values.put(NewsFeedData.C_CREATED_TIME, getCreatedTime());
		values.put(NewsFeedData.C_UPDATED_TIME, getUpdatedTime());

		values.put(NewsFeedData.C_USER_LIKES, getUserLikes());
		values.put(NewsFeedData.C_APPLICATION_NAME, getApplicationName());
		values.put(NewsFeedData.C_USER_ID, getUserId());

		return values;
	}

	public void set(Cursor c) {

		try {

			setId(c.getString(c.getColumnIndex(NewsFeedData.C_ID)));
			setType(c.getString(c.getColumnIndex(NewsFeedData.C_TYPE)));
			setStatusType(c.getString(c.getColumnIndex(NewsFeedData.C_STATUS_TYPE)));

			setFromId(c.getString(c.getColumnIndex(NewsFeedData.C_FROM_UID)));
			setFromName(c.getString(c.getColumnIndex(NewsFeedData.C_FROM_NAME)));
			setFromCategory(c.getString(c.getColumnIndex(NewsFeedData.C_FROM_CATEGORY)));

			setPicture(c.getString(c.getColumnIndex(NewsFeedData.C_PICTURE)));
			setIcon(c.getString(c.getColumnIndex(NewsFeedData.C_ICON)));
			setObjectId(c.getString(c.getColumnIndex(NewsFeedData.C_OBJECT_ID)));

			setLink(c.getString(c.getColumnIndex(NewsFeedData.C_LINK)));
			setCaption(c.getString(c.getColumnIndex(NewsFeedData.C_CAPTION)));
			setDescription(c.getString(c.getColumnIndex(NewsFeedData.C_DESCRIPTION)));
			setStory(c.getString(c.getColumnIndex(NewsFeedData.C_STORY)));
			setStoryTags(c.getString(c.getColumnIndex(NewsFeedData.C_STORY_TAGS)));
			setMessage(c.getString(c.getColumnIndex(NewsFeedData.C_MESSAGE)));
			setName(c.getString(c.getColumnIndex(NewsFeedData.C_NAME)));
			// Log.i(TAG, "c.getString...Comments" +
			// c.getString(c.getColumnIndex(NewsFeedData.C_COMMENTS_COUNT)));
			setCommentsCount(c.getString(c.getColumnIndex(NewsFeedData.C_COMMENTS_COUNT)));
			// Log.i(TAG, "c.getString...Likes" +
			// c.getString(c.getColumnIndex(NewsFeedData.C_LIKES_COUNT)));
			setLikesCount(c.getString(c.getColumnIndex(NewsFeedData.C_LIKES_COUNT)));
			setSharesCount(c.getString(c.getColumnIndex(NewsFeedData.C_SHARES_COUNT)));

			setCreatedTime(c.getString(c.getColumnIndex(NewsFeedData.C_CREATED_TIME)));
			setUpdatedTime(c.getString(c.getColumnIndex(NewsFeedData.C_UPDATED_TIME)));
			setUserLikes(c.getString(c.getColumnIndex(NewsFeedData.C_USER_LIKES)));
			setApplicationName(c.getString(c.getColumnIndex(NewsFeedData.C_APPLICATION_NAME)));
			setUserId(c.getString(c.getColumnIndex(NewsFeedData.C_USER_ID)));

		} catch (Exception e) {

			Log.i(TAG, NewsFeedItem.class.getSimpleName() + "Exception() " + e.toString());
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	/*
	 * Getters and setters
	 */

	public void setUserId(String userId) {
		mUserId = userId;
	}

	public String getUserId() {
		return mUserId;
	}

	// id
	public void setId(String id) {
		mId = id;
	}

	public String getId() {
		return mId;
	}

	// object id
	public void setObjectId(String id) {
		mObjectId = id;
	}

	public String getObjectId() {
		return mObjectId;
	}

	// status_type
	public void setStatusType(String type) {
		mStatusType = type;
	}

	public String getStatusType() {
		return mStatusType;
	}

	// profile image

	public String getProfilePicture() {

		boolean isTablet = FBClientApplication.getApplication().getResources().getBoolean(R.bool.isTablet);

		if (isTablet) {
			return "https://graph.facebook.com/" + getFromId() + "/picture?width=100&height=100";
		} else {
			return "https://graph.facebook.com/" + getFromId() + "/picture?width=50&height=50";
		}

	}

	// name

	public void setName(String name) {
		mName = name;
	}

	public String getName() {
		return mName;
	}

	// Type

	public String getType() {
		return mType;
	}

	public void setType(String type) {
		this.mType = type;
	}

	// From Name

	public String getFromName() {
		return mFromName;
	}

	public void setFromName(String fromName) {
		this.mFromName = fromName;
	}

	// From Id

	public String getFromId() {
		return mFromId;
	}

	public void setFromId(String fromId) {
		this.mFromId = fromId;
	}

	// From Category

	public String getFromCategory() {
		return mFromCategory;
	}

	public void setFromCategory(String fromCategory) {
		this.mFromCategory = fromCategory;
	}

	// Picture

	public String getPicture() {
		return mPicture;
	}

	public void setPicture(String picture) {
		this.mPicture = picture;
	}

	// Icon

	public String getIcon() {
		return mIcon;
	}

	public void setIcon(String icon) {
		this.mIcon = icon;
	}

	// Link

	public String getLink() {
		return mLink;
	}

	public void setLink(String link) {
		this.mLink = link;
	}

	// Caption

	public String getCaption() {
		return mCaption;
	}

	public void setCaption(String caption) {
		this.mCaption = caption;
	}

	// Description

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		this.mDescription = description;
	}

	// Message

	public void setMessage(String message) {
		this.mMessage = message;
	}

	public String getMessage() {
		return mMessage;
	}

	// Story

	public String getStory() {
		return mStory;
	}

	public void setStory(String story) {
		this.mStory = story;
	}

	// Comments Count

	public String getCommentsCount() {
		return mCommentsCount;
	}

	public void setCommentsCount(String commentsCount) {
		this.mCommentsCount = commentsCount;
	}

	// Likes Count

	public String getLikesCount() {
		return mLikesCount;
	}

	public void setLikesCount(String likesCount) {
		this.mLikesCount = likesCount;
	}

	// Shares Count

	public String getSharesCount() {
		return mSharesCount;
	}

	public void setSharesCount(String sharesCount) {
		this.mSharesCount = sharesCount;
	}

	// Created Time

	public String getCreatedTime() {
		return mCreatedTime;
	}

	public void setCreatedTime(String createdTime) {
		this.mCreatedTime = createdTime;
	}

	// Updated Time

	public String getUpdatedTime() {
		return mUpdatedTime;
	}

	public void setUpdatedTime(String updatedTime) {
		this.mUpdatedTime = updatedTime;
	}

	// User likes

	public String getUserLikes() {
		return mUserLikes;
	}

	public void setUserLikes(String userLikes) {
		this.mUserLikes = userLikes;
	}

	// Application name

	public void setApplicationName(String appName) {
		this.mApplicationName = appName;
	}

	public String getApplicationName() {
		return mApplicationName;
	}

	public String getStoryTags() {
		return mStoryTags;
	}

	public void setStoryTags(String storyTags) {
		this.mStoryTags = storyTags;
	}

	@Override
	public String toString() {

		return "Id: " + getId() + ", Time: " + getCreatedTime() + ", From: " + getFromName() + ", Title: " + getName() + ", Message: " + getMessage() + ", Description: " + getDescription();
	}

}
