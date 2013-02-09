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

package com.bluebitapps.fbclientbase.checkins;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;

public class Checkin {

	private String id;
	private String fromId;
	private String fromName;
	private String message;
	private String placeId;
	private String placeName;
	private String city;
	private String country;
	private String longitude;
	private String latitude;
	private String createdTime;

	public ContentValues toContentValues(){
		ContentValues values = new ContentValues();
		values.clear();
		values.put(CheckinsData.C_ID, getId());
		values.put(CheckinsData.C_FROM_ID, getFromId());
		values.put(CheckinsData.C_FROM_NAME, getFromName());
		values.put(CheckinsData.C_MESSAGE, getMessage());
		values.put(CheckinsData.C_PLACE_ID, getPlaceId());
		values.put(CheckinsData.C_PLACE_NAME, getPlaceName());
		values.put(CheckinsData.C_CITY, getCity());
		values.put(CheckinsData.C_COUNTRY, getCountry());
		values.put(CheckinsData.C_LONGITUDE, getLongitude());
		values.put(CheckinsData.C_LATITUDE, getLatitude());
		values.put(CheckinsData.C_CREATED_TIME, getCreatedTime());
		return values;
	}
	
	public static Checkin fromJSON(JSONObject obj) {
		Checkin checkin = new Checkin();
		try {
			checkin.setId(obj.getString("id"));
			if (obj.has("from")) {
				JSONObject from = obj.getJSONObject("from");
				if (from.has("id")) {
					checkin.setFromId(from.getString("id"));
				}
				if (from.has("name")) {
					checkin.setFromName(from.getString("name"));
				}
			}
			
			if(obj.has("created_time")){
				checkin.setCreatedTime(obj.getString("created_time"));
			}
			
			if(obj.has("place")){
				JSONObject place = obj.getJSONObject("place");
				if(place.has("id")){
					checkin.setPlaceId(place.getString("id"));
				}
				
				if(place.has("name")){
					checkin.setPlaceName(place.getString("name"));
				}
				
				if(place.has("location")){
					JSONObject location = place.getJSONObject("location");
					if(location.has("city")){
						checkin.setCity(location.getString("city"));
					}
						
					if(location.has("county")){
						checkin.setCountry(location.getString("country"));
					}
					
					if(location.has("latitude")){
						checkin.setLatitude(location.getString("latitude"));
					}
					if(location.has("longitude")){
						checkin.setLongitude(location.getString("longitude"));
					}
					
				}
			}
		} catch (JSONException e) {
			// TODO handle e
		}
		return checkin;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	public String getPlaceName() {
		return placeName;
	}

	public void setPlaceName(String placeName) {
		this.placeName = placeName;
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

	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

}
