/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.albums;

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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

//TODO: need to remove albums from db that have been deleted on FB. Need to clear db, then insert new albums.

public class AlbumsService extends IntentService {

	public static final String REFRESH_ALBUMS_DATA_SUCCESS = "refresh album data success";
	public static final String REFRESH_ALBUMS_DATA_FAIL = "refresh album data fail";
	public static final String ALBUMS_COUNT_KEY = "albums count key";

	private Context mContext;
	private String mObjectId;

	public AlbumsService() {
		super("AlbumsService");
		mContext = this;
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
			getAlbums();
		}
	}

	private void getAlbums() {

		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(mObjectId + "/albums", new AlbumsListener());

		Logger.i(AlbumsService.class.getSimpleName() + "#getAlbums()");
	}

	private class AlbumsListener implements RequestListener {

		List<Album> albums = new ArrayList<Album>();

		@Override
		public void onComplete(String response, Object state) {
			try {
				Logger.i(AlbumsListener.class.getSimpleName() + "#onComplete()");

				final JSONObject dataJsonObject = new JSONObject(response);
				final JSONArray jsonArray = dataJsonObject.getJSONArray("data");

				Logger.i("Number of albums: " + jsonArray.length());
				int albumsCount = jsonArray.length();

				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject obj = jsonArray.getJSONObject(i);
						Album album = Album.fromJson(obj, mContext, mObjectId);
						albums.add(album);
					}
				}

				FBClientApplication.getApplication().getAlbumsData().deleteRowsForUser(mObjectId);
				try {
					for (Album album : albums) {
						((FBClientApplication) getApplication()).getAlbumsData().insertOrIgnore(album.toContentValues());
						Logger.i(AlbumsListener.class.getSimpleName() + "#values:" + album.toString().toString());
					}
				} catch (ConcurrentModificationException e) {
					Logger.i(Logger.getClassAndMethod() + e.toString());
					sendBroadcast(new Intent(AlbumsService.REFRESH_ALBUMS_DATA_FAIL));
					return;
				}

				Intent intent = new Intent(AlbumsService.REFRESH_ALBUMS_DATA_SUCCESS);
				intent.putExtra(AlbumsService.ALBUMS_COUNT_KEY, albumsCount);
				sendBroadcast(intent);

			} catch (JSONException e) {
				Logger.i(AlbumsListener.class.getSimpleName() + e.toString());
				sendBroadcast(new Intent(AlbumsService.REFRESH_ALBUMS_DATA_FAIL));
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(AlbumsListener.class.getSimpleName() + "#AlbumsListener" + e.toString());
			sendBroadcast(new Intent(AlbumsService.REFRESH_ALBUMS_DATA_FAIL));
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(AlbumsListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(AlbumsService.REFRESH_ALBUMS_DATA_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(AlbumsListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(AlbumsService.REFRESH_ALBUMS_DATA_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(AlbumsListener.class.getSimpleName() + e.toString());
			sendBroadcast(new Intent(AlbumsService.REFRESH_ALBUMS_DATA_FAIL));
		}
	}
}