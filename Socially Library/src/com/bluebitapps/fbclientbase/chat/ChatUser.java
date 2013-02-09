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

package com.bluebitapps.fbclientbase.chat;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluebitapps.utils.FacebookUtils;

public class ChatUser implements Parcelable{
	private String name;
	private String jabberId;	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJabberId() {
		return jabberId;
	}

	public void setJabberId(String jabberId) {
		this.jabberId = jabberId;
	}
	
	public ChatUser(){}
	
	public ChatUser(Parcel in){
		setName(in.readString());
		setJabberId(in.readString());
	}
	
	public String getFbId(){
		return FacebookUtils.getUserIdFromJabberId(jabberId);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<ChatUser> CREATOR = new Parcelable.Creator<ChatUser>() {
		@Override
		public ChatUser createFromParcel(Parcel in) {
			return new ChatUser(in);
		}

		@Override
		public ChatUser[] newArray(int size) {
			return new ChatUser[size];
		}
	};
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getName());
		dest.writeString(getJabberId());
	}
}