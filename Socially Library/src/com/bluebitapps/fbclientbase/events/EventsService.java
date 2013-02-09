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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class EventsService extends IntentService {

	public static final String REFRESH_EVENTS_DATA_SUCCESS = "refresh events data success";
	public static final String REFRESH_EVENTS_DATA_FAIL = "refresh events data fail";

	public EventsService() {
		super("EventsService");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Logger.i(EventsService.class.getSimpleName() + "#onHandleIntent()");
		Log.i("jan30", Logger.getClassAndMethod());
		
		if(intent == null){
			return;
		}
		
		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();
		if (isValidSession) {
			getEvents();
		}
	}

	private void getEvents() {
		Logger.i(EventsService.class.getSimpleName() + "#getEvents()");

		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request("me/events", new EventsListener());
	}

	private class EventsListener implements RequestListener {

		List<Event> events = new ArrayList<Event>();

		@Override
		public void onComplete(String response, Object state) {
			try {
				Logger.i(EventsService.class.getSimpleName() + "." + EventsListener.class.getSimpleName() + "#onComplete()");
				Logger.i("response: " + response.toString());

				final JSONObject dataJsonObject = new JSONObject(response);
				final JSONArray jsonArray = dataJsonObject.getJSONArray("data");

				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject obj = jsonArray.getJSONObject(i);
						Event event = Event.fromJSON(obj);
						events.add(event);
					}
				}

				try{
				for (Event event : events) {
					Logger.i(EventsService.class.getSimpleName() + "." + EventsListener.class.getSimpleName() + "#values:" + event.toString().toString());
					((FBClientApplication) getApplication()).getEventsData().insertOrIgnore(event.toContentValues());
				}
				}catch(ConcurrentModificationException e){
					sendBroadcast(new Intent(EventsService.REFRESH_EVENTS_DATA_FAIL));
					return;
				}

				sendBroadcast(new Intent(EventsService.REFRESH_EVENTS_DATA_SUCCESS));

			} catch (JSONException e) {
				Logger.i(EventsService.class.getSimpleName() + "." + EventsListener.class.getSimpleName() + e.toString());
				sendBroadcast(new Intent(EventsService.REFRESH_EVENTS_DATA_FAIL));
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(EventsService.class.getSimpleName() + "." + EventsListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(EventsService.REFRESH_EVENTS_DATA_FAIL));
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(EventsService.class.getSimpleName() + "." + EventsListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(EventsService.REFRESH_EVENTS_DATA_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(EventsService.class.getSimpleName() + "." + EventsListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(EventsService.REFRESH_EVENTS_DATA_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(EventsService.class.getSimpleName() + "." + EventsListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(EventsService.REFRESH_EVENTS_DATA_FAIL));
		}
	}
}