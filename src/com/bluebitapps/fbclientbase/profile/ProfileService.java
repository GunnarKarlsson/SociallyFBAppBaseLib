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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

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

public class ProfileService extends IntentService {

	public static final String REFRESH_PROFILE_DATA_SUCCESS = "refresh profile data success";
	public static final String REFRESH_PROFILE_DATA_FAIL = "refresh profile data fail";

	private String mObjectId;

	public ProfileService() {
		super("ProfileService");
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
		
		if(intent == null){
			return;
		}
		
		if(intent.getExtras()==null){
			return;
		}

		Bundle bundle = intent.getExtras();
		mObjectId = bundle.getString(com.bluebitapps.fbclientbase.Constants.OBJECT_ID_KEY);
		Logger.i(Logger.getClassAndMethod() + mObjectId);

		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();
		if (isValidSession) {
			getProfile();
		}
	}

	private void getProfile() {

		String query = "SELECT uid, username, pic, birthday, sex, relationship_status, pic_cover FROM user WHERE uid=" + mObjectId;
		Bundle params = new Bundle();
		params.putString("method", "fql.query");
		params.putString("query", query);
		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(null, params, new ProfileListener());

	}

	private class ProfileListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {
			try {
				Logger.i(Logger.getClassAndMethod() + "#onComplete()");
				Logger.i("response: " + response.toString());

				final JSONArray jsonArray = new JSONArray(response);
				JSONObject obj = jsonArray.getJSONObject(0);

				Profile profile = Profile.fromJSON(obj);

				((FBClientApplication) getApplication()).getProfileData().insertOrIgnore(profile.toContentValues());
				Logger.i(ProfileService.class.getSimpleName() + "." + ProfileListener.class.getSimpleName() + "#values:" + profile.toString());

				sendBroadcast(new Intent(ProfileService.REFRESH_PROFILE_DATA_SUCCESS));

			} catch (JSONException e) {
					Logger.i(Logger.getClassAndMethod() + e.toString());
					sendBroadcast(new Intent(ProfileService.REFRESH_PROFILE_DATA_FAIL));
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(ProfileService.class.getSimpleName() + "." + ProfileListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(ProfileService.REFRESH_PROFILE_DATA_FAIL));
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
				Logger.i(ProfileService.class.getSimpleName() + "." + ProfileListener.class.getSimpleName() + e.toString());
				sendBroadcast(new Intent(ProfileService.REFRESH_PROFILE_DATA_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
				Logger.i(ProfileService.class.getSimpleName() + "." + ProfileListener.class.getSimpleName() + e.toString());
				sendBroadcast(new Intent(ProfileService.REFRESH_PROFILE_DATA_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
				Logger.i(ProfileService.class.getSimpleName() + "." + ProfileListener.class.getSimpleName() + e.toString());
				sendBroadcast(new Intent(ProfileService.REFRESH_PROFILE_DATA_FAIL));
		}
	}
}