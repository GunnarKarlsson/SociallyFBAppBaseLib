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

package com.bluebitapps.fbclientbase.page;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;

import com.bluebitapps.fbclientbase.debug.Logger;

public class Page {

	private String id;
	private String name;
	private String category;
	private String description;
	private String city;
	private String country;
	private String latitude;
	private String longitude;
	private String profilePic;
	private String fanCount;
	private String talkingAbout;
	private String coverPhoto;
	private String generalInfo;
	
	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		values.clear();
		Logger.i(Page.class.getSimpleName() + "#toContentValues(): getId(): "+ getId());
		values.put(PageData.C_ID, getId());
		values.put(PageData.C_NAME, getName());
		values.put(PageData.C_CATEGORY, getCategory());
		values.put(PageData.C_DESCRIPTION, getDescription());
		values.put(PageData.C_CITY, getCity());
		values.put(PageData.C_COUNTRY, getCountry());
		values.put(PageData.C_LATITUDE, getLatitude());
		values.put(PageData.C_LONGITUDE, getLongitude());
		values.put(PageData.C_PROFILE_PIC, getProfilePic());
		values.put(PageData.C_FAN_COUNT, getFanCount());
		values.put(PageData.C_COVER_PHOTO, getCoverPhoto());
		values.put(PageData.C_GENERAL_INFO, getGeneralInfo());
		return values;
	}

	public static Page fromJSON(JSONObject obj) {
		Page page = new Page();

		try {
			if (obj.has("page_id")) {
				page.setId(obj.getString("page_id"));
			}
			if(obj.has("id")){
				page.setId(obj.getString("id"));
			}
			if(obj.has("name")){
				page.setName(obj.getString("name"));
			}
			if(obj.has("category")){
				page.setCategory(obj.getString("category"));
			}
			if(obj.has("description")){
				page.setDescription(obj.getString("description"));
			}
			if(obj.has("pic_cover")){
				JSONObject cover = obj.getJSONObject("pic_cover");
				page.setCoverPhoto(cover.getString("source"));
			}
			if(obj.has("fan_count")){
				page.setFanCount(obj.getString("fan_count"));
			}
			if(obj.has("talkingAbout")){
				page.setTalkingAbout(obj.getString("global_brand_talking_about_count"));
			}
			if(obj.has("pic")){
				page.setProfilePic(obj.getString("pic"));
			}
			if(obj.has("general_info")){
				page.setGeneralInfo(obj.getString("general_info"));
			}
			if(obj.has("location")){
				JSONObject location = obj.getJSONObject("location");
				if(location.has("city")){
					page.setCity(location.getString("city"));
				}
				if(location.has("country")){
					page.setCountry(location.getString("country"));
				}
				if(location.has("latitude")){
					page.setLatitude(location.getString("latitude"));
				}
				if(location.has("longitude")){
					page.setLongitude(location.getString("longitude"));
				}
			}

		} catch (JSONException e) {
			Logger.i(Page.class.getSimpleName() + e.toString());
		}

		return page;
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

	public String getFanCount() {
		return fanCount;
	}

	public void setFanCount(String fanCount) {
		this.fanCount = fanCount;
	}

	public String getTalkingAbout() {
		return talkingAbout;
	}

	public void setTalkingAbout(String talkingAbout) {
		this.talkingAbout = talkingAbout;
	}

	public String getCoverPhoto() {
		return coverPhoto;
	}

	public void setCoverPhoto(String coverPhoto) {
		this.coverPhoto = coverPhoto;
	}

	public String getGeneralInfo() {
		return generalInfo;
	}

	public void setGeneralInfo(String generalPhoto) {
		this.generalInfo = generalPhoto;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

}