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

import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.albums.AlbumsFragment;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;
import com.bluebitapps.fbclientbase.base.BaseSlidingMenuActivity.CustomTabListener;
import com.bluebitapps.fbclientbase.checkins.PlaceMapFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.newsfeed.NewsFeedFragment;
import com.bluebitapps.fbclientbase.profile.ProfileFragment;

public class PlaceProfileActivity extends BaseThemedActivity{
	
	private ActionBar mActionBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabbed_activity);
		
		Bundle bundle = getIntent().getExtras();
		String placeId = bundle.getString(Constants.OBJECT_ID_KEY);
		String placeName = bundle.getString(Constants.OBJECT_TITLE_KEY);
		String tabIndex = bundle.getString(Constants.TAB_INDEX_KEY);
		
		Log.i("jan9", Logger.getClassAndMethod() + "object id " + placeId);
		
		ViewGroup rootView = (ViewGroup)findViewById(R.id.rootView);

		setThemeAndConfigureActionBar(rootView);
		
		mActionBar = getActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		PlaceProfileFragment placeProfileFragment = PlaceProfileFragment.newInstance(placeId, placeName, true);
		placeProfileFragment.setRetainInstance(true);

		AlbumsFragment albumsFragment = AlbumsFragment.newInstance(placeId, Constants.STATE_PROFILE, placeName,true);
		albumsFragment.setRetainInstance(true);

		PlaceMapFragment mapFragment = PlaceMapFragment.newInstance(placeId, placeName);
		mapFragment.setRetainInstance(true);

		NewsFeedFragment newsFeedFragment = NewsFeedFragment.newInstance(placeId, Constants.STATE_PROFILE, placeName,true);
		newsFeedFragment.setRetainInstance(true);

		ActionBar.Tab tab = mActionBar.newTab().setText("Profile").setTabListener(new CustomTabListener<ProfileFragment>(placeProfileFragment));
		mActionBar.addTab(tab);

		tab = mActionBar.newTab().setText("Wall").setTabListener(new CustomTabListener<NewsFeedFragment>(newsFeedFragment));
		mActionBar.addTab(tab);

		tab = mActionBar.newTab().setText("Photos").setTabListener(new CustomTabListener<AlbumsFragment>(albumsFragment));
		mActionBar.addTab(tab);

		tab = mActionBar.newTab().setText("Map").setTabListener(new CustomTabListener<PlaceMapFragment>(mapFragment));
		mActionBar.addTab(tab);

		if (Constants.TAB_INDEX_WALL.equals(tabIndex)) {
			mActionBar.setSelectedNavigationItem(1);
		}		

	}

}