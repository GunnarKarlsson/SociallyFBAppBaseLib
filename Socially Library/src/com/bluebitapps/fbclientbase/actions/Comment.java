/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.actions;

import org.json.JSONException;
import org.json.JSONObject;

import com.bluebitapps.fbclientbase.debug.Logger;

public class Comment implements Likeable {

	private String mId;
	private String mFromName;
	private String mFromId;
	private String mMessage;
	private String mCanRemove;
	private String mCreatedTime;
	private String mLikeCount;
	private String mUserLikes;

	public static Comment fromJson(JSONObject obj) {

		if (obj == null)
			return null;

		Comment item = new Comment();

		try {

			// FQL
			if (obj.has("id")) {
				item.setId(obj.getString("id"));
			}

			if (obj.has("can_remove")) {
				item.setCanRemove(obj.getString("can_remove"));
			}

			if (obj.has("user_likes")) {
				item.setUserLikes(obj.getString("user_likes"));
			}

			if (obj.has("from")) {
				JSONObject from = obj.getJSONObject("from");

				if (from.has("id")) {
					item.setFromId(from.getString("id"));
				}

				if (from.has("name")) {
					item.setFromName(from.getString("name"));
				}
			}

			if (obj.has("username")) {
				item.setFromName(obj.getString("username"));
			}

			// FQL
			if (obj.has("fromid")) {
				item.setFromId(obj.getString("fromid"));
			}

			if (obj.has("message")) {
				item.setMessage(obj.getString("message"));
			}

			// FQL
			if (obj.has("text")) {
				item.setMessage(obj.getString("text"));
			}

			if (obj.has("likes_count")) {
				item.setLikesCount(obj.getString("likes_count"));
			}

			// FQL
			if (obj.has("likes")) {
				item.setLikesCount(obj.getString("likes"));
			}

			if (obj.has("created_time")) {
				item.setCreatedTime(obj.getString("created_time"));
			}

			// FQL
			if (obj.has("time")) {
				item.setCreatedTime(Integer.toString(obj.getInt("time")));
			}

		} catch (JSONException e) {
			Logger.i(Comment.class.getSimpleName() + e.toString());
		}

		return item;
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		this.mId = id;
	}

	public String getFromName() {
		return mFromName;
	}

	public void setFromName(String fromName) {
		this.mFromName = fromName;
	}

	public String getFromId() {
		return mFromId;
	}

	public void setFromId(String fromId) {
		this.mFromId = fromId;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(String message) {
		this.mMessage = message;
	}

	public String getCanRemove() {
		return mCanRemove;
	}

	public void setCanRemove(String canRemove) {
		this.mCanRemove = canRemove;
	}

	public String getCreatedTime() {
		return mCreatedTime;
	}

	public void setCreatedTime(String createdTime) {
		this.mCreatedTime = createdTime;
	}

	public String getLikesCount() {
		return mLikeCount;
	}

	public void setLikesCount(String likeCount) {
		this.mLikeCount = likeCount;
	}

	public String getUserLikes() {
		return mUserLikes;
	}

	public void setUserLikes(String userLikes) {
		this.mUserLikes = userLikes;
	}

	public String getFromPicture() {	
		return "https://graph.facebook.com/" + getFromId() + "/picture?width=50&height=50";
	}
}