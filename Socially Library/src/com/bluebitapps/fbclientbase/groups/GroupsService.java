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

package com.bluebitapps.fbclientbase.groups;

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

public class GroupsService extends IntentService {

	public static final String REFRESH_GROUPS_DATA_SUCCESS = "refresh groups data success";
	public static final String REFRESH_GROUPS_DATA_FAIL = "refresh groups data fail";

	public GroupsService() {
		super("GroupsService");
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
	protected void onHandleIntent(Intent arg0) {
		Logger.i(Logger.getClassAndMethod());
		
		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();
		if (isValidSession) {
			getGroups();
		}
	}

	private void getGroups() {
		Logger.i(Logger.getClassAndMethod());

		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request("me/groups", new GroupsListener());
	}

	private class GroupsListener implements RequestListener {

		List<Group> groups = new ArrayList<Group>();

		@Override
		public void onComplete(String response, Object state) {
			try {
				Logger.i(Logger.getClassAndMethod());
				Logger.i("Response: " + response.toString());

				final JSONObject dataJsonObject = new JSONObject(response);
				final JSONArray jsonArray = dataJsonObject.getJSONArray("data");

				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject obj = jsonArray.getJSONObject(i);
						Group group = Group.fromJSON(obj);
						groups.add(group);
					}
				}

				try{
				for (Group group : groups) {
					((FBClientApplication) getApplication()).getGroupsData().insertOrIgnore(group.toContentValues());
					Logger.i(GroupsListener.class.getSimpleName() + "#values:" + group.toString().toString());
				}
				}catch(ConcurrentModificationException e){
					sendBroadcast(new Intent(GroupsService.REFRESH_GROUPS_DATA_FAIL));
					return;
				}

				sendBroadcast(new Intent(GroupsService.REFRESH_GROUPS_DATA_SUCCESS));

			} catch (JSONException e) {
				Logger.i(Logger.getClassAndMethod() + e.toString());
				sendBroadcast(new Intent(GroupsService.REFRESH_GROUPS_DATA_FAIL));
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(GroupsService.REFRESH_GROUPS_DATA_FAIL));
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(GroupsService.REFRESH_GROUPS_DATA_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(GroupsService.REFRESH_GROUPS_DATA_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(GroupsService.REFRESH_GROUPS_DATA_FAIL));
		}
	}
}