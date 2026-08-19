/* Copyright 2012 Gunnar Karlsson.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.friendrequests.FriendRequest;
import com.bluebitapps.fbclientbase.friendrequests.FriendRequestsFragment;
import com.bluebitapps.utils.OutputUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class EventsInvitationService extends IntentService {

	public static final String REFRESH_EVENTS_DATA_SUCCESS = "refresh events data success";
	public static final String REFRESH_EVENTS_DATA_FAIL = "refresh events data fail";

	public EventsInvitationService() {
		super("EventsInvitationService");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();
		if (isValidSession) {
			getEvents();
		}
	}

	private void getEvents() {

		final String query1 = "SELECT eid, rsvp_status FROM event_member WHERE uid = me() AND rsvp_status='not_replied' ORDER BY start_time";
		final String query2 = "SELECT eid, name, start_time, end_time FROM event WHERE eid IN (SELECT eid FROM #query1) ORDER BY start_time";

		final JSONObject jsonQueries = new JSONObject() {
			{
				try {
					put("query1", query1);
					put("query2", query2);
				} catch (Exception e) {
					Logger.i(Logger.getClassAndMethod());
				}
			}
		};

		Bundle params = new Bundle();
		params.putString("method", "fql.multiquery");
		params.putString("queries", jsonQueries.toString());
		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(null, params, new InvitesRequestListener());

	}

	private class InvitesRequestListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {
			
			Log.i("jan29", Logger.getClassAndMethod() + " response: " + response.toString()); 

			final List<Event> invitations = new ArrayList<Event>();

			try {
				JSONArray a = new JSONArray(response);
				JSONObject invitesObj = a.getJSONObject(0);
				JSONObject eventDetailsObj = a.getJSONObject(1);
				JSONArray invitesJsonArray = invitesObj.getJSONArray("fql_result_set");
				JSONArray eventsDetailsJsonArray = eventDetailsObj.getJSONArray("fql_result_set");

				HashMap<String, String> eventDetailsMap = new HashMap<String, String>();

				if (eventsDetailsJsonArray.length() > 0) {

					for (int i = 0; i < eventsDetailsJsonArray.length(); i++) {
						String uid = "";
						if (eventsDetailsJsonArray.getJSONObject(i).has("eid")) {
							uid = eventsDetailsJsonArray.getJSONObject(i).getString("eid");
						}
						
						eventDetailsMap.put(uid, eventsDetailsJsonArray.getJSONObject(i).toString());
					}
				}

				if (invitesJsonArray.length() > 0) {
					for (int i = 0; i < invitesJsonArray.length(); i++) {
						JSONObject obj = invitesJsonArray.getJSONObject(i);
						Event event = Event.fromJSON(obj);

						if (eventDetailsMap.containsKey(event.getId())) {
							
							String jsonObjStr = eventDetailsMap.get(event.getId());
							JSONObject jsonEventObj = new JSONObject(jsonObjStr);
							
							Log.i("jan29", "jsonEventObj.toString(): " + jsonEventObj.toString());
							
							if(jsonEventObj.has("name")){
								event.setName(jsonEventObj.getString("name"));
							}
							
							if(jsonEventObj.has("start_time")){
								event.setStartTime(jsonEventObj.getString("start_time"));
							}
							
							if(jsonEventObj.has("end_time")){
								event.setEndTime(jsonEventObj.getString("end_time"));
							}
							/*
							if(jsonEventObj.has("rsvp_status")){
								event.setRsvpStatus(jsonEventObj.getString("rsvp_status"));
							}
							*/
						}

						invitations.add(event);
					}
				}

			} catch (JSONException e) {
				Log.i("jan29", e.toString());
				Logger.i(Logger.getMethodName());
			}
			
			Log.i("jan29", "invitations.size(): " + invitations.size());
			
			try{
			for (Event event : invitations) {
				((FBClientApplication) getApplication()).getEventsData().insertOrIgnore(event.toContentValues());
			}
			}catch(ConcurrentModificationException e){
				sendBroadcast(new Intent(EventsService.REFRESH_EVENTS_DATA_FAIL));
				return;
				
			}
			
			sendBroadcast(new Intent(EventsService.REFRESH_EVENTS_DATA_SUCCESS));
		}

		@Override
		public void onIOException(IOException e, Object state) {
			sendBroadcast(new Intent(EventsService.REFRESH_EVENTS_DATA_FAIL));
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			sendBroadcast(new Intent(EventsService.REFRESH_EVENTS_DATA_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			sendBroadcast(new Intent(EventsService.REFRESH_EVENTS_DATA_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			sendBroadcast(new Intent(EventsService.REFRESH_EVENTS_DATA_FAIL));
		}
	}
}
