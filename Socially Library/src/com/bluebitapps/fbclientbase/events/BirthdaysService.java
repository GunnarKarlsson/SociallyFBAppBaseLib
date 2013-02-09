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
import android.os.Bundle;

import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class BirthdaysService extends IntentService {

	public static final String REFRESH_BIRTHDAYS_DATA_SUCCESS = "refresh birthdays data success";
	public static final String REFRESH_BIRTHDAYS_DATA_FAIL = "refersh birthdays data fail";

	public BirthdaysService() {
		super("EventsService");
	}

	/**
	 * Need to override onStartCommand() or onHandleIntent() will not be called.
	 * Is called for every call from e.g. alarm manager started in
	 * broadcastReceiver.
	 */

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Logger.i(Logger.getClassAndMethod());

		if (intent == null) {
			return;
		}

		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();
		if (isValidSession) {
			getBirthdays();
		}
	}

	private void getBirthdays() {

		Logger.i(Logger.getClassAndMethod());

		String query = "SELECT uid, name, birthday_date FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1=me())";
		Bundle params = new Bundle();
		params.putString("method", "fql.query");
		params.putString("query", query);
		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(null, params, new BirthdaysRequestListener());
	}

	private class BirthdaysRequestListener implements RequestListener {

		List<Birthday> birthdays = new ArrayList<Birthday>();

		@Override
		public void onComplete(String response, Object state) {
			try {
				Logger.i(BirthdaysRequestListener.class.getSimpleName() + "#onComplete()");
				Logger.i("response: " + response.toString());

				final JSONArray jsonArray = new JSONArray(response);

				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject obj = jsonArray.getJSONObject(i);
						Birthday bd = Birthday.fromJSON(obj);
						birthdays.add(bd);
					}
				}

				try {
					for (Birthday birthday : birthdays) {
						Logger.i(BirthdaysRequestListener.class.getSimpleName() + "#values:" + birthday.toString().toString());
						((FBClientApplication) getApplication()).getBirthdaysData().insertOrIgnore(birthday.toContentValues());
					}
				} catch (ConcurrentModificationException e) {
					Logger.i(Logger.getClassAndMethod() + e.toString());
				}

				sendBroadcast(new Intent(BirthdaysService.REFRESH_BIRTHDAYS_DATA_SUCCESS));

			} catch (JSONException e) {
				Logger.i(BirthdaysRequestListener.class.getSimpleName() + e.toString());
				sendBroadcast(new Intent(BirthdaysService.REFRESH_BIRTHDAYS_DATA_FAIL));
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(BirthdaysService.class.getSimpleName() + "." + BirthdaysRequestListener.class.getSimpleName() + "#EventsListener" + e.toString());
			sendBroadcast(new Intent(BirthdaysService.REFRESH_BIRTHDAYS_DATA_FAIL));
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(BirthdaysService.class.getSimpleName() + "." + BirthdaysRequestListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(BirthdaysService.REFRESH_BIRTHDAYS_DATA_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(BirthdaysService.class.getSimpleName() + "." + BirthdaysRequestListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(BirthdaysService.REFRESH_BIRTHDAYS_DATA_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(BirthdaysService.class.getSimpleName() + "." + BirthdaysRequestListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(BirthdaysService.REFRESH_BIRTHDAYS_DATA_FAIL));
		}
	}
}