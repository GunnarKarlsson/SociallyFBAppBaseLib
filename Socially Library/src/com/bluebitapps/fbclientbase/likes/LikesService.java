/*
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.likes;

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

//TODO: need to remove albums from db that have been deleted on FB. Need to clear db, then insert new albums.

public class LikesService extends IntentService {

	public static final String REFRESH_LIKES_DATA_SUCCESS = "refresh likes data success";
	public static final String REFRESH_LIKES_DATA_FAIL = "refresh likes data fail";
	public static final String LIKES_COUNT_KEY = "albums count key";

	private String mObjectId;

	public LikesService() {
		super("LikesService");
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

		if (intent == null) {
			return;
		}

		if (intent.getExtras() == null) {
			return;
		}

		Bundle bundle = intent.getExtras();

		mObjectId = bundle.getString(Constants.OBJECT_ID_KEY);
		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();
		if (isValidSession) {
			getLikes();
		}
	}

	private void getLikes() {
		Logger.i(LikesService.class.getSimpleName() + "#getLikes()");

		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(mObjectId + "/likes", new LikesListener());
	}

	private class LikesListener implements RequestListener {

		List<LikedObject> likes = new ArrayList<LikedObject>();

		@Override
		public void onComplete(String response, Object state) {
			try {
				Logger.i(LikesListener.class.getSimpleName() + "#onComplete()");

				final JSONObject dataJsonObject = new JSONObject(response);
				final JSONArray jsonArray = dataJsonObject.getJSONArray("data");

				Logger.i("Number of likes: " + jsonArray.length());
				int LikesCount = jsonArray.length();

				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject obj = jsonArray.getJSONObject(i);

						Logger.i(obj.toString());
						LikedObject likedObject = LikedObject.fromJSON(obj, mObjectId);
						likes.add(likedObject);
					}
				}

				FBClientApplication.getApplication().getAlbumsData().deleteRowsForUser(mObjectId);

				try {
					for (LikedObject like : likes) {
						Logger.i(LikesListener.class.getSimpleName() + "#values:" + like.toString());
						((FBClientApplication) getApplication()).getLikesData().insertOrIgnore(like.toContentValues());
					}
				} catch (ConcurrentModificationException e) {
					sendBroadcast(new Intent(LikesService.REFRESH_LIKES_DATA_FAIL));
					return;
				}

				Intent intent = new Intent(LikesService.REFRESH_LIKES_DATA_SUCCESS);
				intent.putExtra(LikesService.LIKES_COUNT_KEY, LikesCount);
				sendBroadcast(intent);

			} catch (JSONException e) {
				Logger.i(Logger.getClassAndMethod() + e.toString());
				sendBroadcast(new Intent(LikesService.REFRESH_LIKES_DATA_FAIL));
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(LikesService.REFRESH_LIKES_DATA_FAIL));
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(LikesService.REFRESH_LIKES_DATA_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(LikesService.REFRESH_LIKES_DATA_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			sendBroadcast(new Intent(LikesService.REFRESH_LIKES_DATA_FAIL));
		}

	}

}