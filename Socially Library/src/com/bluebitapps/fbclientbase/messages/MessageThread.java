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

package com.bluebitapps.fbclientbase.messages;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.utils.StringUtil;

public class MessageThread implements Parcelable {

	private String thread_id;
	private String subject;
	private String recipients;
	private String snippet;
	private String messageCount;
	private String unread;
	private String updatedTime;
	private String friendId;
	private String friendName;

	public MessageThread() {
	}

	public MessageThread(Parcel in) {

		setId(in.readString());
		setSubject(in.readString());
		setRecipients(in.readString());
		setSnippet(in.readString());
		setMessageCount(in.readString());
		setUnread(in.readString());
		setUpdatedTime(in.readString());
		setFriendName(in.readString());
		setFriendId(in.readString());
	}

	public static final Parcelable.Creator<MessageThread> CREATOR = new Parcelable.Creator<MessageThread>() {
		@Override
		public MessageThread createFromParcel(Parcel in) {
			return new MessageThread(in);
		}

		@Override
		public MessageThread[] newArray(int size) {
			return new MessageThread[size];
		}
	};

	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(getId());
		dest.writeString(getSubject());
		dest.writeString(getRecipients());
		dest.writeString(getSnippet());
		dest.writeString(getMessageCount());
		dest.writeString(getUnread());
		dest.writeString(getUpdatedTime());
		dest.writeString(getFriendName());
		dest.writeString(getFriendId());
	}

	public static MessageThread fromJSON(JSONObject obj) {
		MessageThread messageThread = new MessageThread();

		Logger.i("obj to String: " + obj.toString());

		try {

			if (obj.has("thread_id")) {
				messageThread.setId(obj.getString("thread_id"));
				if(obj.getString("thread_id").equals("25733147624219")){					
					Log.i("chat", "threadId set in message object: " + obj.getString("thread_id"));
				}
			}

			if (obj.has("subject")) {
				Logger.i("obj has subject");
				messageThread.setSubject(obj.getString("subject"));
			}

			if (obj.has("recipients")) {
				Logger.i("obj has recipients");
				messageThread.setRecipients(obj.getString("recipients"));
			}

			if (obj.has("snippet")) {
				messageThread.setSnippet(obj.getString("snippet"));
			}

			if (obj.has("message_count")) {
				messageThread.setMessageCount(obj.getString("message_count"));
			}

			if (obj.has("unread")) {
				messageThread.setUnread(obj.getString("unread"));
			}

			if (obj.has("updated_time")) {
				Log.i("jan27", Logger.getClassAndMethod() + obj.getString("updated_time"));
				messageThread.setUpdatedTime(obj.getString("updated_time"));
			}

		} catch (JSONException e) {
			Logger.i(e.toString());
		}

		return messageThread;
	}

	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		values.clear();
		values.put(MessageThreadData.C_ID, getId());
		values.put(MessageThreadData.C_SUBJECT, getSubject());
		values.put(MessageThreadData.C_RECIPIENTS, getRecipients());
		values.put(MessageThreadData.C_SNIPPET, getSnippet());
		values.put(MessageThreadData.C_MESSAGE_COUNT, getMessageCount());
		values.put(MessageThreadData.C_UNREAD, getUnread());
		values.put(MessageThreadData.C_UPDATED_TIME, getUpdatedTime());
		values.put(MessageThreadData.C_FRIEND_NAME, getFriendName());
		values.put(MessageThreadData.C_FRIEND_ID, getFriendId());
		return values;
	}

	public String getId() {
		return thread_id;
	}

	public void setId(String id) {
		this.thread_id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getRecipients() {
		return recipients;
	}

	public void setRecipients(String recipients) {
		this.recipients = recipients;
		if (StringUtil.notEmpty(recipients)) {
			Logger.i("recipients: " + recipients);
			String userId = FBClientApplication.getApplication().getFBConnection().getUserId();
			String temp1 = recipients.replace("[", "");
			String temp2 = temp1.replace("]", "");
			String[] items = temp2.split(",");
			List<String> names = Arrays.asList(items);
			for (int i = 0; i < names.size(); i++) {
				if (!names.get(i).equalsIgnoreCase(userId)) {
					friendId = names.get(i);
					break;
				}
			}
		}
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	public String getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(String messageCount) {
		this.messageCount = messageCount;
	}

	public String getUnread() {
		return unread;
	}

	public void setUnread(String unread) {
		this.unread = unread;
	}

	public String getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(String updatedTime) {
		this.updatedTime = updatedTime;
	}

	public String getFriendId() {
		return friendId;
	}
	
	public void setFriendId(String id){
		friendId = id;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String toString() {
		String str = "ThreadId: " + getId() + ", Snippet: " + getSnippet() + ", # Messages: " + getMessageCount() + ", # Unread:" + getUnread() + ", Friend's name: " + getFriendName() + ", Friend's id: " + getFriendId();
		return str;
	}

	public String getFriendName() {
		return friendName;
	}

	public void setFriendName(String friendName) {
		this.friendName = friendName;
	}
}