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

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class CheckinsService extends IntentService {

	public static final String REFRESH_CHECKINS_DATA_SUCCESS = "refresh checkins data success";
	public static final String REFRESH_CHECKINS_DATA_FAIL = "refresh checkins data fail";

	private String mObjectId;

	public CheckinsService() {
		super("CheckinsService");
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
		Logger.i(CheckinsService.class.getSimpleName() + "#onHandleIntent()");

		if (intent == null) {
			return;
		}

		if (intent.getExtras() == null) {
			return;
		}

		Bundle bundle = intent.getExtras();

		mObjectId = bundle.getString(Constants.OBJECT_ID_KEY);

		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();

		if (isValidSession && mObjectId != null) {
			getCheckins();
		}
	}

	private void getCheckins() {

		Bundle params = new Bundle();
		params.putString("type", "checkin");

		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request("search", params, new CheckinsListener());
	}

	private class CheckinsListener implements RequestListener {

		List<Checkin> checkins = new ArrayList<Checkin>();

		@Override
		public void onComplete(String response, Object state) {
			try {
				Logger.i(CheckinsService.class.getSimpleName() + "." + CheckinsListener.class.getSimpleName() + "#onComplete()");
				Logger.i("response: " + response.toString());

				final JSONObject dataJsonObject = new JSONObject(response);
				final JSONArray jsonArray = dataJsonObject.getJSONArray("data");

				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject obj = jsonArray.getJSONObject(i);
						Checkin checkin = Checkin.fromJSON(obj);
						checkins.add(checkin);
					}
				}

				try {
					for (Checkin checkin : checkins) {
						Logger.i(CheckinsService.class.getSimpleName() + "." + CheckinsListener.class.getSimpleName() + "#values:" + checkin.toString().toString());
						((FBClientApplication) getApplication()).getCheckinsData().insertOrIgnore(checkin.toContentValues());
					}
				} catch (ConcurrentModificationException e) {
					sendBroadcast(new Intent(CheckinsService.REFRESH_CHECKINS_DATA_FAIL));
					return;
				}

				sendBroadcast(new Intent(CheckinsService.REFRESH_CHECKINS_DATA_SUCCESS));

			} catch (JSONException e) {
				Logger.i(CheckinsService.class.getSimpleName() + "." + CheckinsListener.class.getSimpleName() + e.toString());
				sendBroadcast(new Intent(CheckinsService.REFRESH_CHECKINS_DATA_FAIL));
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(CheckinsService.class.getSimpleName() + "." + CheckinsListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(CheckinsService.REFRESH_CHECKINS_DATA_FAIL));
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(CheckinsService.class.getSimpleName() + "." + CheckinsListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(CheckinsService.REFRESH_CHECKINS_DATA_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(CheckinsService.class.getSimpleName() + "." + CheckinsListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(CheckinsService.REFRESH_CHECKINS_DATA_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(CheckinsService.class.getSimpleName() + "." + CheckinsListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(CheckinsService.REFRESH_CHECKINS_DATA_FAIL));
		}
	}
}