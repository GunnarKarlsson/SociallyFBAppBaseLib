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

package com.bluebitapps.fbclientbase.subscriptions;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.ViewGroup;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.albums.AlbumsFragment;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;
import com.bluebitapps.fbclientbase.base.BaseSlidingMenuActivity.CustomTabListener;
import com.bluebitapps.fbclientbase.newsfeed.NewsFeedFragment;
import com.bluebitapps.fbclientbase.profile.ProfileFragment;

public class SubscriptionActivity extends BaseThemedActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabbed_activity);

		Bundle bundle = getIntent().getExtras();
		String userId = bundle.getString(Constants.OBJECT_ID_KEY);
		String title = bundle.getString(Constants.OBJECT_TITLE_KEY);
		Boolean clearTop = bundle.getBoolean(Constants.CLEAR_TOP_ON_HOME_SELECTED);

		ViewGroup rootView = (ViewGroup) findViewById(R.id.rootView);

		setThemeAndConfigureActionBar(rootView);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setTitle("Your Profile");// TODO change to any users name

		ProfileFragment profileFragment = ProfileFragment.newInstance(userId, Constants.STATE_PROFILE, title, true);
		profileFragment.setRetainInstance(true);
		AlbumsFragment albumsFragment = AlbumsFragment.newInstance(userId, Constants.STATE_PROFILE, title, true);
		albumsFragment.setRetainInstance(true);
		NewsFeedFragment newsFeedFragment = NewsFeedFragment.newInstance(userId, Constants.STATE_PROFILE, title, true);
		newsFeedFragment.setRetainInstance(true);

		ActionBar.Tab tab = actionBar.newTab().setText("Profile").setTabListener(new CustomTabListener<ProfileFragment>(profileFragment));
		actionBar.addTab(tab);
		tab = actionBar.newTab().setText("Wall").setTabListener(new CustomTabListener<NewsFeedFragment>(newsFeedFragment));
		actionBar.addTab(tab);
		tab = actionBar.newTab().setText("Photos").setTabListener(new CustomTabListener<AlbumsFragment>(albumsFragment));
		actionBar.addTab(tab);
	}
}
