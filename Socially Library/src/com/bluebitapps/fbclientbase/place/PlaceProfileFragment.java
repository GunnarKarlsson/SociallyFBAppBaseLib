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

package com.bluebitapps.fbclientbase.place;

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
import com.bluebitapps.fbclientbase.base.BaseFragment;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.page.Page;
import com.bluebitapps.fbclientbase.page.PageData;
import com.bluebitapps.fbclientbase.profile.ProfileData;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

public class PlaceProfileFragment extends BaseNavigationFragment {

	private PlaceProfileUpdateReceiver mDataUpdateReceiver;
	private LoadingView mLoadingView;
	private ViewGroup mRootView;
	private Page mPlace;
	private ImageView mCoverPhoto;
	private ImageView mProfilePhoto;
	private boolean isFirstDataRequest;

	private class PlaceProfileUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			Logger.i(Logger.getClassAndMethod());

			if (intent.getAction().equals(PlaceService.REFRESH_PLACE_DATA_SUCCESS)) {
				Logger.i(Logger.getClassAndMethod() + PlaceService.REFRESH_PLACE_DATA_SUCCESS);
				getProfile();
			}

			if (PlaceService.REFRESH_PLACE_DATA_FAIL.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + PlaceService.REFRESH_PLACE_DATA_FAIL);
				OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.data_could_not_be_refreshed));
			}
		}
	}

	public static final PlaceProfileFragment newInstance(String objectId, String title, boolean refreshOnlyMenuFlag) {
		PlaceProfileFragment f = new PlaceProfileFragment();
		Bundle bundle = new Bundle();
		Logger.i(Logger.getClassAndMethod() + " objectId:c" + objectId);
		bundle.putString(Constants.OBJECT_ID_KEY, objectId);
		bundle.putString(Constants.OBJECT_TITLE_KEY, title);
		bundle.putBoolean(FLAG_HAS_ONLY_REFRESH_MENU_ITEM_IN_ACTIONBAR, refreshOnlyMenuFlag);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onResume() {
		super.onResume();
		Crouton.cancelAllCroutons();
		if (mDataUpdateReceiver == null) {
			mDataUpdateReceiver = new PlaceProfileUpdateReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(PlaceService.REFRESH_PLACE_DATA_SUCCESS);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			if (getActivity() != null) {
				getActivity().registerReceiver(mDataUpdateReceiver, intentFilter);
			}
		}
		getProfile();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mDataUpdateReceiver != null && getActivity() != null) {
			getActivity().unregisterReceiver(mDataUpdateReceiver);
			mDataUpdateReceiver = null;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		isFirstDataRequest = true;
		Bundle bundle = getArguments();

		if (bundle != null) {
			setObjectId(bundle.getString(Constants.OBJECT_ID_KEY));
			if (bundle.getString(Constants.OBJECT_TITLE_KEY) != null) {
				setTitle(bundle.getString(Constants.OBJECT_TITLE_KEY));
			} else {
				setTitle(getResources().getString(R.string.place_profile));
			}
		}

		Log.i("jan9", "objectId: " + getObjectId());
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Logger.i(Logger.getClassAndMethod());

		ViewGroup vg = null;

		if (getActivity() != null) {

			vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.fragment_place_profile);
			mRootView = (ViewGroup) vg.findViewById(R.id.contentRoot);
			mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);
			mProfilePhoto = (ImageView) vg.findViewById(R.id.profilePhoto);
			mRootView.setVisibility(View.GONE);
			mLoadingView.setVisibility(View.VISIBLE);
		}

		return vg;
	}

	public void refresh() {

		getProfile();
	}

	private void getProfile() {

		if (getActivity() == null) {
			return;
		}

		FBClientApplication app = (FBClientApplication) getActivity().getApplication();
		PageData data = app.getPageData();
		Cursor c = data.getPageById(getObjectId());
		Logger.i(Logger.getClassAndMethod() + "getObjectId: " + getObjectId());
		Page place = new Page();

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					place.setId(c.getString(c.getColumnIndex(PageData.C_ID)));
					place.setName(c.getString(c.getColumnIndex(PageData.C_NAME)));
					place.setCategory(c.getString(c.getColumnIndex(PageData.C_CATEGORY)));
					place.setDescription(c.getString(c.getColumnIndex(PageData.C_DESCRIPTION)));
					place.setCity(c.getString(c.getColumnIndex(PageData.C_CITY)));
					place.setCountry(c.getString(c.getColumnIndex(PageData.C_COUNTRY)));
					place.setLatitude(c.getString(c.getColumnIndex(PageData.C_LATITUDE)));
					place.setLongitude(c.getString(c.getColumnIndex(PageData.C_LONGITUDE)));
					place.setProfilePic(c.getString(c.getColumnIndex(PageData.C_PROFILE_PIC)));
					place.setFanCount(c.getString(c.getColumnIndex(PageData.C_FAN_COUNT)));
					place.setTalkingAbout(c.getString(c.getColumnIndex(PageData.C_TALKING_ABOUT)));
					place.setCoverPhoto(c.getString(c.getColumnIndex(ProfileData.C_COVER_PHOTO)));

				} while (c.moveToNext());
			}
		}

		mPlace = place;
		if (c != null) {
			c.close();
		}

		if (StringUtil.notEmpty(place.getId())) {
			setLayout();
		} else {
			if (isFirstDataRequest == true) {
				Intent intent = new Intent(getActivity(), PlaceService.class);
				intent.putExtra(Constants.OBJECT_ID_KEY, getObjectId());
				Logger.i(Logger.getClassAndMethod() + " getObjectId(): " + getObjectId());
				if (getActivity() != null) {
					getActivity().startService(intent);
				}
			} else {
				OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.data_not_available));
			}
		}
	}

	private void setLayout() {

		if (StringUtil.notEmpty(mPlace.getName())) {
			TextView name = (TextView) mRootView.findViewById(R.id.nameValue);
			name.setText(mPlace.getName());
			configText(name);
		} else {
			((TextView) mRootView.findViewById(R.id.nameValue)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mPlace.getDescription())) {
			TextView description = (TextView) mRootView.findViewById(R.id.descriptionValue);
			description.setText(mPlace.getDescription());
			configText(description);
		} else {
			((TextView) mRootView.findViewById(R.id.descriptionValue)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mPlace.getCity())) {
			((TextView) mRootView.findViewById(R.id.cityValue)).setText(mPlace.getCity());
		} else {
			((TableRow) mRootView.findViewById(R.id.city)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mPlace.getCountry())) {
			((TextView) mRootView.findViewById(R.id.countryValue)).setText(mPlace.getCountry());
		} else {
			((TableRow) mRootView.findViewById(R.id.country)).setVisibility(View.GONE);
		}

		String token = getApplication().getFBConnection().getFacebook().getAccessToken();
		String query = "https://graph.facebook.com/" + mPlace.getId() + "/picture?width=600&height=600&access_token=" + token;
		getImageLoader().displayImage(query, mProfilePhoto, getImageDisplayOptions());

		if (StringUtil.notEmpty(mPlace.getFanCount())) {
			((TextView) mRootView.findViewById(R.id.likesCountValue)).setText(mPlace.getFanCount());
		} else {
			((TableRow) mRootView.findViewById(R.id.likesCount)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mPlace.getTalkingAbout())) {
			((TextView) mRootView.findViewById(R.id.talkingAboutValue)).setText(mPlace.getTalkingAbout());
		} else {
			((TableRow) mRootView.findViewById(R.id.talkingAbout)).setVisibility(View.GONE);
		}

		mLoadingView.setVisibility(View.GONE);
		mRootView.setVisibility(View.VISIBLE);
	}
}