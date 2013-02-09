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

package com.bluebitapps.fbclientbase.friends;

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

import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class FriendsService extends IntentService {

	public static final String REFRESH_FRIENDS_DATA_SUCCESS = "refresh friends data success";
	public static final String REFRESH_FRIENDS_DATA_FAIL = "refresh friends data fail";

	public FriendsService() {
		super("FriendsService");
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
		Logger.i(FriendsService.class.getSimpleName() + "#onHandleIntent()");
		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();
		if (isValidSession) {
			getFriends();
		}
	}

	private void getFriends() {
		Logger.i(FriendsService.class.getSimpleName() + "#getFriends()");

		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request("me/friends", new FriendsListener());
	}

	private class FriendsListener implements RequestListener {

		List<Friend> friends = new ArrayList<Friend>();

		@Override
		public void onComplete(String response, Object state) {
			try {
				Logger.i(FriendsService.class.getSimpleName() + "." + FriendsListener.class.getSimpleName() + "#onComplete()");
				Logger.i("response: " + response.toString());

				final JSONObject dataJsonObject = new JSONObject(response);
				final JSONArray jsonArray = dataJsonObject.getJSONArray("data");

				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject obj = jsonArray.getJSONObject(i);
						Friend friend = Friend.fromJSON(obj);
						friends.add(friend);
					}
				}

				try{
				for (Friend friend : friends) {
					((FBClientApplication) getApplication()).getFriendsData().insertOrIgnore(friend.toContentValues());
				}
				}catch(ConcurrentModificationException e){
					sendBroadcast(new Intent(FriendsService.REFRESH_FRIENDS_DATA_FAIL));
					return;
				}

				sendBroadcast(new Intent(FriendsService.REFRESH_FRIENDS_DATA_SUCCESS));

			} catch (JSONException e) {
				Logger.i(FriendsService.class.getSimpleName() + "." + FriendsListener.class.getSimpleName() + e.toString());
				sendBroadcast(new Intent(FriendsService.REFRESH_FRIENDS_DATA_FAIL));
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(FriendsService.class.getSimpleName() + "." + FriendsListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(FriendsService.REFRESH_FRIENDS_DATA_FAIL));
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(FriendsService.class.getSimpleName() + "." + FriendsListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(FriendsService.REFRESH_FRIENDS_DATA_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(FriendsService.class.getSimpleName() + "." + FriendsListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(FriendsService.REFRESH_FRIENDS_DATA_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(FriendsService.class.getSimpleName() + "." + FriendsListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(FriendsService.REFRESH_FRIENDS_DATA_FAIL));
		}
	}
}