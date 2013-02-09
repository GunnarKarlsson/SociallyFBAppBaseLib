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

package com.bluebitapps.fbclientbase.page;

import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.albums.Album;
import com.bluebitapps.fbclientbase.albums.AlbumsFragment;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;
import com.bluebitapps.fbclientbase.base.BaseSlidingMenuActivity.CustomTabListener;
import com.bluebitapps.fbclientbase.newsfeed.NewsFeedFragment;

public class PageActivity extends BaseThemedActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.standard_activity);
		ViewGroup rootView = (ViewGroup) findViewById(R.id.rootView);
		setThemeAndConfigureActionBar(rootView);

		Bundle extras = getIntent().getExtras();
		String objectId = extras.getString(Constants.OBJECT_ID_KEY);
		String title = extras.getString(Constants.OBJECT_TITLE_KEY);
		String tabIndex = extras.getString(Constants.TAB_INDEX_KEY);

		ActionBar actionBar = getActionBar();

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		PageFragment pageFragment = PageFragment.newInstance(objectId, title);
		pageFragment.setRetainInstance(true);
		AlbumsFragment albumsFragment = AlbumsFragment.newInstance(objectId, Constants.STATE_PROFILE, title, true);
		albumsFragment.setRetainInstance(true);
		NewsFeedFragment newsFeedFragment = NewsFeedFragment.newInstance(objectId, Constants.STATE_PROFILE, title, true);
		newsFeedFragment.setRetainInstance(true);

		ActionBar.Tab tab = actionBar.newTab().setText("Profile").setTabListener(new CustomTabListener<PageFragment>(pageFragment));
		actionBar.addTab(tab);
		tab = actionBar.newTab().setText("Wall").setTabListener(new CustomTabListener<NewsFeedFragment>(newsFeedFragment));
		actionBar.addTab(tab);
		tab = actionBar.newTab().setText("Photos").setTabListener(new CustomTabListener<AlbumsFragment>(albumsFragment));
		actionBar.addTab(tab);

		if (Constants.TAB_INDEX_WALL.equals(tabIndex)) {
			actionBar.setSelectedNavigationItem(1);
		}
	}
}