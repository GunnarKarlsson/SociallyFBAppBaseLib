/* Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.checkins;

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

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.MainActivity;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.debug.Logger;

public class PostCheckinService extends IntentService {

	public static final String ACTION_POSTED_CHECKIN = "posted checkin";
	private static final int CHECKIN_NOTIFICATION_ID = 8;
	
	private String mMessage;
	private String mPlaceName;
	private String mPlaceId;
	//private String mPlaceLocation;
	//TODO: add location to Post request
	private String mLatitude;
	private String mLongitude;
	private String mPrivacySetting;
	
	public PostCheckinService() {
		super("PostCheckinService");
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
		
		if(intent == null){
			return;
		}
		
		if(intent.getExtras()==null){
			return;
		}
		
		Bundle bundle = intent.getExtras();
		
		mMessage = bundle.getString(PostCheckinActivity.MESSAGE_KEY);
		mPlaceName = bundle.getString(PostCheckinActivity.PLACE_NAME_KEY);
		mPlaceId = bundle.getString(PostCheckinActivity.PLACE_ID_KEY);
		//mPlaceLocation = bundle.getString(PostCheckinActivity.PLACE_LOCATION_KEY);
		mLatitude = bundle.getString(PostCheckinActivity.LATITUDE_KEY);
		mLongitude = bundle.getString(PostCheckinActivity.LONGITUDE_KEY);
		mPrivacySetting = bundle.getString(PostCheckinActivity.PRIVACY_SETTING_KEY);
		
		Logger.i(PostCheckinService.class.getSimpleName() + "#onHandleIntent mPlaceName: " + mPlaceName);
		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();
		if (isValidSession) {
			postCheckin();
		}
	}

	private void postCheckin() {

		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.setAction(Constants.ACTION_STATUS_UPDATE_RESULT);
		
		Bundle params = new Bundle();
		params.putString("place", mPlaceId);
		
		if(mMessage != null){			
			params.putString("message", mMessage);
		}
		
		JSONObject privacy = new JSONObject();
		try {
			privacy.put("value", mPrivacySetting);
			params.putString("privacy", privacy.toString());
		} catch (JSONException e) {
			Log.i("feb6", Logger.getClassAndMethod() + "privacy value exception: " + e);
		}
		
		try {
			JSONObject coordinates = new JSONObject();
			coordinates.put("latitude", mLatitude);
			coordinates.put("longitude", mLongitude);
			params.putString("coordinates", coordinates.toString());
			FBClientApplication.getApplication().getFBConnection().getAsyncFacebookRunner().request("me/checkins", params, "POST", new PlacesCheckInListener(), null);
		} catch (JSONException e) {
			Logger.i(PostCheckinService.class.getSimpleName() + "#postCheckin" + e.toString());
			sendErrorNotification();
		}
	}
	
	public class PlacesCheckInListener implements RequestListener {
		@Override
		public void onComplete(final String response, final Object state) {
			Logger.i("PostCheckinService: Checkin posted successfully: "+response.toString());
			sendNotification();
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(e.toString());
			sendErrorNotification();
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(e.toString());
			sendErrorNotification();
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(e.toString());
			sendErrorNotification();
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(e.toString());
			sendErrorNotification();
		}		
	}

	private void sendNotification() {
		Intent notificationIntent = new Intent(PostCheckinService.this, MainActivity.class);
		notificationIntent.setAction(PostCheckinService.ACTION_POSTED_CHECKIN);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		PendingIntent contentIntent = PendingIntent.getActivity(PostCheckinService.this, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(PostCheckinService.this);
		builder.setContentIntent(contentIntent).setSmallIcon(R.drawable.sliding_menu_icon_checkins).setTicker("Checkin successfully posted").setWhen(System.currentTimeMillis()).setAutoCancel(true).setContentTitle("Checkin posted to Facebook").setContentText("Your checkin was posted to Facebook");
		Notification notification = builder.getNotification();
		notificationManager.notify(CHECKIN_NOTIFICATION_ID, notification);
	}

	private void sendErrorNotification() {
		Intent notificationIntent = new Intent(PostCheckinService.this, MainActivity.class);
		notificationIntent.setAction(PostCheckinService.ACTION_POSTED_CHECKIN);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(PostCheckinService.this, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(PostCheckinService.this);
		builder.setContentIntent(contentIntent).setSmallIcon(R.drawable.sliding_menu_icon_checkins).setTicker("Checkin not posted").setWhen(System.currentTimeMillis()).setAutoCancel(true).setContentTitle("Checkin not posted to Facebook").setContentText("Your checkin was not posted. Please try again.");
		Notification notification = builder.getNotification();
		notificationManager.notify(CHECKIN_NOTIFICATION_ID, notification);
	}
}