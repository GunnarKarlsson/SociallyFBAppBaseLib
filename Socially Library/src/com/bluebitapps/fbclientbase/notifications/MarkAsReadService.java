/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.notifications;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.MainActivity;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseSlidingMenuActivity;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.photos.UploadPhotoService;
import com.bluebitapps.fbclientbase.photos.UploadPhotoService.PhotoUploadListener;

import android.R.bool;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bluebitapps.utils.StringUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

public class MarkAsReadService extends IntentService {

	public static final String MARK_AS_READ_SUCCESS = "mark as read success";
	public static final String MARK_AS_READ_FAIL = "mark as read fail";

	// private String mNotificationId;

	private ArrayList<String> mIds;

	/**
	 * The constructor should not take a String parameter, or it will throw
	 * java.lang.InstantiationException. It must pass a String name to super.
	 */

	public MarkAsReadService() {
		super("MarkAsReadService");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i("jan30", Logger.getClassAndMethod());
		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();

		if(intent == null){
			return;
		}
		
		Bundle bundle = intent.getExtras();

		if (bundle == null) {
			return;
		}

		mIds = bundle.getStringArrayList(Constants.OBJECT_ID_KEY);
		Log.i("jan23", Logger.getClassAndMethod() + mIds.toString());

		if (isValidSession) {
			postMarkAsRead();
		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private void postMarkAsRead() {

		JSONArray batch_array = new JSONArray();

		String userId = FBClientApplication.getApplication().getFBConnection().getUserId();

		for (int i = 0; i < mIds.size(); i++) {
			Log.i("jan30", Logger.getClassAndMethod() + "mIds.get(i): " + mIds.get(i));
			String query = "notif_" + userId + "_" + mIds.get(i);
			Log.i("jan30", Logger.getClassAndMethod() + "query: " + query);
			JSONObject notification = new JSONObject();
			try {
				notification.put("method", "POST");
				notification.put("relative_url", query);
				notification.put("body", "unread=0");
				// notification.put("unread", unreadValue);
				batch_array.put(notification);
			} catch (JSONException e) {
				e.printStackTrace();
				Log.e("jan23", e.getMessage());
			}
		}

		Log.i("jan23", "batch_array: " + batch_array.toString());
		String url = "https://graph.facebook.com";

		Bundle args = new Bundle();
		args.putString("access_token", FBClientApplication.getApplication().getFBConnection().getFacebook().getAccessToken());
		args.putString("batch", batch_array.toString());

		String ret = "";
		int counter = 0;

		try {
			ret = Util.openUrl(url, "POST", args);
			Log.i("jan30", "response " + ret);

			try {

				JSONArray array = new JSONArray(ret);
				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = array.getJSONObject(i);
					if (obj.has("code")) {
						String code = obj.getString("code");
						if ("200".equalsIgnoreCase(code)) {
							counter++;
						}
					}
				}

			} catch (JSONException e) {
				sendFailMessage();
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (counter >= mIds.size()) {
			for (int i = 0; i < mIds.size(); i++) {
				boolean isDeleted = FBClientApplication.getApplication().getNotificationsData().deleteTitle(mIds.get(i));

				if (isDeleted) {
					Log.i("jan23", "success: " + mIds.get(i));
					Intent intent = new Intent();
					intent.putExtra("notification_id", mIds.get(i));
					intent.setAction(MarkAsReadService.MARK_AS_READ_SUCCESS);
					sendBroadcast(intent);
				} else {
					Log.i("jan23", "fail: " + mIds.get(i));
					sendFailMessage();
				}
			}
		}
	
	}

	private void sendFailMessage() {
		Logger.i(Logger.getClassAndMethod());
		Intent intent = new Intent();
		// intent.putExtra("notification_id", mNotificationId);
		intent.setAction(MarkAsReadService.MARK_AS_READ_FAIL);
		sendBroadcast(intent);
	}
	
}