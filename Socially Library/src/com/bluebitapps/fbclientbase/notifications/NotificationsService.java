/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.notifications;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
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
import com.bluebitapps.fbclientbase.events.EventsInvitationService;
import com.bluebitapps.fbclientbase.events.EventsService;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bluebitapps.utils.StringUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

/**
 * NotificationsService requests the user's Facebook notifications and saved
 * them in a database.
 * 
 * Beware the idiosyncrasies of IntentService: - Constructor should not take a
 * String parameter, but should pass one to super. - Must override
 * onStartCommand(), or onHandleIntent() will not be called.
 * 
 * @author Gunnar Karlsson
 * 
 */

public class NotificationsService extends IntentService {

	public static final String SCHEDULED_NOTIFICATIONS_REQUEST = "scheduled notifications request";
	public static final String NOTIFICATION_REQUEST_TYPE_KEY = "notifications request type key";
	public static final String REFRESH_NOTIFICATIONS_DATA_SUCCESS = "refresh notifications data intent";
	public static final String REFRESH_NOTIFICATIONS_FAIL = "refresh notifications fail";
	public static final String FLAG_IS_REQUEST_FROM_FRAGMENT = "key is request from fragment";

	private boolean includeStream = true;
	private boolean includePhotos = true;
	private boolean includePage = true;
	private boolean includeEvents = true;
	private boolean includeGroups = true;
	private boolean includeFriendRequests = true;
	private boolean isRequestFromFragment = false;

	/**
	 * The constructor should not take a String parameter, or it will throw
	 * java.lang.InstantiationException. It must pass a String name to super.
	 */

	public NotificationsService() {
		super("NotificationService");

		Logger.i(NotificationsService.class.getSimpleName() + "#constructor");

	}

	/**
	 * Need to override onStartCommand() or onHandleIntent() will not be called.
	 * Is called for every call from e.g. alarm manager started in
	 * broadcastreceiver.
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
		
		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();

		isRequestFromFragment = intent.getBooleanExtra(NotificationsService.FLAG_IS_REQUEST_FROM_FRAGMENT, false);
		Log.i("jan22", "isRequestFromFragment: " + isRequestFromFragment);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		if(prefs==null){
			return;
		}

		includeStream = prefs.getBoolean("stream", true);
		includePhotos = prefs.getBoolean("photo", true);
		includePage = prefs.getBoolean("page", true);
		includeEvents = prefs.getBoolean("event", true);
		includeGroups = prefs.getBoolean("group", true);
		includeFriendRequests = prefs.getBoolean("friendrequest", true);

		if (isValidSession) {
			getNotifications();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private void getNotifications() {
		Log.i("notiftype", Logger.getClassAndMethod());
		final String query1 = "select notification_id, sender_id, created_time, title_text, body_text, app_id, is_unread, is_hidden, object_id, object_type from notification where recipient_id=me() and is_unread=1";
		final String query2 = "select name, uid from user where uid IN (SELECT sender_id FROM #query1)";

		final JSONObject jsonQueries = new JSONObject() {
			{
				try {
					put("query1", query1);
					put("query2", query2);
				} catch (Exception e) {
					Logger.i(NotificationsService.class.getSimpleName() + "#getNotifications().jsonQueries: " + e.toString());
				}
			}
		};

		Bundle params = new Bundle();
		params.putString("method", "fql.multiquery");
		params.putString("queries", jsonQueries.toString());
		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(null, params, new NotificationsListener());
	}

	private class NotificationsListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {

			Log.i("notiftype", Logger.getClassAndMethod() + response);

			final List<FBNotification> notifications = new ArrayList<FBNotification>();

			try {
				JSONArray a = new JSONArray(response);
				JSONObject notificationsObj = a.getJSONObject(0);
				JSONObject userNamesObj = a.getJSONObject(1);
				JSONArray messagesJsonArray = notificationsObj.getJSONArray("fql_result_set");
				JSONArray userNamesJsonArray = userNamesObj.getJSONArray("fql_result_set");

				HashMap<String, String> userNamesMap = new HashMap<String, String>();

				if (userNamesJsonArray.length() > 0) {

					// Place user names in map
					for (int i = 0; i < userNamesJsonArray.length(); i++) {
						String uid = "";
						String name = "";
						if (userNamesJsonArray.getJSONObject(i).has("uid")) {
							uid = userNamesJsonArray.getJSONObject(i).getString("uid");
						}
						if (userNamesJsonArray.getJSONObject(i).has("name")) {
							name = userNamesJsonArray.getJSONObject(i).getString("name");
						}

						userNamesMap.put(uid, name);
					}
				}

				// Create notification list an add names from map to list
				if (messagesJsonArray.length() > 0) {
					for (int i = 0; i < messagesJsonArray.length(); i++) {
						JSONObject obj = messagesJsonArray.getJSONObject(i);
						FBNotification notification = FBNotification.fromJSON(obj);

						if (userNamesMap.containsKey(notification.getSenderId())) {
							String name = userNamesMap.get(notification.getSenderId());
							notification.setSenderName(name);
						}
						
						if (StringUtil.notEmpty(notification.getTitleText())) {
							if (StringUtil.stringContainsItemFromList(notification.getTitleText(), Constants.forbiddenStringsForNotifications)) {
								continue;
							}
						}

						if (includeStream && "stream".equalsIgnoreCase(notification.getObjectType())) {
							notifications.add(notification);
						} else if (includePhotos && "photo".equalsIgnoreCase(notification.getObjectType())) {
							notifications.add(notification);
						} else if (includePage && "page".equalsIgnoreCase(notification.getObjectType())) {
							notifications.add(notification);
						} else if (includeEvents && "event".equalsIgnoreCase(notification.getObjectType())) {
							notifications.add(notification);
							startService(new Intent(NotificationsService.this, EventsInvitationService.class));
						} else if (includeGroups && "group".equalsIgnoreCase(notification.getObjectType())) {
							notifications.add(notification);
						} else if (includeFriendRequests && ("friend_request".equalsIgnoreCase(notification.getObjectType()) || ("friendrequest".equalsIgnoreCase(notification.getObjectType())))) {
							notifications.add(notification);
						}
					}
				}

			} catch (JSONException e) {
				Logger.i(NotificationsService.class.getSimpleName() + "." + NotificationsListener.class.getSimpleName() + "." + e.toString());
			}

			int notificationsCount = notifications.size();
			
			// Add notifications to database
			try{
			for (FBNotification notification : notifications) {
				FBClientApplication application = (FBClientApplication) getApplication();
				application.getNotificationsData().insertOrIgnore(notification.getContentValues());
			}
			}catch(ConcurrentModificationException e){
				sendBroadcast(new Intent(NotificationsService.REFRESH_NOTIFICATIONS_FAIL));
				return;
			}

			//Notify fragment
			sendBroadcast(new Intent(NotificationsService.REFRESH_NOTIFICATIONS_DATA_SUCCESS));

			//Notify SlidingMenu
			Intent intent = new Intent(Constants.ACTION_NEW_NOTIFICATIONS);
			intent.putExtra(Constants.NOTIFICATION_COUNT_KEY, notificationsCount);
			sendBroadcast(intent);

			if (!isRequestFromFragment) {
				createSystemNotification();
			}

		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(NotificationsService.class.getSimpleName() + "." + NotificationsListener.class.getSimpleName() + "." + e.toString());
			sendBroadcast(new Intent(NotificationsService.REFRESH_NOTIFICATIONS_FAIL));
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(NotificationsService.class.getSimpleName() + "." + NotificationsListener.class.getSimpleName() + "." + e.toString());
			sendBroadcast(new Intent(NotificationsService.REFRESH_NOTIFICATIONS_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(NotificationsService.class.getSimpleName() + "." + NotificationsListener.class.getSimpleName() + "." + e.toString());
			sendBroadcast(new Intent(NotificationsService.REFRESH_NOTIFICATIONS_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(NotificationsService.class.getSimpleName() + "." + NotificationsListener.class.getSimpleName() + "." + e.toString());
			sendBroadcast(new Intent(NotificationsService.REFRESH_NOTIFICATIONS_FAIL));
		}

	}

	private void createSystemNotification() {

		
		long unreadCount = FBClientApplication.getApplication().getNotificationsData().getUnreadCount();
		Log.i("notiftype", "unreadCount: " + unreadCount);

		String notificationTitle;
		if (unreadCount > 1) {
			notificationTitle = unreadCount + " " + getResources().getString(R.string.new_notifications);
		} else if (unreadCount == 1) {
			notificationTitle = getResources().getString(R.string.one_new_notification);
		} else {
			return;
		}		
		
		FBClientApplication app = FBClientApplication.getApplication();
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);

		String notifDeliveryTypeKey = "";

		try {
			notifDeliveryTypeKey = app.getResources().getString(com.bluebitapps.fbclientbase.R.string.PREFS_NOTIFICATION_DELIVERY_KEY);

		} catch (NotFoundException e) {
			//TODO: handle ?
		}

		String type = sharedPreferences.getString(notifDeliveryTypeKey, "statusbar");
		
		if("statusbar".equals(type)){
			postToStatusBar(unreadCount, notificationTitle);
		}else if("popup".equals(type)){
			launchPopup();
		}
		
	}
	
	private void postToStatusBar(long unreadCount, String title){
		Intent intent = new Intent(Constants.ACTION_NEW_NOTIFICATIONS);
		intent.putExtra(Constants.NOTIFICATION_COUNT_KEY, unreadCount);
		sendBroadcast(intent);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(com.bluebitapps.fbclientbase.R.drawable.notification_icon_for_notifications_drawer, getResources().getString(R.string.notification), System.currentTimeMillis());
		Context context = getApplicationContext();

		String notificationText = getResources().getString(R.string.go_to_socially_to_read_new_notifications);

		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.setAction(Constants.REQUEST_NOTIFICATIONS);
		PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// if setLastestEventInfo isn't set, notification will not appear.
		notification.setLatestEventInfo(context, title, notificationText, pendingIntent);

		notificationManager.notify(1, notification);
	}
	
	private void launchPopup(){
		Intent dialogIntent = new Intent(getBaseContext(), NotificationsAlertActivity.class);
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //NEEDS TO BE LAUNCHED ONLY ONCE OR REPLACE THE LATEST
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);//DON'T MOVE  OUR APP TO FRONT IF USER IS LOOKING AT SOMETHING ELSE, JUST SHOW NOTIFICATION.
		getApplication().startActivity(dialogIntent);
	}

}