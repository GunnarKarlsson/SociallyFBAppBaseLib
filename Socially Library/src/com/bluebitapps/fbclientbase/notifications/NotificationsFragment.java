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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.albums.AlbumsActivity;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.events.EventActivity;
import com.bluebitapps.fbclientbase.friendrequests.FriendRequestsActivity;
import com.bluebitapps.fbclientbase.groups.GroupActivity;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.page.PageActivity;
import com.bluebitapps.fbclientbase.photos.ImagePagerActivity;
import com.bluebitapps.fbclientbase.photos.Photo;
import com.bluebitapps.fbclientbase.profile.ProfileActivity;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;
import com.bluebitapps.utils.FacebookUtils;
import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;

public class NotificationsFragment extends BaseNavigationFragment {

	public static final String FLAG_STARTED_FROM_NOTIFICATION_TAP = "flag started from notification tap";
	public static final String FLAG_KEY = "flag key";

	private DataUpdateReceiver mDataUpdateReceiver;
	private boolean isStartedFromNotificationTab;
	private boolean isFirstDataRequest;
	private ArrayList<FBNotification> mNotifications;
	private ListView mListView;
	private ItemAdapter mAdapter;
	private LoadingView mLoadingView;

	private class DataUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (NotificationsService.REFRESH_NOTIFICATIONS_DATA_SUCCESS.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + NotificationsService.REFRESH_NOTIFICATIONS_DATA_SUCCESS);
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}
				stopRefreshMenuItemAnimation();
				getNotifications();
			}

			if (NotificationsService.REFRESH_NOTIFICATIONS_FAIL.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + NotificationsService.REFRESH_NOTIFICATIONS_FAIL);
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}
				stopRefreshMenuItemAnimation();
				// TODO: should we show message?
				// OutputUtil.showCrouton(getActivity(),
				// "New notifications could not be fetched");
			}

			if (MarkAsReadService.MARK_AS_READ_SUCCESS.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + MarkAsReadService.MARK_AS_READ_SUCCESS);
				isFirstDataRequest = false;
				stopRefreshMenuItemAnimation();
				getNotifications();
			}

			if (MarkAsReadService.MARK_AS_READ_FAIL.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + MarkAsReadService.MARK_AS_READ_FAIL);
				stopRefreshMenuItemAnimation();
				OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.one_or_more_notifications_couldnt_be_marked_as_read));
			}
		}
	}

	public static final NotificationsFragment newInstance(String flag) {
		NotificationsFragment f = new NotificationsFragment();
		Bundle bundle = new Bundle();
		bundle.putString(NotificationsFragment.FLAG_KEY, flag);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setHasOptionsMenu(true);
		isFirstDataRequest = true;
		prepareRefreshMenuItemAnimation();

		mNotifications = new ArrayList<FBNotification>();

		Log.i("jan23", Logger.getClassAndMethod());

		Bundle bundle = getArguments();

		if (bundle.containsKey(NotificationsFragment.FLAG_KEY)) {
			String flag = bundle.getString(NotificationsFragment.FLAG_KEY);

			if (NotificationsFragment.FLAG_STARTED_FROM_NOTIFICATION_TAP.equals(flag)) {
				isStartedFromNotificationTab = true;
			}
		}

		setTitle(getResources().getString(R.string.notifications_menu_item));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.notifications_list);

		mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);
		mListView = (ListView) vg.findViewById(R.id.notificationsListView);

		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		mListView.setMultiChoiceModeListener(new ModeCallback());

		mAdapter = new ItemAdapter();
		mListView = (ListView) vg.findViewById(R.id.notificationsListView);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				FBNotification notification = mNotifications.get(position);
				String objectId = notification.getObjectId();
				String senderId = notification.getSenderId();
				String type = notification.getObjectType();
				String notificationId = notification.getId();
				String senderName = notification.getSenderName();

				Logger.i(Logger.getClassAndMethod() + "type: " + type);

				// TODO handle tab selection
				if ("page".equalsIgnoreCase(type)) {
					// OK

					markAsReadWhenClicking(notificationId);

					Intent intent = new Intent(getActivity(), PageActivity.class);
					intent.putExtra(Constants.OBJECT_ID_KEY, objectId);
					intent.putExtra(Constants.OBJECT_TITLE_KEY, senderName);
					intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
					intent.putExtra(Constants.TAB_INDEX_KEY, Constants.TAB_INDEX_WALL);
					getActivity().startActivity(intent);
					getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				} else if ("photo".equals(type)) {

					markAsReadWhenClicking(notificationId);
					// OK
					ArrayList<Photo> photos = new ArrayList<Photo>();
					Photo photo = new Photo();
					photo.setId(objectId);
					Log.i("feb19", Logger.getClassAndMethod() + " objectId: " + objectId);
					photos.add(photo);

					Intent intent = new Intent(getActivity(), ImagePagerActivity.class);
					intent.putParcelableArrayListExtra("photos", photos);
					intent.putExtra(Constants.PHOTO_ACCESS_VIA_NOTIFICATION, Constants.TRUE);
					int pos = 0;
					intent.putExtra("position", pos);
					intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
					getActivity().startActivity(intent);
					getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

				} else if ("stream".equalsIgnoreCase(type) || "friend".equalsIgnoreCase(type)) {

					markAsReadWhenClicking(notificationId);
					// OK
					Intent intent = new Intent(getActivity(), ProfileActivity.class);
					intent.putExtra(Constants.OBJECT_ID_KEY, senderId);
					intent.putExtra(Constants.OBJECT_TITLE_KEY, senderName);
					intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
					intent.putExtra(Constants.TAB_INDEX_KEY, Constants.TAB_INDEX_WALL);
					getActivity().startActivity(intent);
					getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

				} else if ("group".equalsIgnoreCase(type)) {

					markAsReadWhenClicking(notificationId);
					// OK
					Intent intent = new Intent(getActivity(), GroupActivity.class);
					intent.putExtra(Constants.OBJECT_ID_KEY, objectId);
					intent.putExtra(Constants.OBJECT_TITLE_KEY, senderName);
					intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
					intent.putExtra(Constants.TAB_INDEX_KEY, Constants.TAB_INDEX_WALL);
					getActivity().startActivity(intent);
					getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

				} else if ("event".equalsIgnoreCase(type)) {

					markAsReadWhenClicking(notificationId);
					// OK
					Intent intent = new Intent(getActivity(), EventActivity.class);
					Log.i("jan16", "senderId: " + senderId);
					intent.putExtra(Constants.OBJECT_ID_KEY, objectId);
					intent.putExtra(Constants.OBJECT_TITLE_KEY, "Event");
					intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
					intent.putExtra(Constants.TAB_INDEX_KEY, 0);
					intent.putExtra(EventActivity.IS_INVITATION, true);
					getActivity().startActivity(intent);
					getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

				} else if ("albums".equalsIgnoreCase(type)) {

					markAsReadWhenClicking(notificationId);
					// OK
					Intent intent = new Intent(getActivity(), AlbumsActivity.class);
					intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
					getActivity().startActivity(intent);
					getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

				} else if ("friend_request".equalsIgnoreCase(type)) {

					markAsReadWhenClicking(notificationId);
					// OK
					Intent intent = new Intent(getActivity(), FriendRequestsActivity.class);
					intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
					getActivity().startActivity(intent);
					getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				}
			}
		});
		return vg;
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
			getActivity().registerReceiver(mDataUpdateReceiver, intentFilter);
		}
		startRefreshMenuItemAnimation();
		getNotifications();

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mDataUpdateReceiver != null) {
			getActivity().unregisterReceiver(mDataUpdateReceiver);
			// An activity can be resumed after pausing, so the activity may not
			// register the listener in the second call to onResume.
			mDataUpdateReceiver = null;
		}
	}

	@Override
	public void onRefresh() {
		startRefreshMenuItemAnimation();
		Logger.i(Logger.getClassAndMethod());
		getNotificationsFromFB();
	}

	private void getNotifications() {
		startRefreshMenuItemAnimation();

		getNotificationsFromDatabase();
		// getActivity().invalidateOptionsMenu();

		if (isFirstDataRequest == true) {

			if (mNotifications.size() > 0) {
				mLoadingView.setVisibility(View.GONE);
			}

			if (isStartedFromNotificationTab) {
				isStartedFromNotificationTab = false;
				stopRefreshMenuItemAnimation();
			} else {
				getNotificationsFromFB();
			}

		} else {
			mLoadingView.setVisibility(View.GONE);

			if (mNotifications.size() < 1) {
				OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.no_new_notifications));
			}
		}

		if (getActivity() != null) {
			Intent intent = new Intent(Constants.ACTION_NEW_NOTIFICATIONS);
			intent.putExtra(Constants.NOTIFICATION_COUNT_KEY, mNotifications.size());
			getActivity().sendBroadcast(intent);
		}

	}

	private void getNotificationsFromDatabase() {
		Logger.i(Logger.getClassAndMethod());

		if (getActivity() == null) {
			return;
		}

		ArrayList<FBNotification> notifications = new ArrayList<FBNotification>();

		FBClientApplication app = (FBClientApplication) getActivity().getApplication();

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

		Logger.i(Logger.getClassAndMethod() + mNotifications.size());
		if (getActivity() != null) {
			// Notify SlidingMenu
			Intent intent = new Intent(Constants.ACTION_NEW_NOTIFICATIONS);
			intent.putExtra(Constants.NOTIFICATION_COUNT_KEY, mNotifications.size());
			getActivity().sendBroadcast(intent);

			// Write subtitle
			String notificationString = getResources().getString(R.string.notification_lowercase);
			String notificationsString = getResources().getString(R.string.notifications_lowercase);
			String notificationWord = mNotifications.size() == 1 ? notificationString : notificationsString;
			String str = mNotifications.size() + " " + notificationWord;
			getActivity().getActionBar().setSubtitle(str);
		}

		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}

	}

	private void getNotificationsFromFB() {
		if (getActivity() == null) {
			return;
		}

		Intent intent = new Intent(getActivity(), NotificationsService.class);
		intent.putExtra(NotificationsService.FLAG_IS_REQUEST_FROM_FRAGMENT, true);
		getActivity().startService(intent);
	}

	class ItemAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView fromName;
			public ImageView fromPicture;
			public TextView createdTime;
			public TextView title;
			public TextView body;
		}

		@Override
		public int getCount() {
			if (mNotifications != null) {
				return mNotifications.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			View view = convertView;
			final ViewHolder holder;

			if (getActivity() != null) {

				FBNotification notification = mNotifications.get(position);
				if (convertView == null) {
					view = getActivity().getLayoutInflater().inflate(R.layout.notification_item, null);

					holder = new ViewHolder();
					holder.title = (TextView) view.findViewById(R.id.title);
					configText(holder.title);
					holder.fromName = (TextView) view.findViewById(R.id.fromName);
					configFromText(holder.fromName);
					holder.fromPicture = (ImageView) view.findViewById(R.id.fromPicture);
					holder.createdTime = (TextView) view.findViewById(R.id.createdTime);
					configTimeText(holder.createdTime);
					holder.body = (TextView) view.findViewById(R.id.body);
					configBodyText(holder.body);

					view.setTag(holder);
				} else
					holder = (ViewHolder) view.getTag();

				holder.fromName.setText(notification.getSenderName());
				holder.createdTime.setText(FacebookUtils.convertUnixTimeStampToRelativeTime(mNotifications.get(position).getCreatedTime(), getActivity()));
				holder.title.setText(notification.getTitleText());
				getImageLoader().displayImage(notification.getProfilePicture(), holder.fromPicture, getImageDisplayOptions());

				if (StringUtil.notEmpty(notification.getBodyText())) {
					holder.body.setVisibility(View.VISIBLE);
					holder.body.setText(notification.getBodyText());
				} else {
					holder.body.setVisibility(View.GONE);
				}
			}

			return view;
		}
	}

	private class ModeCallback implements ListView.MultiChoiceModeListener {

		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.list_select_mark_as_read, menu);
			mode.setTitle("Select Items");
			setSubtitle(mode);
			return true;
		}

		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return true;
		}

		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

			startRefreshMenuItemAnimation();

			if (item.getItemId() == R.id.markAsRead) {
				SparseBooleanArray selectedPos = mListView.getCheckedItemPositions();

				ArrayList<String> ids = new ArrayList<String>();

				for (int i = 0; i < mNotifications.size(); i++) {
					int pos = i;
					if (selectedPos.get(pos)) {
						Log.i("jan23", Logger.getClassAndMethod() + "sender " + mNotifications.get(pos).getSenderName());
						Log.i("jan23", Logger.getClassAndMethod() + "title text: " + mNotifications.get(pos).getTitleText());
						ids.add(mNotifications.get(pos).getId());
					}
				}

				Intent intent = new Intent(NotificationsFragment.this.getActivity(), MarkAsReadService.class);
				intent.putStringArrayListExtra(Constants.OBJECT_ID_KEY, ids);
				NotificationsFragment.this.getActivity().startService(intent);

				mode.finish();
				return true;
			} else {
				return false;
			}
		}

		public void onDestroyActionMode(ActionMode mode) {
		}

		public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
			setSubtitle(mode);

			// get notification_ids.

		}

		private void setSubtitle(ActionMode mode) {
			final int checkedCount = mListView.getCheckedItemCount();
			switch (checkedCount) {
			case 0:
				mode.setSubtitle(null);
				break;
			case 1:
				mode.setSubtitle(R.string.one_item_selected);
				break;
			default:
				String subTitle = checkedCount + " " + getResources().getString(R.string.items_selected);
				mode.setSubtitle(subTitle);
				break;
			}
		}
	}

	private void markAsReadWhenClicking(final String id) {
		ArrayList<String> ids = new ArrayList<String>();
		ids.add(id);
		Intent intent = new Intent(NotificationsFragment.this.getActivity(), MarkAsReadService.class);
		intent.putStringArrayListExtra(Constants.OBJECT_ID_KEY, ids);
		NotificationsFragment.this.getActivity().startService(intent);
	}
}