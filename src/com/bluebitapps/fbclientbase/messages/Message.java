/*******************************************************************************
 * Copyright 2012 Gunnar Karlsson.
 *******************************************************************************/

package com.bluebitapps.fbclientbase.messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable{

	private String id;
	private String jabberId;
	private String fromName;
	private String fromId;
	private String messageText;
	private String createdTime;
	private String threadId;
	private String viewerId;

	public static Message fromJsonFromFQL(JSONObject obj){
		
		Message message = new Message();
		
		try{
			if(obj.has("message_id")){
				message.setId(obj.getString("message_id"));
			}
			if(obj.has("thread_id")){
				message.setThreadId(obj.getString("thread_id"));
			}
			if(obj.has("author_id")){
				message.setFromId(obj.getString("author_id"));
			}
			if(obj.has("body")){
				message.setMessageText(obj.getString("body"));
			}
			if(obj.has("created_time")){
				message.setCreatedTime(obj.getString("created_time"));
			}
			if(obj.has("viewer_id")){
				message.setViewerId(obj.getString("viewer_id"));
			}
			
		}catch(Exception e){
			
		}
		
		return message;
	}
	
	public static Message fromJSON(JSONObject obj) {
		Message message = new Message();
		try {
			if (obj.has("id")) {
				message.setId(obj.getString("id"));
			}
			if (obj.has("from")) {
				JSONObject from = obj.getJSONObject("from");
				if (from.has("id")) {
					message.setFromId(from.getString("id"));
				}
				if (from.has("name")) {
					message.setFromName(from.getString("name"));
				}
			}
			if(obj.has("message")){
				message.setMessageText(obj.getString("message"));
			}
			if(obj.has("created_time")){
				message.setCreatedTime(obj.getString("created_time"));
			}
		} catch (Exception e) {
		
		}
		return message;
		
	}

	public void setJabberId(String id){
		jabberId = id;
	}
	
	public String getJabberId(){
		return jabberId;
	}
	
	public String getIdFromJabberId(){
		if(jabberId == null)return "";
		
		Pattern intsOnlyPattern = Pattern.compile("\\d+");
		Matcher match = intsOnlyPattern.matcher(jabberId);
		match.find();
		return match.group();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public String getViewerId() {
		return viewerId;
	}

	public void setViewerId(String viewerId) {
		this.viewerId = viewerId;
	}

	public Message() {
	}

	public Message(Parcel in) {
		
		setId(in.readString());
		setJabberId(in.readString());
		setFromName(in.readString());
		setFromId(in.readString());
		setMessageText(in.readString());
		setCreatedTime(in.readString());
		setThreadId(in.readString());
		setViewerId(in.readString());

	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
		@Override
		public Message createFromParcel(Parcel in) {
			return new Message(in);
		}

		@Override
		public Message[] newArray(int size) {
			return new Message[size];
		}
	};

	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(getId());
		dest.writeString(getJabberId());
		dest.writeString(getFromName());
		dest.writeString(getFromId());
		dest.writeString(getMessageText());
		dest.writeString(getCreatedTime());
		dest.writeString(getThreadId());
		dest.writeString(getViewerId());
	}
}