package com.bluebitapps.fbclientbase.groups;

/* Copyright 2012 Gunnar Karlsson.
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

public class GroupProfileFragment extends BaseNavigationFragment {

	private LoadingView mLoadingView;
	private ViewGroup mRootView;
	private Group mGroup;
	private ImageView mProfilePhoto;
	private String mName;

	public static final GroupProfileFragment newInstance(String objectId, String state, String title, boolean refreshOnlyMenuFlag) {
		GroupProfileFragment f = new GroupProfileFragment();
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
		getProfile();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setHasOptionsMenu(true);
		prepareRefreshMenuItemAnimation();
		
		Bundle bundle = getArguments();

		setObjectId(bundle.getString(Constants.OBJECT_ID_KEY));
		setState(getArguments().getString(Constants.STATE_KEY));
		mName = bundle.getString(Constants.OBJECT_TITLE_KEY);
		
		if(StringUtil.notEmpty(bundle.getString(Constants.OBJECT_TITLE_KEY))){
			setTitle(bundle.getString(Constants.OBJECT_TITLE_KEY));
		}else{
			if(getActivity()!=null){
				String title = getActivity().getResources().getString(R.string.profile);
				setTitle(title);
			}
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Logger.i(Logger.getClassAndMethod());

		ViewGroup vg = null;
		if(getActivity()!=null){
			
		vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.group_fragment_profile);
		mRootView = (ViewGroup) vg.findViewById(R.id.contentRoot);
		mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);
		mProfilePhoto = (ImageView) vg.findViewById(R.id.profilePhoto);
		mRootView.setVisibility(View.GONE);
		mLoadingView.setVisibility(View.VISIBLE);

		}
		return vg;
	}

	@Override
	public void onRefresh() {
		Logger.i(Logger.getClassAndMethod());
	}

	private void getProfile() {
		startRefreshMenuItemAnimation();
		getProfileFromDatabase();
		setLayout();
	}

	private void getProfileFromDatabase() {
		if(getActivity()==null){
			return;
		}
		FBClientApplication app = (FBClientApplication) getActivity().getApplication();
		GroupsData data = app.getGroupsData();
		Cursor c = data.getGroupById(getObjectId());
		Group group = new Group();

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					group.setId(c.getString(c.getColumnIndex(GroupsData.C_ID)));
					group.setName(c.getString(c.getColumnIndex(GroupsData.C_NAME)));
					group.setDescription(c.getString(c.getColumnIndex(GroupsData.C_DESCRIPTION)));
				} while (c.moveToNext());
			}
		}

		mGroup = group;
		if (c != null) {
			c.close();
		}

	}

	private void setLayout() {
		Logger.i(Logger.getClassAndMethod());

		if (StringUtil.notEmpty(mName)) {
			TextView name = (TextView)mRootView.findViewById(R.id.nameValue);
			name.setText(mName);
			configFromText(name);
		} else {
			((TableRow) mRootView.findViewById(R.id.name)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mGroup.getDescription())) {
			((TextView) mRootView.findViewById(R.id.descriptionValue)).setText(mGroup.getDescription());
		} else {
			((TableRow) mRootView.findViewById(R.id.description)).setVisibility(View.GONE);
		}

		String token = getApplication().getFBConnection().getFacebook().getAccessToken();
		String query = "https://graph.facebook.com/" + mGroup.getId() + "/picture?width=600&height=600&access_token=" + token;
		getImageLoader().displayImage(query, mProfilePhoto, getImageDisplayOptions());

		mLoadingView.setVisibility(View.GONE);
		mRootView.setVisibility(View.VISIBLE);
		stopRefreshMenuItemAnimation();
	}

}