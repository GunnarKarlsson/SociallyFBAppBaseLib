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

package com.bluebitapps.fbclientbase.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;
import com.bluebitapps.utils.FacebookUtils;
import com.bluebitapps.utils.OutputUtil;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;

//TODO: BUG - Keeps recreating new fragments in infinite loop

public class EventProfileFragment extends BaseNavigationFragment {

	private Event mEvent;
	private ImageView mEventImage;
	private String mName;
	private TextView mNameTextView;
	private TextView mStartTextView;
	private TextView mEndTextView;
	private TextView mStatusTextView;
	private Button mJoinBtn;
	private Button mMaybeBtn;
	private Button mDeclineBtn;
	private String mRequestType;
	private ViewGroup mButtons;
	private boolean isInvitation;
	private EventDataReceiver mReceiver;
	private boolean isFirstDataRequest;

	private class EventDataReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (EventInvitationResponseService.RESPONSE_POST_SUCCESS.equals(intent.getAction())) {
				stopRefreshMenuItemAnimation();
				getProfile();
			}

			if (EventInvitationResponseService.RESPONSE_POST_FAIL.equals(intent.getAction())) {
				stopRefreshMenuItemAnimation();
				enableAllButtons();
				OutputUtil.showCrouton(getActivity(), "Response couldn't be sent");
			}
		}
	}

	public static final EventProfileFragment newInstance(String objectId, boolean refreshOnlyMenuFlag, boolean isInvitation) {
		EventProfileFragment f = new EventProfileFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.OBJECT_ID_KEY, objectId);
		bundle.putBoolean(BaseNavigationFragment.FLAG_HAS_ONLY_REFRESH_MENU_ITEM_IN_ACTIONBAR, refreshOnlyMenuFlag);
		bundle.putBoolean(EventActivity.IS_INVITATION, isInvitation);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onResume() {
		super.onResume();
		Crouton.cancelAllCroutons();
		configText(mNameTextView);
		configText(mStartTextView);
		configText(mEndTextView);

		if (mReceiver == null) {
			mReceiver = new EventDataReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(EventInvitationResponseService.RESPONSE_POST_SUCCESS);
			intentFilter.addAction(EventInvitationResponseService.RESPONSE_POST_FAIL);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			if (getActivity() != null) {
				getActivity().registerReceiver(mReceiver, intentFilter);
			}
		}
		getProfile();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mReceiver != null && getActivity() != null) {
			getActivity().unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setHasOptionsMenu(true);
		prepareRefreshMenuItemAnimation();
		// startRefreshMenuItemAnimation();

		Bundle bundle = getArguments();

		setObjectId(bundle.getString(Constants.OBJECT_ID_KEY));
		isInvitation = bundle.getBoolean(EventActivity.IS_INVITATION, false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Log.i("jan30", Logger.getClassAndMethod());

		ViewGroup vg = null;

		vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.event_row_invitation);

		mEventImage = (ImageView) vg.findViewById(R.id.eventPicture);

		mNameTextView = (TextView) vg.findViewById(R.id.eventName);
		configText(mNameTextView);

		mStartTextView = (TextView) vg.findViewById(R.id.eventStartTime);
		configText(mStartTextView);

		mEndTextView = (TextView) vg.findViewById(R.id.eventEndTime);
		configText(mEndTextView);

		mStatusTextView = (TextView) vg.findViewById(R.id.status);
		//configText(mStatusTextView);

		mButtons = (ViewGroup) vg.findViewById(R.id.buttons);

		mJoinBtn = (Button) vg.findViewById(R.id.joinBtn);
		// mJoinBtn.setEnabled(true);

		mMaybeBtn = (Button) vg.findViewById(R.id.maybeBtn);
		// mMaybeBtn.setEnabled(true);

		mDeclineBtn = (Button) vg.findViewById(R.id.declineBtn);
		// mDeclineBtn.setEnabled(true);

		// if (isInvitation) {

		mJoinBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("jan30", Logger.getClassAndMethod());
				startRefreshMenuItemAnimation();
				disableAllButtons();
				mRequestType = Constants.EVENT_INVITATION_RESPONSE_JOIN;
				respondToEvent(Constants.EVENT_INVITATION_RESPONSE_JOIN);
			}
		});

		mMaybeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startRefreshMenuItemAnimation();
				disableAllButtons();
				mRequestType = Constants.EVENT_INVITATION_RESPONSE_MAYBE;
				respondToEvent(Constants.EVENT_INVITATION_RESPONSE_MAYBE);
			}
		});

		mDeclineBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startRefreshMenuItemAnimation();
				disableAllButtons();
				mRequestType = Constants.EVENT_INVITATION_RESPONSE_DECLINE;
				respondToEvent(Constants.EVENT_INVITATION_RESPONSE_DECLINE);
			}
		});
		// }

		// if (!isInvitation) {
		//mButtons.setVisibility(View.GONE);
		//mStatusTextView.setText("attending");
		// }
		return vg;
	}

	private void disableAllButtons() {

		Log.i("jan30", Logger.getClassAndMethod());
		mJoinBtn.setEnabled(false);
		int color = 0x888888;
		mJoinBtn.setTextColor(color);
		mMaybeBtn.setEnabled(false);
		mMaybeBtn.setTextColor(color);
		mDeclineBtn.setEnabled(false);
		mDeclineBtn.setTextColor(color);

	}

	private void enableAllButtons() {
		int color = 0x222222;
		mJoinBtn.setEnabled(true);
		mJoinBtn.setTextColor(color);
		mMaybeBtn.setEnabled(true);
		mMaybeBtn.setTextColor(color);
		mDeclineBtn.setEnabled(true);
		mDeclineBtn.setTextColor(color);
	}
/*
	private void setStatus() {

		if (StringUtil.notEmpty(mRequestType)) {

			mButtons.setVisibility(View.GONE);

			String status = "";
			if (Constants.EVENT_INVITATION_RESPONSE_DECLINE.equals(mRequestType)) {
				status = "Declined";
			} else if (Constants.EVENT_INVITATION_RESPONSE_JOIN.equals(mRequestType)) {
				status = "Joined";
			} else if (Constants.EVENT_INVITATION_RESPONSE_MAYBE.equals(mRequestType)) {
				status = "Maybe will join";
			}
			mStatusTextView.setVisibility(View.VISIBLE);
			mStatusTextView.setText(status);
		} else {
			// enableAllButtons();
			OutputUtil.showCrouton(getActivity(), "Response could not be processed");
		}
	}
	*/

	private void respondToEvent(String action) {
		Log.i("jan30", Logger.getClassAndMethod());
		mJoinBtn.setEnabled(false);
		mMaybeBtn.setEnabled(false);
		mDeclineBtn.setEnabled(false);

		Intent intent = new Intent(getActivity(), EventInvitationResponseService.class);
		intent.putExtra(Constants.OBJECT_ID_KEY, getObjectId());
		intent.putExtra(Constants.ACTION_TYPE, action);
		getActivity().startService(intent);
	}

	private void getProfile() {

		getProfileFromDatabase();

		setLayout();

	}

	private void getProfileFromDatabase() {
		if (getActivity() == null) {
			return;
		}
		FBClientApplication app = (FBClientApplication) getActivity().getApplication();
		EventsData data = app.getEventsData();
		Log.i("jan29", Logger.getClassAndMethod() + "obj id: " + getObjectId());
		Cursor c = data.getEventById(getObjectId());
		Event event = new Event();

		if (c != null) {
			if (c.moveToFirst()) {
				do {

					event.setId(c.getString(c.getColumnIndex(EventsData.C_ID)));
					event.setName(c.getString(c.getColumnIndex(EventsData.C_NAME)));
					event.setStartTime(c.getString(c.getColumnIndex(EventsData.C_START_TIME)));
					event.setEndTime(c.getString(c.getColumnIndex(EventsData.C_END_TIME)));
					event.setRsvpStatus(c.getString(c.getColumnIndex(EventsData.C_RSVP_STATUS)));

				} while (c.moveToNext());
			}
		}

		mEvent = event;
		Log.i("jan30", "event details:");
		Log.i("jan30", "event id:" + event.getId());
		Log.i("jan30", "event name: " + event.getName());
		Log.i("jan30", "mEvent.getName(): " + mEvent.getName());
		Log.i("jan30", "event.getRsvpStatus(): " + event.getRsvpStatus());
		if (c != null) {
			c.close();
		}

		setLayout();

	}

	private void setLayout() {
		if (getActivity() == null) {
			return;
		}
		Log.i("jan30", Logger.getClassAndMethod());

		mNameTextView.setText(mEvent.getName());
		// mNameTextView.invalidate();

		if (mEvent.getStartTime() != null) {

			if (mEvent.getStartTime().contains(":")) {

				String startTime = (String) FacebookUtils.convertFacebookEventTimeToRelativeTime(mEvent.getStartTime());
				mStartTextView.setText(startTime);

				String endTime = (String) FacebookUtils.convertFacebookEventTimeToRelativeTime(mEvent.getEndTime());
				mEndTextView.setText(endTime);
			} else {
				String startTime = (String) FacebookUtils.convertInvitedToEventTimeStamp(mEvent.getStartTime());
				mStartTextView.setText(startTime);
				String endTime = (String) FacebookUtils.convertInvitedToEventTimeStamp(mEvent.getEndTime());
				mEndTextView.setText(endTime);
			}
		}

		if ("attending".equals(mEvent.getRsvpStatus())) {
			Log.i("jan30", "pos A");
			mButtons.setVisibility(View.GONE);
			String str = "You've joined this event";
			mStatusTextView.setVisibility(View.VISIBLE);
			mStatusTextView.setText(str);
		}else if("maybe".equals(mEvent.getRsvpStatus())){
			mButtons.setVisibility(View.GONE);
			mStatusTextView.setText("Maybe you'll join");
		}else if("declined".equals(mEvent.getRsvpStatus())){
			mButtons.setVisibility(View.GONE);
			mStatusTextView.setText("You've declined this event");
		} else if ("not_replied".equals(mEvent.getRsvpStatus())) {
			Log.i("jan30", "pos B");
			mButtons.setVisibility(View.VISIBLE);
			mStatusTextView.setVisibility(View.GONE);
		}

		String token = getApplication().getFBConnection().getFacebook().getAccessToken();

		String query = "https://graph.facebook.com/" + mEvent.getId() + "/picture?access_token=" + token;

		getImageLoader().displayImage(query, mEventImage, getImageDisplayOptions());

		stopRefreshMenuItemAnimation();

	}

	@Override
	protected void onRefresh() {
		super.onRefresh();
		startRefreshMenuItemAnimation();
		getProfileFromDatabase();
	}

}