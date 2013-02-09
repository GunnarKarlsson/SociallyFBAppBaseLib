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

package com.bluebitapps.fbclientbase.notifications;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.albums.AlbumsActivity;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.events.EventActivity;
import com.bluebitapps.fbclientbase.events.EventProfileFragment;
import com.bluebitapps.fbclientbase.friendrequests.FriendRequestsActivity;
import com.bluebitapps.fbclientbase.groups.GroupActivity;
import com.bluebitapps.fbclientbase.page.PageActivity;
import com.bluebitapps.fbclientbase.photos.ImagePagerActivity;
import com.bluebitapps.fbclientbase.photos.Photo;
import com.bluebitapps.fbclientbase.profile.ProfileActivity;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;
import com.bluebitapps.utils.OutputUtil;

/*
 * Launched in singleTop launchMode. Updates received via onNewIntent().
 * 
 * */

public class NotificationsAlertActivity extends BaseThemedActivity {

	public static final String MARKED_AS_READ_IN_POP_UP = "marked as read in popup";

	private DataUpdateReceiver mDataUpdateReceiver;
	private ArrayList<FBNotification> mNotifications = new ArrayList<FBNotification>();
	private ArrayList<Integer> mPositionsWaitingToBeMarkedRead = new ArrayList<Integer>();
	private TextView mMessageTextView;
	private int mNotificationsIndex = 0;
	Button mMarkAsReadButton;
	Button mNextButton;
	Button mViewButton;

	private class DataUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (MarkAsReadService.MARK_AS_READ_SUCCESS.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + MarkAsReadService.MARK_AS_READ_SUCCESS);

				// broadcast to menu to decrement -1.
				Intent informSlidingMenuIntent = new Intent(NotificationsAlertActivity.MARKED_AS_READ_IN_POP_UP);
				sendBroadcast(informSlidingMenuIntent);

				if (mPositionsWaitingToBeMarkedRead.size() > 0) {
					if (mNotifications.size() > 0) {
						int pos = mPositionsWaitingToBeMarkedRead.get(0);
						mNotifications.remove(pos);
						mPositionsWaitingToBeMarkedRead.remove(0);
						mNotificationsIndex++;
						if (mNotificationsIndex >= (mNotifications.size())) {
							mNotificationsIndex = 0;
						}
						displayNotification();
					}
					if (mMarkAsReadButton != null) {

						mMarkAsReadButton.setText("Mark as read");
						mMarkAsReadButton.setEnabled(true);
						mNextButton.setEnabled(true);
					}
				}
			}

			if (MarkAsReadService.MARK_AS_READ_FAIL.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + MarkAsReadService.MARK_AS_READ_FAIL);
				mMarkAsReadButton.setText("Mark as read");
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.NotificationAlertStyle);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notifications_alert_activity);
		ImageView iv = (ImageView) findViewById(R.id.imageView);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String themeSelection = prefs.getString(Constants.THEME_PREFERENCES_KEY, Constants.THEME_DEFAULT);
		ThemeFactory.setThemeAsViewBackground(iv, themeSelection);

		mViewButton = (Button) findViewById(R.id.viewBtn);
		mViewButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				markAsRead();
				displaySection();
			}
		});

		Button closeBtn = (Button) findViewById(R.id.closeBtn);
		closeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				NotificationsAlertActivity.this.finish();
			}
		});

		mNextButton = (Button) findViewById(R.id.nextBtn);

		mNextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mMessageTextView != null && mNotifications != null && mNotifications.size() > 1) {
					mNotificationsIndex++;
					if (mNotificationsIndex >= (mNotifications.size())) {
						mNotificationsIndex = 0;
					}
					mMessageTextView.setText(mNotifications.get(mNotificationsIndex).getTitleText());
					mMessageTextView.invalidate();
				}

			}
		});

		mMarkAsReadButton = (Button) findViewById(R.id.markAsReadBtn);

		mMarkAsReadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mMarkAsReadButton.setText("wait a sec...");
				mMarkAsReadButton.setEnabled(false);
				mNextButton.setEnabled(false);
				mPositionsWaitingToBeMarkedRead.add(mNotificationsIndex);
				markAsRead();

			}
		});

		mMessageTextView = (TextView) findViewById(R.id.message);
	}

	private void markAsRead() {
		ArrayList<String> ids = new ArrayList<String>();
		String id = "";
		try {
			id = mNotifications.get(mNotificationsIndex).getId();
		} catch (IndexOutOfBoundsException e) {
			return;
		}

		ids.add(id);
		Intent intent = new Intent(this, MarkAsReadService.class);
		intent.putStringArrayListExtra(Constants.OBJECT_ID_KEY, ids);
		startService(intent);
	}

	private void displaySection() {

		FBNotification notification = mNotifications.get(mNotificationsIndex);
		String objectId = notification.getObjectId();
		String senderId = notification.getSenderId();
		String type = notification.getObjectType();
		String senderName = notification.getSenderName();

		Logger.i(Logger.getClassAndMethod() + "type: " + type);

		if ("page".equalsIgnoreCase(type)) {
			Intent intent = new Intent(this, PageActivity.class);
			intent.putExtra(Constants.OBJECT_ID_KEY, objectId);
			intent.putExtra(Constants.OBJECT_TITLE_KEY, senderName);
			intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
			intent.putExtra(Constants.TAB_INDEX_KEY, Constants.TAB_INDEX_WALL);
			startActivity(intent);
			this.finish();
			// overridePendingTransition(R.anim.slide_in_right,
			// R.anim.slide_out_left);
		} else if ("photo".equals(type)) {
			ArrayList<Photo> photos = new ArrayList<Photo>();
			Photo photo = new Photo();
			photo.setId(objectId);
			photos.add(photo);

			Intent intent = new Intent(this, ImagePagerActivity.class);
			intent.putParcelableArrayListExtra("photos", photos);
			intent.putExtra(Constants.PHOTO_ACCESS_VIA_NOTIFICATION, Constants.TRUE);
			int pos = 0;
			intent.putExtra("position", pos);
			intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
			startActivity(intent);
			this.finish();
			// overridePendingTransition(R.anim.slide_in_right,
			// R.anim.slide_out_left);

		} else if ("stream".equalsIgnoreCase(type) || "friend".equalsIgnoreCase(type)) {
			Intent intent = new Intent(this, ProfileActivity.class);
			intent.putExtra(Constants.OBJECT_ID_KEY, senderId);
			intent.putExtra(Constants.OBJECT_TITLE_KEY, senderName);
			intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
			intent.putExtra(Constants.TAB_INDEX_KEY, Constants.TAB_INDEX_WALL);
			startActivity(intent);
			this.finish();
			// overridePendingTransition(R.anim.slide_in_right,
			// R.anim.slide_out_left);

		} else if ("group".equalsIgnoreCase(type)) {
			Intent intent = new Intent(this, GroupActivity.class);
			intent.putExtra(Constants.OBJECT_ID_KEY, objectId);
			intent.putExtra(Constants.OBJECT_TITLE_KEY, senderName);
			intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
			intent.putExtra(Constants.TAB_INDEX_KEY, Constants.TAB_INDEX_WALL);
			startActivity(intent);
			this.finish();
			// overridePendingTransition(R.anim.slide_in_right,
			// R.anim.slide_out_left);

		} else if ("event".equalsIgnoreCase(type)) {
			Intent intent = new Intent(this, EventActivity.class);
			Log.i("jan16", "senderId: " + senderId);
			intent.putExtra(Constants.OBJECT_ID_KEY, objectId);
			intent.putExtra(Constants.OBJECT_TITLE_KEY, "Event");
			intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
			intent.putExtra(EventActivity.IS_INVITATION, true);
			intent.putExtra(Constants.TAB_INDEX_KEY, 0);
			startActivity(intent);
			this.finish();
			// overridePendingTransition(R.anim.slide_in_right,
			// R.anim.slide_out_left);

		} else if ("albums".equalsIgnoreCase(type)) {

			Intent intent = new Intent(this, AlbumsActivity.class);
			intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
			startActivity(intent);
			this.finish();
			// overridePendingTransition(R.anim.slide_in_right,
			// R.anim.slide_out_left);

		} else if ("friend_request".equalsIgnoreCase(type)) {

			Intent intent = new Intent(this, FriendRequestsActivity.class);
			intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
			startActivity(intent);
			this.finish();// overridePendingTransition(R.anim.slide_in_right,
							// R.anim.slide_out_left);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mDataUpdateReceiver == null) {
			mDataUpdateReceiver = new DataUpdateReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(NotificationsService.REFRESH_NOTIFICATIONS_DATA_SUCCESS);
			intentFilter.addAction(NotificationsService.REFRESH_NOTIFICATIONS_FAIL);
			intentFilter.addAction(MarkAsReadService.MARK_AS_READ_SUCCESS);
			intentFilter.addAction(MarkAsReadService.MARK_AS_READ_FAIL);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			registerReceiver(mDataUpdateReceiver, intentFilter);
		}
		getNotifications();

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mDataUpdateReceiver != null) {
			unregisterReceiver(mDataUpdateReceiver);
			// An activity can be resumed after pausing, so the activity may not
			// register the listener in the second call to onResume.
			mDataUpdateReceiver = null;
		}
	}

	private void getNotifications() {
		Logger.i(Logger.getClassAndMethod());

		ArrayList<FBNotification> notifications = new ArrayList<FBNotification>();

		FBClientApplication app = (FBClientApplication) getApplication();

		Cursor c = app.getNotificationsData().getUnreadNotifications();

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					String id = c.getString(c.getColumnIndex(NotificationsData.C_ID));
					String senderId = c.getString(c.getColumnIndex(NotificationsData.C_SENDER_ID));
					String senderName = c.getString(c.getColumnIndex(NotificationsData.C_SENDER_NAME));
					String createdTime = c.getString(c.getColumnIndex(NotificationsData.C_CREATED_TIME));
					String titleText = c.getString(c.getColumnIndex(NotificationsData.C_TITLE_TEXT));
					String bodyText = c.getString(c.getColumnIndex(NotificationsData.C_BODY_TEXT));
					String appId = c.getString(c.getColumnIndex(NotificationsData.C_APP_ID));
					String appName = c.getString(c.getColumnIndex(NotificationsData.C_APP_NAME));
					String isUnread = c.getString(c.getColumnIndex(NotificationsData.C_IS_UNREAD));
					String isHidden = c.getString(c.getColumnIndex(NotificationsData.C_IS_HIDDEN));
					String objectId = c.getString(c.getColumnIndex(NotificationsData.C_OBJECT_ID));
					String objectType = c.getString(c.getColumnIndex(NotificationsData.C_OBJECT_TYPE));

					FBNotification notification = new FBNotification();
					notification.setId(id);
					Log.i("notif", "new notification from db table: " + notification.getId());
					notification.setSenderId(senderId);
					notification.setSenderName(senderName);
					notification.setCreatedTime(createdTime);
					notification.setTitleText(titleText);
					notification.setBodyText(bodyText);
					notification.setAppId(appId);
					notification.setAppName(appName);
					notification.setIsUnread(isUnread);
					notification.setIsHidden(isHidden);
					notification.setObjectId(objectId);
					notification.setObjectType(objectType);

					notifications.add(notification);

				} while (c.moveToNext());
			}
		}
		if (c != null) {
			c.close();
		}

		mNotifications.clear();
		mNotifications = notifications;

		displayNotification();
	}

	private void displayNotification() {
		if (mNotifications.size() > 0) {
			mMessageTextView.setText(mNotifications.get(0).getTitleText());
		} else {
			mMessageTextView.setText("All notifications marked as read");
			mViewButton.setEnabled(false);
		}

		if (mNotifications.size() < 2) {
			mNextButton.setEnabled(false);
		}
	}
}