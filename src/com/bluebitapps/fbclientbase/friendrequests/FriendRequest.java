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

package com.bluebitapps.fbclientbase.friendrequests;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.debug.Logger;

public class FriendRequest implements Parcelable{
	
	private String fromUid;
	private String fromName;
	private String time;
	private String message;
	private String unread;
	private FBClientApplication mApp;

	public FriendRequest(){
		mApp = FBClientApplication.getApplication();
	}
	
	public static FriendRequest fromJSON(JSONObject obj){
		FriendRequest request = new FriendRequest();
		try {

			if(obj.has("uid_from")){
				request.setFromUid(obj.getString("uid_from"));
			}
			if(obj.has("time")){				
				request.setTime(obj.getString("time"));
			}
			if(obj.has("created_time")){				
				request.setMessage(obj.getString("message"));
			}
			if(obj.has("unread")){
				request.setUnread(obj.getString("unread"));
			}
			
		} catch (JSONException e) {
			Logger.i(e.toString());
		}

		return request;
	}
	
	public String getFromUid() {
		return fromUid;
	}
	public void setFromUid(String fromUid) {
		this.fromUid = fromUid;
	}
	public String getFromName() {
		return fromName;
	}
	public void setFromName(String fromName) {
		this.fromName = fromName;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getUnread() {
		return unread;
	}
	public void setUnread(String unread) {
		this.unread = unread;
	}
	public String getProfilePicture(){
		
		//String token = mApp.getFBConnection().getFacebook().getAccessToken();
		Log.i("frtest", "uid: "+ getFromUid());
		return "https://graph.facebook.com/" + getFromUid()
				+ "/picture";
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public FriendRequest(Parcel in) {

		setFromUid(in.readString());
		setFromName(in.readString());
		setTime(in.readString());
		setMessage(in.readString());
		setUnread(in.readString());
	}

	public static final Parcelable.Creator<FriendRequest> CREATOR = new Parcelable.Creator<FriendRequest>() {
		@Override
		public FriendRequest createFromParcel(Parcel in) {
			return new FriendRequest(in);
		}

		@Override
		public FriendRequest[] newArray(int size) {
			return new FriendRequest[size];
		}
	};

	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(getFromUid());
		dest.writeString(getFromName());
		dest.writeString(getTime());
		dest.writeString(getMessage());
		dest.writeString(getUnread());
	}
}