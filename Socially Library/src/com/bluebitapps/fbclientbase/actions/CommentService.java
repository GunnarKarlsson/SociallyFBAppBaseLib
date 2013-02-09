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

package com.bluebitapps.fbclientbase.actions;

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
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

/**
 * Posts a comment for an object.
 * 
 * @author Gunnar Karlsson
 * 
 */

public class CommentService extends IntentService {

	public static final String COMMENT_POST_SUCCESS = "comment post success";
	public static final String COMMENT_POST_FAIL = "comment post fail";

	public CommentService() {
		super("CommentService");
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
		Logger.i(CommentService.class.getSimpleName() + "#onHandleIntent");

		if(intent == null){
			return;
		}
		
		if(intent.getExtras()==null){
			return;
		}
		
		Bundle bundle = intent.getExtras();
		String action = bundle.getString(Constants.ACTION_TYPE);
		String message = bundle.getString(Constants.MESSAGE_KEY);

		String objectId = bundle.getString(Constants.COMMENT_OBJECT_ID_KEY);

		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();
		if (isValidSession) {
			if (action.equalsIgnoreCase(Constants.ACTION_TYPE_DELETE_COMMENT)) {
				deleteComment(objectId);
			} else {
				postComment(objectId, message);
			}
		}
	}

	private void postComment(String objectId, String message) {
		Logger.i(CommentService.class.getSimpleName() + "#postComment()");

		Bundle params = new Bundle();
		params.putString("message", message);

		String query = objectId + "/comments";

		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(query, params, "POST", new CommentPostListener(), null);
	}

	private void deleteComment(String objectId) {
		Logger.i(CommentService.class.getSimpleName() + "#deleteComment()");

		// need to pass in empty params, can't pass in null.
		Bundle params = new Bundle();

		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(objectId + "/comments", params, "DELETE", new CommentPostListener(), null);

	}

	private class CommentPostListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {
			Logger.i(CommentPostListener.class.getSimpleName() + "#onComplete()");
			Logger.i("Response: " + response.toString());
			try {
				JSONObject reply = new JSONObject(response);
				if (reply.has("id")) {
					Intent intent = new Intent(CommentService.COMMENT_POST_SUCCESS);
					sendBroadcast(intent);
				} else {
					sendBroadcast(new Intent(CommentService.COMMENT_POST_FAIL));
				}
			} catch (JSONException e) {
				Logger.i(CommentService.class.getSimpleName() + "." + CommentPostListener.class.getSimpleName() + ": " + e.toString());
				sendBroadcast(new Intent(CommentService.COMMENT_POST_FAIL));
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(CommentService.class.getSimpleName() + "." + CommentPostListener.class.getSimpleName() + ": " + e.toString());
			sendBroadcast(new Intent(CommentService.COMMENT_POST_FAIL));
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(CommentService.class.getSimpleName() + "." + CommentPostListener.class.getSimpleName() + ": " + e.toString());
			sendBroadcast(new Intent(CommentService.COMMENT_POST_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(CommentService.class.getSimpleName() + "." + CommentPostListener.class.getSimpleName() + ": " + e.toString());
			sendBroadcast(new Intent(CommentService.COMMENT_POST_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(CommentService.class.getSimpleName() + "." + CommentPostListener.class.getSimpleName() + ": " + e.toString());
			sendBroadcast(new Intent(CommentService.COMMENT_POST_FAIL));
		}
	}
}