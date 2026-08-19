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

package com.bluebitapps.fbclientbase.photos;

import org.json.JSONException;
import org.json.JSONObject;

import com.bluebitapps.fbclientbase.actions.Likeable;
import com.bluebitapps.fbclientbase.debug.Logger;

class PhotoComment implements Likeable{
	private String id;
	private String objectId;
	private String fromId;
	private String fromName;
	private String text;
	private String time;
	private String userLikes;
	private String likesCount;

	public static PhotoComment fromJSON(JSONObject obj){
		PhotoComment comment = new PhotoComment();
		
		try{
		if(obj.has("id")){
			comment.setId(obj.getString("id"));
		}
			
		if(obj.has("object_id")){
			comment.setObjectId(obj.getString("object_id"));
		}
		if(obj.has("fromid")){
			comment.setFromId(obj.getString("fromid"));
		}
		if(obj.has("time")){
			comment.setTime(obj.getString("time"));
		}
		if(obj.has("text")){
			comment.setText(obj.getString("text"));
		}
		if(obj.has("user_likes")){
			comment.setUserLikes(obj.getString("user_likes"));
		}
		if(obj.has("likes")){
			comment.setLikesCount(obj.getString("likes"));
		}
		}catch(JSONException e){
			Logger.i(e.toString());
		}
		
		return comment;
	}
	
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getUserLikes() {
		return userLikes;
	}
	public void setUserLikes(String userLikes) {
		this.userLikes = userLikes;
	}

	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getFromPicture(){
		return "https://graph.facebook.com/" + getFromId() + "/picture?width=50&height=50";
	}

	@Override
	public String getLikesCount() {
		return likesCount;
	}

	@Override
	public void setLikesCount(String value) {
		this.likesCount = value;
		
	}

	@Override
	public String getId() {
		return id;
	}
	
	public void setId(String value){
		this.id = value;
	}
}
