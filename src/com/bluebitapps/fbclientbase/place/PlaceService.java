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

package com.bluebitapps.fbclientbase.place;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.page.Page;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class PlaceService extends IntentService {

	public static final String REFRESH_PLACE_DATA_SUCCESS = "refresh place data success";
	public static final String REFRESH_PLACE_DATA_FAIL = "refresh place data fail";

	private String mPlaceId;

	public PlaceService() {
		super("PlaceService");
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
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				mPlaceId = bundle.getString(Constants.OBJECT_ID_KEY);
				if (mPlaceId != null) {
					boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();
					if (isValidSession) {
						getPlaceProfile();
					}

				} else {
					// TODO: Handle that mPlaceId is null.
				}
			}
		}

	}

	private void getPlaceProfile() {
		Logger.i(Logger.getMethodName() + "mPlaceId: " +mPlaceId);
		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(mPlaceId, new PlaceProfileListener());
	}

	private class PlaceProfileListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {
			try {

				JSONObject obj = new JSONObject(response);

				Page page = Page.fromJSON(obj);

				try{
					((FBClientApplication) getApplication()).getPageData().insertOrIgnore(page.toContentValues());					
				}catch(Exception e){
					Logger.i(e.toString());//TODO: Specific exception more precisely, not catch-all Exception
				}

				sendBroadcast(new Intent(PlaceService.REFRESH_PLACE_DATA_SUCCESS));

			} catch (JSONException e) {
				Logger.i(Logger.getClassAndMethod() + e.toString());
				sendBroadcast(new Intent(PlaceService.REFRESH_PLACE_DATA_FAIL));
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(PlaceService.REFRESH_PLACE_DATA_FAIL));
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(PlaceService.REFRESH_PLACE_DATA_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(PlaceService.REFRESH_PLACE_DATA_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(PlaceService.REFRESH_PLACE_DATA_FAIL));
		}
	}
}