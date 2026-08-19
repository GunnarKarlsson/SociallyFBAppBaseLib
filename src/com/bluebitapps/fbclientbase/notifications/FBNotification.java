/**
* Copyright 2012 Gunnar Karlsson.
*/

package com.bluebitapps.fbclientbase.notifications;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;

import com.bluebitapps.fbclientbase.debug.Logger;

public class FBNotification{
	
	private String id;
	private String senderId;
	private String senderName;
	private String createdTime;
	private String titleText;
	private String bodyText;
	private String appId;
	private String appName;
	private String isUnread;
	private String isHidden;
	private String objectId;
	private String objectType;

	public ContentValues getContentValues(){
		ContentValues values = new ContentValues();
		values.clear();
		values.put(NotificationsData.C_ID, getId());
		values.put(NotificationsData.C_SENDER_ID, getSenderId());
		values.put(NotificationsData.C_SENDER_NAME, getSenderName());
		values.put(NotificationsData.C_CREATED_TIME, getCreatedTime());
		values.put(NotificationsData.C_TITLE_TEXT, getTitleText());
		values.put(NotificationsData.C_BODY_TEXT, getBodyText());
		values.put(NotificationsData.C_APP_ID, getAppId());
		values.put(NotificationsData.C_APP_NAME, getAppName());
		values.put(NotificationsData.C_IS_UNREAD, getIsUnread());
		values.put(NotificationsData.C_IS_HIDDEN, getIsHidden());
		values.put(NotificationsData.C_OBJECT_ID, getObjectId());
		values.put(NotificationsData.C_OBJECT_TYPE, getObjectType());
		
		return values;
	}
	
	public static FBNotification fromJSON(JSONObject obj) {
		
		FBNotification notification = new FBNotification();
		try {
			//Graph
			if(obj.has("id")){
				notification.setId(obj.getString("id"));
			}
			//FQL
			if(obj.has("notification_id")){
				notification.setId(obj.getString("notification_id"));
				Logger.i(Logger.getClassAndMethod() + "notification_id: " + obj.getString("notification_id"));
			}
			
			if(obj.has("sender_id")){				
				notification.setSenderId(obj.getString("sender_id"));
				Logger.i(Logger.getClassAndMethod() + "id: " + obj.getString("sender_id"));
			}
			if(obj.has("created_time")){				
				notification.setCreatedTime(obj.getString("created_time"));
			}
			if(obj.has("title_text")){
				notification.setTitleText(obj.getString("title_text"));
			}
			if(obj.has("body_text")){
				notification.setBodyText(obj.getString("body_text"));
			}
			if(obj.has("app_id")){
				notification.setAppId(obj.getString("app_id"));
			}
			if(obj.has("is_unread")){
				Logger.i("is_unread: " + obj.getString("is_unread"));
				notification.setIsUnread(obj.getString("is_unread"));
			}
			if(obj.has("is_hidden")){
				notification.setIsHidden(obj.getString("is_hidden"));
			}
			if(obj.has("object_id")){
				notification.setObjectId(obj.getString("object_id"));
			}
			if(obj.has("object_type")){
				notification.setObjectType(obj.getString("object_type"));
			}
			
		} catch (JSONException e) {
			Logger.i(e.toString());
		}

		return notification;
	}
	
	public String getProfilePicture() {
		return "https://graph.facebook.com/" + getSenderId()
				+ "/picture?width=100&height=100";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public String getTitleText() {
		return titleText;
	}

	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}

	public String getBodyText() {
		return bodyText;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getIsUnread() {
		return isUnread;
	}

	public void setIsUnread(String isUnread) {
		this.isUnread = isUnread;
	}

	public String getIsHidden() {
		return isHidden;
	}

	public void setIsHidden(String isHidden) {
		this.isHidden = isHidden;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
}