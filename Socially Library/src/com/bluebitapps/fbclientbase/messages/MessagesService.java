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

package com.bluebitapps.fbclientbase.messages;

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
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class MessagesService extends IntentService {

	public static final String REFRESH_MESSAGES_DATA_SUCCESS = "refresh messages data intent";
	public static final String REFRESH_MESSAGES_DATA_FAIL = "refresh messages data fail";

	List<MessageThread> mMessageThreads = new ArrayList<MessageThread>();
	private boolean isCalledOnce = false;

	public MessagesService() {
		super("MesagesService");
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
		Log.i("feb7", Logger.getClassAndMethod());
		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();

		if (isCalledOnce) {
			Log.i("feb7", "has been called once - return");
			return;
		}

		isCalledOnce = true;

		if (isValidSession) {
			getMessages();
		}
	}

	private void getMessages() {
		Logger.i(Logger.getClassAndMethod());

		String query = "SELECT thread_id, subject, recipients, snippet, message_count, unread, updated_time FROM thread WHERE folder_id = 0";
		Bundle params = new Bundle();
		params.putString("method", "fql.query");
		params.putString("query", query);
		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(null, params, new MessagesListener());

	}

	private class MessagesListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {
			try {

				final JSONArray jsonArray = new JSONArray(response);

				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject obj = jsonArray.getJSONObject(i);
						MessageThread message = MessageThread.fromJSON(obj);
						mMessageThreads.add(message);
					}
				}

				String ids = "(";
				for (int i = 0; i < mMessageThreads.size(); i++) {
					ids += mMessageThreads.get(i).getFriendId() + ",";
				}
				String query = "";
				try {
					ids = ids.substring(0, ids.length() - 2);
					ids += ")";
					Logger.i(Logger.getClassAndMethod() + "ids: " + ids);
					query = "SELECT name, uid FROM user WHERE uid IN " + ids;
				} catch (StringIndexOutOfBoundsException e) {
					Logger.i(Logger.getClassAndMethod() + e.toString());
					sendBroadcast(new Intent(MessagesService.REFRESH_MESSAGES_DATA_FAIL));
				}

				Bundle params = new Bundle();
				params.putString("method", "fql.query");
				params.putString("query", query);
				((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(null, params, new NamesListener());

			} catch (JSONException e) {
				Logger.i(Logger.getClassAndMethod() + e.toString());
				sendBroadcast(new Intent(MessagesService.REFRESH_MESSAGES_DATA_FAIL));
			} catch (StringIndexOutOfBoundsException e) {
				Logger.i(Logger.getClassAndMethod() + e.toString());
				sendBroadcast(new Intent(MessagesService.REFRESH_MESSAGES_DATA_FAIL));
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(MessagesService.REFRESH_MESSAGES_DATA_FAIL));
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(MessagesService.REFRESH_MESSAGES_DATA_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(MessagesService.REFRESH_MESSAGES_DATA_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(MessagesService.REFRESH_MESSAGES_DATA_FAIL));
		}
	}

	private class NamesListener implements RequestListener {

		HashMap<String, String> namesMap = new HashMap<String, String>();

		@Override
		public void onComplete(String response, Object state) {
			try {
				JSONArray data = new JSONArray(response);

				// Place names in map with uid as key
				for (int i = 0; i < data.length(); i++) {
					String uid = data.getJSONObject(i).getString("uid");
					String name = data.getJSONObject(i).getString("name");
					namesMap.put(uid, name);
				}

				// set name from map in thread list
				for (int j = 0; j < mMessageThreads.size(); j++) {
					String friendName = namesMap.get(mMessageThreads.get(j).getFriendId());
					String friendId = mMessageThreads.get(j).getFriendId();
					mMessageThreads.get(j).setFriendName(friendName);
					mMessageThreads.get(j).setFriendId(friendId);
				}

				// save to db and broadcast
				try {
					for (MessageThread message : mMessageThreads) {
						((FBClientApplication) getApplication()).getMessagesData().insertOrIgnore(message.toContentValues());
					}
				} catch (ConcurrentModificationException e) {
					sendBroadcast(new Intent(MessagesService.REFRESH_MESSAGES_DATA_FAIL));
					return;
				}

				sendBroadcast(new Intent(MessagesService.REFRESH_MESSAGES_DATA_SUCCESS));

			} catch (JSONException e) {
				Logger.i(Logger.getClassAndMethod() + e.toString());
				sendBroadcast(new Intent(MessagesService.REFRESH_MESSAGES_DATA_FAIL));
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(MessagesService.REFRESH_MESSAGES_DATA_FAIL));
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(MessagesService.REFRESH_MESSAGES_DATA_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(MessagesService.REFRESH_MESSAGES_DATA_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(MessagesService.REFRESH_MESSAGES_DATA_FAIL));
		}
	}
}