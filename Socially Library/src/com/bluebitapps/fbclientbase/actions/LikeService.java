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

package com.bluebitapps.fbclientbase.actions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

/**
 * Posts a user like for an object.
 * 
 * @author Gunnar Karlsson
 * 
 */

public class LikeService extends IntentService {

	public static final String LIKE_RESPONSE_INTENT = "like response intent";
	public static final String LIKE_RESPONSE_FAIL = "like response fail";

	public LikeService() {
		super("LikeService");
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
		Logger.i(LikeService.class.getSimpleName());
		
		if(intent == null){
			return;
		}

		if(intent.getExtras()==null){
			return;
		}
				
		Bundle bundle = intent.getExtras();
		String action = bundle.getString(Constants.ACTION_TYPE);

		String objectId = bundle.getString(Constants.LIKE_OBJECT_ID_KEY);

		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();
		if (isValidSession) {
			if (action.equalsIgnoreCase(Constants.ACTION_TYPE_UNLIKE)) {
				postUnlike(objectId);
			} else {
				postLike(objectId);
			}
		}
	}

	private void postLike(String objectId) {

		Logger.i(LikeService.class.getSimpleName() + "#postLike()");

		Bundle params = new Bundle();
		String query = objectId + "/likes";
		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(query, params, "POST", new LikePostListener(), null);
	}

	private void postUnlike(String objectId) {

		Logger.i(LikeService.class.getSimpleName() + "#postLike()");

		Bundle params = new Bundle();// need to pass in empty params, can't pass
										// in null.
		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(objectId + "/likes", params, "DELETE", new LikePostListener(), null);
	}

	private class LikePostListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {
			Logger.i(LikePostListener.class.getSimpleName() + "#onComplete()");
			Logger.i("Response: " + response.toString());

			// TODO: check json structure to write parse of boolean response and check for fail.

			sendBroadcast(new Intent(LikeService.LIKE_RESPONSE_INTENT));
			/*
			 * } catch (JSONException e) { if
			 * (FBClientApplication.isLoggingEnabled()) Log.i(TAG,
			 * LikePostListener.class.getSimpleName() + "JSONException() " +
			 * e.toString()); }
			 */
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(LikePostListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(LikeService.LIKE_RESPONSE_FAIL));
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(LikePostListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(LikeService.LIKE_RESPONSE_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(LikePostListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(LikeService.LIKE_RESPONSE_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(LikePostListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(LikeService.LIKE_RESPONSE_FAIL));
		}
	}
}