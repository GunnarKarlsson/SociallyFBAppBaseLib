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

package com.bluebitapps.fbclientbase.profile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
import com.facebook.android.FacebookError;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.base.SectionManager;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;

//TODO: BUG - Keeps recreating new fragments in infinite loop

public class ProfileFragment extends BaseNavigationFragment {

	private ProfileUpdateReceiver mDataUpdateReceiver;
	private LoadingView mLoadingView;
	private ViewGroup mRootView;
	private Profile mProfile;
	private ImageView mCoverPhoto;
	private ImageView mProfilePhoto;
	private boolean isFirstDataRequest;
	private String mName;

	private class ProfileUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (ProfileService.REFRESH_PROFILE_DATA_SUCCESS.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod());
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();
				getProfile();
			}

			if (ProfileService.REFRESH_PROFILE_DATA_FAIL.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod());
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();
				OutputUtil.showCrouton(getActivity(), "New profile data could not be retrieved");
			}
		}
	}

	public static final ProfileFragment newInstance(String objectId, String state, String title, boolean refreshOnlyMenuFlag) {
		ProfileFragment f = new ProfileFragment();
		Bundle bundle = new Bundle();
		Logger.i(Logger.getClassAndMethod() + "objectId:" + objectId);
		bundle.putString(Constants.OBJECT_ID_KEY, objectId);
		bundle.putString(Constants.STATE_KEY, state);
		bundle.putString(Constants.OBJECT_TITLE_KEY, title);
		bundle.putBoolean(FLAG_HAS_ONLY_REFRESH_MENU_ITEM_IN_ACTIONBAR, refreshOnlyMenuFlag);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onResume() {
		super.onResume();
		Crouton.cancelAllCroutons();
		/*
		if(getActivity()!=null && StringUtil.notEmpty(mName)){
			getActivity().getActionBar().setSubtitle(mName);
		}
		*/
		
		if (mDataUpdateReceiver == null) {
			mDataUpdateReceiver = new ProfileUpdateReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(ProfileService.REFRESH_PROFILE_DATA_SUCCESS);
			intentFilter.addAction(ProfileService.REFRESH_PROFILE_DATA_FAIL);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			if(getActivity()!=null){				
				getActivity().registerReceiver(mDataUpdateReceiver, intentFilter);
			}
		}

		startRefreshMenuItemAnimation();
		getProfile();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mDataUpdateReceiver != null && getActivity()!=null) {
			getActivity().unregisterReceiver(mDataUpdateReceiver);
			// An activity can be resumed after pausing, so the activity may not
			// register the listener in the second call to onResume.
			mDataUpdateReceiver = null;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setHasOptionsMenu(true);
		isFirstDataRequest = true;
		prepareRefreshMenuItemAnimation();
		
		//FBClientApplication.getApplication().getBackStack().push(SectionManager.STATE_PROFILE);

		Bundle bundle = getArguments();

		setObjectId(bundle.getString(Constants.OBJECT_ID_KEY));
		setState(getArguments().getString(Constants.STATE_KEY));
		mName = bundle.getString(Constants.OBJECT_TITLE_KEY);
		
		if(StringUtil.notEmpty(bundle.getString(Constants.OBJECT_TITLE_KEY))){
			setTitle(bundle.getString(Constants.OBJECT_TITLE_KEY));
		}else{
			setTitle("Profile");
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Logger.i(Logger.getClassAndMethod());

		ViewGroup vg = null;
		if(getActivity()!=null){
			
		vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.fragment_profile);
		mRootView = (ViewGroup) vg.findViewById(R.id.contentRoot);
		mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);
		//mCoverPhoto = (ImageView) vg.findViewById(R.id.coverPhoto);
		mProfilePhoto = (ImageView) vg.findViewById(R.id.profilePhoto);
		mRootView.setVisibility(View.GONE);
		mLoadingView.setVisibility(View.VISIBLE);

		}
		return vg;
	}

	@Override
	public void onRefresh() {
		Logger.i(Logger.getClassAndMethod());
		getProfileFromFB();
	}

	private void getProfile() {

		getProfileFromDatabase();

		if (isFirstDataRequest) {

			if (mProfile != null && StringUtil.notEmpty(mProfile.getId())) {
				mLoadingView.setVisibility(View.GONE);
				setLayout();
			}

			//getActivity().invalidateOptionsMenu();
			getProfileFromFB();
			
		} else {
			
			mLoadingView.setVisibility(View.GONE);

			if (mProfile != null && StringUtil.notEmpty(mProfile.getId())) {
				setLayout();
			}else{
				OutputUtil.showCrouton(getActivity(), "Profile could not be retrieved");				
			}
			
			//getActivity().invalidateOptionsMenu();
		}
				
	}

	private void getProfileFromDatabase() {
		if(getActivity()==null){
			return;
		}
		FBClientApplication app = (FBClientApplication) getActivity().getApplication();
		ProfileData data = app.getProfileData();
		Cursor c = data.getProfileById(getObjectId());
		Profile profile = new Profile();

		if (c != null) {
			if (c.moveToFirst()) {
				do {

					profile.setId(c.getString(c.getColumnIndex(ProfileData.C_ID)));

					profile.setFirstName(c.getString(c.getColumnIndex(ProfileData.C_FIRST_NAME)));
					profile.setLastName(c.getString(c.getColumnIndex(ProfileData.C_LAST_NAME)));
					profile.setLink(c.getString(c.getColumnIndex(ProfileData.C_LINK)));
					profile.setName(c.getString(c.getColumnIndex(ProfileData.C_NAME)));
					profile.setUserName(c.getString(c.getColumnIndex(ProfileData.C_USER_NAME)));
					profile.setBirthday(c.getString(c.getColumnIndex(ProfileData.C_BIRTHDAY)));
					profile.setHomeTownId(c.getString(c.getColumnIndex(ProfileData.C_HOME_TOWN_ID)));
					profile.setHomeTownName(c.getString(c.getColumnIndex(ProfileData.C_HOME_TOWN_NAME)));
					profile.setHomeTownId(c.getString(c.getColumnIndex(ProfileData.C_HOME_TOWN_ID)));
					profile.setLocationId(c.getString(c.getColumnIndex(ProfileData.C_LOCATION_ID)));
					profile.setLocationName(c.getString(c.getColumnIndex(ProfileData.C_LOCATION_NAME)));
					profile.setGender(c.getString(c.getColumnIndex(ProfileData.C_GENDER)));
					profile.setOwnerName(c.getString(c.getColumnIndex(ProfileData.C_OWNER_NAME)));
					profile.setOwnerId(c.getString(c.getColumnIndex(ProfileData.C_OWNER_ID)));
					profile.setOwnerName(c.getString(c.getColumnIndex(ProfileData.C_OWNER_NAME)));
					profile.setOwnerCategory(c.getString(c.getColumnIndex(ProfileData.C_OWNER_CATEGORY)));
					profile.setDescription(c.getString(c.getColumnIndex(ProfileData.C_DESCRIPTION)));
					profile.setStartTime(c.getString(c.getColumnIndex(ProfileData.C_START_TIME)));
					profile.setEndTime(c.getString(c.getColumnIndex(ProfileData.C_END_TIME)));
					profile.setTimeZone(c.getString(c.getColumnIndex(ProfileData.C_TIME_ZONE)));
					profile.setIsDateOnly(c.getString(c.getColumnIndex(ProfileData.C_IS_DATE_ONLY)));
					profile.setLocation(c.getString(c.getColumnIndex(ProfileData.C_LOCATION)));
					profile.setVenueName(c.getString(c.getColumnIndex(ProfileData.C_VENUE_NAME)));
					profile.setPrivacy(c.getString(c.getColumnIndex(ProfileData.C_PRIVACY)));
					profile.setUpdatedTime(c.getString(c.getColumnIndex(ProfileData.C_UPDATED_TIME)));
					profile.setUpdatedTime(c.getString(c.getColumnIndex(ProfileData.C_UPDATED_TIME)));
					profile.setIcon(c.getString(c.getColumnIndex(ProfileData.C_ICON)));
					profile.setEmail(c.getString(c.getColumnIndex(ProfileData.C_EMAIL)));
					profile.setCreatedTime(c.getString(c.getColumnIndex(ProfileData.C_CREATED_TIME)));
					profile.setCategory(c.getString(c.getColumnIndex(ProfileData.C_CATEGORY)));
					profile.setCoverPhoto(c.getString(c.getColumnIndex(ProfileData.C_COVER_PHOTO)));

				} while (c.moveToNext());
			}
		}

		mProfile = profile;
		if (c != null) {
			c.close();
		}

	}

	private void getProfileFromFB() {
		Log.i("jan16", Logger.getClassAndMethod());
		Intent intent = new Intent(getActivity(), ProfileService.class);
		intent.putExtra(Constants.OBJECT_ID_KEY, getObjectId());
		Logger.i(Logger.getClassAndMethod() + " getObjectId(): " + getObjectId());
		if(getActivity()!=null){			
			getActivity().startService(intent);
		}

	}

	private void setLayout() {
		Logger.i(Logger.getClassAndMethod());

		if (StringUtil.notEmpty(mProfile.getFirstName())) {
			TextView firstName = (TextView)mRootView.findViewById(R.id.firstNameValue);
			firstName.setText(mProfile.getFirstName());
			configText(firstName);
		} else {
			((TableRow) mRootView.findViewById(R.id.firstName)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mProfile.getLastName())) {
			TextView lastName = (TextView)mRootView.findViewById(R.id.lastNameValue);
			lastName.setText(mProfile.getLastName());
			configFromText(lastName);
		} else {
			((TableRow) mRootView.findViewById(R.id.lastName)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mName)) {
			TextView name = (TextView)mRootView.findViewById(R.id.nameValue);
			name.setText(mName);
			configFromText(name);
		} else {
			((TableRow) mRootView.findViewById(R.id.name)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mProfile.getUserName())) {
			((TextView) mRootView.findViewById(R.id.userNameValue)).setText(mProfile.getUserName());
		} else {
			((TableRow) mRootView.findViewById(R.id.userName)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mProfile.getBirthday())) {
			((TextView) mRootView.findViewById(R.id.birthdayValue)).setText(mProfile.getBirthday());
		} else {
			((TableRow) mRootView.findViewById(R.id.birthday)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mProfile.getHomeTownName())) {
			((TextView) mRootView.findViewById(R.id.homeTownNameValue)).setText(mProfile.getHomeTownName());
		} else {
			((TableRow) mRootView.findViewById(R.id.homeTownName)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mProfile.getCoverPhoto())) {

			getImageLoader().displayImage(mProfile.getCoverPhoto(), mCoverPhoto, getImageDisplayOptions());
		}

		String token = getApplication().getFBConnection().getFacebook().getAccessToken();
		String query = "https://graph.facebook.com/" + mProfile.getId() + "/picture?width=600&height=600&access_token=" + token;
		getImageLoader().displayImage(query, mProfilePhoto, getImageDisplayOptions());

		if (StringUtil.notEmpty(mProfile.getRelationshipStatus())) {
			((TextView) mRootView.findViewById(R.id.relationshipstatusValue)).setText(mProfile.getRelationshipStatus());
		} else {
			((TableRow) mRootView.findViewById(R.id.relationshipStatus)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mProfile.getOwnerName())) {
			((TextView) mRootView.findViewById(R.id.ownerNameValue)).setText(mProfile.getOwnerName());
		} else {
			((TableRow) mRootView.findViewById(R.id.ownerName)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mProfile.getOwnerCategory())) {
			((TextView) mRootView.findViewById(R.id.ownerCategoryValue)).setText(mProfile.getOwnerCategory());
		} else {
			((TableRow) mRootView.findViewById(R.id.ownerCategory)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mProfile.getDescription())) {
			((TextView) mRootView.findViewById(R.id.descriptionValue)).setText(mProfile.getDescription());
		} else {
			((TableRow) mRootView.findViewById(R.id.description)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mProfile.getStartTime())) {
			((TextView) mRootView.findViewById(R.id.startTimeValue)).setText(mProfile.getStartTime());
		} else {
			((TableRow) mRootView.findViewById(R.id.startTime)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mProfile.getEndTime())) {
			((TextView) mRootView.findViewById(R.id.endTimeValue)).setText(mProfile.getEndTime());
		} else {
			((TableRow) mRootView.findViewById(R.id.endTime)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mProfile.getLocation())) {
			((TextView) mRootView.findViewById(R.id.locationValue)).setText(mProfile.getLocation());
		} else {
			((TableRow) mRootView.findViewById(R.id.location)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mProfile.getVenueName())) {
			((TextView) mRootView.findViewById(R.id.venueNameValue)).setText(mProfile.getVenueName());
		} else {
			((TableRow) mRootView.findViewById(R.id.venueName)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mProfile.getPrivacy())) {
			((TextView) mRootView.findViewById(R.id.privacyValue)).setText(mProfile.getPrivacy());
		} else {
			((TableRow) mRootView.findViewById(R.id.privacy)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mProfile.getUpdatedTime())) {
			((TextView) mRootView.findViewById(R.id.updatedTimeValue)).setText(mProfile.getUpdatedTime());
		} else {
			((TableRow) mRootView.findViewById(R.id.updatedTime)).setVisibility(View.GONE);
		}

		if ((StringUtil.notEmpty(mProfile.getIcon()))) {
			((TextView) mRootView.findViewById(R.id.iconValue)).setText(mProfile.getIcon());
		} else {
			((TableRow) mRootView.findViewById(R.id.icon)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mProfile.getEmail())) {
			((TextView) mRootView.findViewById(R.id.emailValue)).setText(mProfile.getEmail());
		} else {
			((TableRow) mRootView.findViewById(R.id.email)).setVisibility(View.GONE);
		}

		//mLoadingView.setVisibility(View.GONE);
		mRootView.setVisibility(View.VISIBLE);
	}

}
