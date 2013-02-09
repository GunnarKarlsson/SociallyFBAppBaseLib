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

package com.bluebitapps.fbclientbase.events;

import org.json.JSONException;
import org.json.JSONObject;

import com.bluebitapps.fbclientbase.debug.Logger;

import android.content.ContentValues;
import android.util.Log;

public class Event {
	
	private String id;
	private String name;
	private String startTime;
	private String endTime;
	private String rsvpStatus;
	
	public static Event fromJSON(JSONObject obj){
		
		Log.i("jan29", Logger.getClassAndMethod() + obj.toString());
		
		Event event= new Event();

		try {
			if(obj.has("id")){				
				event.setId(obj.getString("id"));
			}
			if(obj.has("eid")){
				event.setId(obj.getString("eid"));
			}
			if(obj.has("name")){				
				event.setName(obj.getString("name"));
			}
			if(obj.has("start_time")){				
				event.setStartTime(obj.getString("start_time"));
			}
			if(obj.has("end_time")){				
				event.setEndTime(obj.getString("end_time"));
			}
			if(obj.has("rsvp_status")){
				Log.i("jan29", "rsvp set in Event instance");
				event.setRsvpStatus(obj.getString("rsvp_status"));
			}

		} catch (JSONException e) {
			Log.i("jan29", Logger.getClassAndMethod());
		}

		return event;
	}
	
	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		values.clear();
		values.put(EventsData.C_ID, getId());
		values.put(EventsData.C_NAME, getName());
		values.put(EventsData.C_START_TIME, getStartTime());
		values.put(EventsData.C_END_TIME, getEndTime());
		values.put(EventsData.C_RSVP_STATUS, getRsvpStatus());
		return values;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public String getRsvpStatus() {
		return rsvpStatus;
	}
	public void setRsvpStatus(String rsvpStatus) {
		this.rsvpStatus = rsvpStatus;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}