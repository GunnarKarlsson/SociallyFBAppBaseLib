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

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

/**
 * Posts a comment for an object.
 * 
 * @author Gunnar Karlsson
 * 
 */

public class EventInvitationResponseService extends IntentService {

	public static final String RESPONSE_POST_SUCCESS = "comment post success";
	public static final String RESPONSE_POST_FAIL = "comment post fail";

	private String mObjectId;

	public EventInvitationResponseService() {
		super("EventInvitationResponseService");
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
		Log.i("jan30", Logger.getClassAndMethod());
		Bundle bundle = intent.getExtras();
		String action = bundle.getString(Constants.ACTION_TYPE);
		mObjectId = bundle.getString(Constants.OBJECT_ID_KEY);

		if (mObjectId == null) {
			return;
		}

		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();
		if (isValidSession) {
			if (Constants.EVENT_INVITATION_RESPONSE_DECLINE.equals(action)) {
				declineEvent();
			} else if (Constants.EVENT_INVITATION_RESPONSE_JOIN.equals(action)) {
				joinEvent();
			} else if (Constants.EVENT_INVITATION_RESPONSE_MAYBE.equals(action)) {
				maybeJoinEvent();
			}
		}
	}

	private void joinEvent() {
		Log.i("jan30", Logger.getClassAndMethod());
		Bundle params = new Bundle();
		String query = mObjectId + "/attending";
		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(query, params, "POST", new EventResponseListener(), null);
	}

	private void declineEvent() {
		Bundle params = new Bundle();
		String query = mObjectId + "/declined";
		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(query, params, "post", new EventResponseListener(), null);
	}

	private void maybeJoinEvent() {
		Bundle params = new Bundle();
		String query = mObjectId + "/maybe";
		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(query, params, "post", new EventResponseListener(), null);
	}

	private class EventResponseListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {
			Log.i("jan30", "Response: " + response.toString());
			if ("true".equals(response)) {
				FBClientApplication.getApplication().getEventsData().updateRsvpStatusForEvent(mObjectId, "attending");
				
				Intent intent = new Intent(EventInvitationResponseService.RESPONSE_POST_SUCCESS);
				sendBroadcast(intent);
			} else {
				sendBroadcast(new Intent(EventInvitationResponseService.RESPONSE_POST_FAIL));
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(EventInvitationResponseService.RESPONSE_POST_FAIL));
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(EventInvitationResponseService.RESPONSE_POST_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(EventInvitationResponseService.RESPONSE_POST_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(EventInvitationResponseService.RESPONSE_POST_FAIL));
		}
	}
}