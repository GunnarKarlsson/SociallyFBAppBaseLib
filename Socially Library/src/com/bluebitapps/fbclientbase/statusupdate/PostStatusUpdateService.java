/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.statusupdate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bluebitapps.utils.StringUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.MainActivity;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.debug.Logger;

public class PostStatusUpdateService extends IntentService {

	public static final String TAG = "globalTag";
	public static final String MESSAGE_KEY = "message key";
	public static final String LINK_KEY = "link key";
	public static final String LINK_NAME_KEY = "link name key";
	public static final String PRIVACY_KEY = "privacy key";
	private static final int STATUS_UPDATE_NOTIFICATION_ID = 16;
	private String mMessage;
	private String mLink;
	private String mLinkName;
	private String mPrivacySetting;

	public PostStatusUpdateService() {
		super("PostStatusUpdateService");
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
		mMessage = bundle.getString(MESSAGE_KEY);
		mLink = bundle.getString(LINK_KEY);
		mLinkName = bundle.getString(LINK_NAME_KEY);
		mPrivacySetting = bundle.getString(PRIVACY_KEY);

		// test
		Logger.i(Logger.getClassAndMethod() + " mMessage: " + mMessage);
		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();
		if (isValidSession) {
			postStatusUpdate();
		}
	}

	private void postStatusUpdate() {

		Log.i("feb6", Logger.getClassAndMethod() + mPrivacySetting);

		// Log.i("feb6", Logger.getClassAndMethod() + privacy);

		Bundle params = new Bundle();
		if (StringUtil.notEmpty(mMessage)) {
			params.putString("message", mMessage);
		}
		params.putString("link", mLink);
		params.putString("name", mLinkName);

		JSONObject privacy = new JSONObject();
		try {
			privacy.put("value", mPrivacySetting);
			params.putString("privacy", privacy.toString());
		} catch (JSONException e) {
			Log.i("feb6", Logger.getClassAndMethod() + "privacy value exception: " + e);
		}

		FBClientApplication.getApplication().getFBConnection().getAsyncFacebookRunner().request("me/feed", params, "POST", new StatusUpdateListener(), null);
	}

	/*
	 * callback for the photo upload
	 */
	public class StatusUpdateListener implements RequestListener {

		@Override
		public void onComplete(final String response, final Object state) {
			Logger.i(Logger.getClassAndMethod());
			// TODO: check success
			sendNotification();
		}

		public void onFacebookError(FacebookError error) {
			sendErrorNotification();
		}

		@Override
		public void onIOException(IOException e, Object state) {
			sendErrorNotification();
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			sendErrorNotification();
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			sendErrorNotification();
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			sendErrorNotification();
		}
	}

	private void sendNotification() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notificationIntent.setAction(Constants.ACTION_STATUS_UPDATE_RESULT);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setContentIntent(contentIntent);
		builder.setContentTitle(getResources().getString(R.string.your_post_successfully_posted))
				.setContentText(getResources().getString(R.string.your_post_has_been_successfully_posted_to_facebook_from_socially)).setSmallIcon(R.drawable.status_update_ok);

		Notification notification = builder.getNotification();
		notificationManager.notify(STATUS_UPDATE_NOTIFICATION_ID, notification);
	}

	private void sendErrorNotification() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notificationIntent.setAction(Constants.ACTION_STATUS_UPDATE_RESULT);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setContentIntent(contentIntent);
		builder.setContentTitle(getResources().getString(R.string.post_could_not_be_posted)).setContentText(getResources().getString(R.string.your_post_could_not_be_posted_please_try_again))
				.setSmallIcon(R.drawable.status_update_fail);
		Notification notification = builder.getNotification();
		notificationManager.notify(STATUS_UPDATE_NOTIFICATION_ID, notification);
	}
}