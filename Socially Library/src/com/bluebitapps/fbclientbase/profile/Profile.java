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

package com.bluebitapps.fbclientbase.profile;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;

import com.bluebitapps.fbclientbase.debug.Logger;

public class Profile {

	// profile
	private String id;
	private String name;
	private String firstName;
	private String lastName;
	private String link;
	private String userName;
	private String birthday;
	private String homeTownId;
	private String homeTownName;
	private String locationId;
	private String locationName;
	private String gender;
	private String relationshipStatus;

	// event
	private String ownerName;
	private String ownerCategory;
	private String ownerId;
	private String description;
	private String startTime;
	private String endTime;
	private String timeZone;
	private String isDateOnly;
	private String location;
	private String venueName;
	private String privacy;
	private String updatedTime;
	private String coverPhoto;

	// group
	private String icon;
	private String email;

	// page/like
	private String createdTime;
	private String category;

	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		values.clear();
		values.put(ProfileData.C_ID, getId());
		values.put(ProfileData.C_NAME, getName());
		values.put(ProfileData.C_FIRST_NAME, getFirstName());
		values.put(ProfileData.C_LAST_NAME, getLastName());
		values.put(ProfileData.C_LINK, getLink());
		values.put(ProfileData.C_USER_NAME, getUserName());
		values.put(ProfileData.C_BIRTHDAY, getBirthday());
		values.put(ProfileData.C_HOME_TOWN_ID, getHomeTownId());
		values.put(ProfileData.C_HOME_TOWN_NAME, getHomeTownName());
		values.put(ProfileData.C_LOCATION_ID, getLocationId());
		values.put(ProfileData.C_LOCATION_NAME, getLocationName());
		values.put(ProfileData.C_GENDER, getGender());
		values.put(ProfileData.C_OWNER_NAME, getOwnerName());
		values.put(ProfileData.C_OWNER_CATEGORY, getOwnerCategory());
		values.put(ProfileData.C_OWNER_ID, getOwnerId());
		values.put(ProfileData.C_DESCRIPTION, getDescription());
		values.put(ProfileData.C_START_TIME, getStartTime());
		values.put(ProfileData.C_END_TIME, getEndTime());
		values.put(ProfileData.C_TIME_ZONE, getTimeZone());
		values.put(ProfileData.C_IS_DATE_ONLY, getIsDateOnly());
		values.put(ProfileData.C_LOCATION, getLocation());
		values.put(ProfileData.C_VENUE_NAME, getVenueName());
		values.put(ProfileData.C_UPDATED_TIME, getUpdatedTime());
		values.put(ProfileData.C_COVER_PHOTO, getCoverPhoto());
		return values;
	}
	
	public static Profile fromJSON(JSONObject obj) {
		Profile info = new Profile();
		try {

			if(obj.has("uid")){
				info.setId(obj.getString("uid"));				
			}
			
			if(obj.has("username")){
				info.setUserName(obj.getString("username"));
			}
			
			if(obj.has("birthday")){
				info.setBirthday(obj.getString("birthday"));
			}
			
			if(obj.has("sex")){
				info.setGender(obj.getString("sex"));
			}

			if(obj.has("hometown_location")){
				info.setHomeTownName(obj.getString("hometown_location"));
			}
			
			if(obj.has("relationship_status")){
				info.setRelationshipStatus(obj.getString("relationship_status"));
			}
			
			if(obj.has("pic_cover")){
				JSONObject cover = obj.getJSONObject("pic_cover");
				//cover.getJSONObject("source");
				Logger.i(cover.getString("source"));
				Logger.i(cover.getString("source"));
				
				info.setCoverPhoto(cover.get("source").toString());
			}
			
		} catch (JSONException e) {
			Logger.i(e.toString());
		}

		return info;
	}
	
	public void setCoverPhoto(String url){
		coverPhoto = url;
	}
	
	public String getCoverPhoto(){
		return coverPhoto;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getHomeTownId() {
		return homeTownId;
	}

	public void setHomeTownId(String homeTownId) {
		this.homeTownId = homeTownId;
	}

	public String getHomeTownName() {
		return homeTownName;
	}

	public void setHomeTownName(String homeTownName) {
		this.homeTownName = homeTownName;
	}

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getRelationshipStatus() {
		return relationshipStatus;
	}

	public void setRelationshipStatus(String relationshipStatus) {
		this.relationshipStatus = relationshipStatus;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getOwnerCategory() {
		return ownerCategory;
	}

	public void setOwnerCategory(String ownerCategory) {
		this.ownerCategory = ownerCategory;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getIsDateOnly() {
		return isDateOnly;
	}

	public void setIsDateOnly(String isDateOnly) {
		this.isDateOnly = isDateOnly;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getVenueName() {
		return venueName;
	}

	public void setVenueName(String venueName) {
		this.venueName = venueName;
	}

	public String getPrivacy() {
		return privacy;
	}

	public void setPrivacy(String privacy) {
		this.privacy = privacy;
	}

	public String getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(String updatedTime) {
		this.updatedTime = updatedTime;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

}
