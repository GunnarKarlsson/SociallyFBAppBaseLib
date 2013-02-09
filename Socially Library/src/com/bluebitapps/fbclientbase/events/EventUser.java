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

import org.json.JSONObject;

import com.bluebitapps.fbclientbase.FBClientApplication;

public class EventUser {

	private String id;
	private String name;
	private String rsvpStatus;

	public static EventUser fromJSON(JSONObject obj) {

		EventUser eventUser = new EventUser();

		try {
			if (obj.has("name")) {
				eventUser.setName(obj.getString("name"));
			}
			if (obj.has("id")) {
				eventUser.setId(obj.getString("id"));
			}
			if(obj.has("rsvp_status")){
				eventUser.setRsvpStatus("rsvp_status");
			}

		} catch (Exception e) {
			return null;
		}

		return eventUser;
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

	public String getPicture(FBClientApplication app) {
		String token = app.getFBConnection().getFacebook().getAccessToken();

		return "https://graph.facebook.com/" + getId()
				+ "/picture?access_token=" + token;
	}

	public String getRsvpStatus() {
		return rsvpStatus;
	}

	public void setRsvpStatus(String rsvpStatus) {
		this.rsvpStatus = rsvpStatus;
	}
}