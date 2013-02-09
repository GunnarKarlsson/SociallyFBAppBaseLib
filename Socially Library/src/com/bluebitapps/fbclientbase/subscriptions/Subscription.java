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

package com.bluebitapps.fbclientbase.subscriptions;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.debug.Logger;

public class Subscription implements Parcelable{
	
	private String mSubscribedId;
	private String mSubscribedName;
	private FBClientApplication mApp;

	public static Subscription fromJSON(JSONObject obj){
		Subscription subscription = new Subscription();
		try {

			if(obj.has("subscribed_id")){
				subscription.setSubscribedId(obj.getString("subscribed_id"));
			}
			
		} catch (JSONException e) {
			Logger.i(Logger.getMethodName() + e.toString());
		}

		return subscription;
	}
	
	public Subscription(){
		mApp = FBClientApplication.getApplication();
	}
	
	public Subscription(Parcel in) {
		setSubscribedId(in.readString());
		setSubscribedName(in.readString());
	}

	public static final Parcelable.Creator<Subscription> CREATOR = new Parcelable.Creator<Subscription>() {
		@Override
		public Subscription createFromParcel(Parcel in) {
			return new Subscription(in);
		}

		@Override
		public Subscription[] newArray(int size) {
			return new Subscription[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getSubscribedId());
		dest.writeString(getSubscribedName());
	}

	public String getSubscribedId() {
		return mSubscribedId;
	}

	public void setSubscribedId(String subscribedId) {
		this.mSubscribedId = subscribedId;
	}

	public String getSubscribedName() {
		return mSubscribedName;
	}

	public void setSubscribedName(String subscribedName) {
		this.mSubscribedName = subscribedName;
	}
	
	public String getProfilePicture(){
		String token = mApp.getFBConnection().getFacebook().getAccessToken();

		return "https://graph.facebook.com/" + getSubscribedId()
				+ "/picture?access_token=" + token;
	}
}