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

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.bluebitapps.utils.StringUtil;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;
import com.bluebitapps.fbclientbase.base.BaseSlidingMenuActivity.CustomTabListener;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.newsfeed.NewsFeedFragment;
import com.bluebitapps.fbclientbase.profile.ProfileFragment;

public class EventActivity extends BaseThemedActivity {

	public final static String IS_INVITATION = "is invitation";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.tabbed_activity);

		Bundle bundle = getIntent().getExtras();
		String objectId = bundle.getString(Constants.OBJECT_ID_KEY);
		String title = bundle.getString(Constants.OBJECT_TITLE_KEY);
		String tabIndex = bundle.getString(Constants.TAB_INDEX_KEY);
		Boolean isInvitation = bundle.getBoolean(EventActivity.IS_INVITATION);

		setTitle(title);

		ViewGroup rootView = (ViewGroup) findViewById(R.id.rootView);

		setThemeAndConfigureActionBar(rootView);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		EventProfileFragment profileFragment = EventProfileFragment.newInstance(objectId, true, isInvitation);
		profileFragment.setRetainInstance(true);

		EventUserListFragment attendingFragment = EventUserListFragment.newInstance(Constants.STATE_EVENT_ATTENDING, objectId, title, true);
		attendingFragment.setRetainInstance(true);

		EventUserListFragment declinedFragment = EventUserListFragment.newInstance(Constants.STATE_EVENT_DECLINED, objectId, title, true);
		declinedFragment.setRetainInstance(true);
		EventUserListFragment invitedFragment = EventUserListFragment.newInstance(Constants.STATE_EVENT_INVITED, objectId, title, true);
		invitedFragment.setRetainInstance(true);
		EventUserListFragment maybeFragment = EventUserListFragment.newInstance(Constants.STATE_EVENT_MAYBE, objectId, title, true);
		maybeFragment.setRetainInstance(true);

		ActionBar.Tab tab = actionBar.newTab().setText(R.string.event_details).setTabListener(new CustomTabListener<Fragment>(profileFragment));
		actionBar.addTab(tab);
		tab = actionBar.newTab().setText(R.string.invited).setTabListener(new CustomTabListener<Fragment>(invitedFragment));
		actionBar.addTab(tab);
		tab = actionBar.newTab().setText(R.string.attending).setTabListener(new CustomTabListener<Fragment>(attendingFragment));
		actionBar.addTab(tab);
		tab = actionBar.newTab().setText(R.string.declined).setTabListener(new CustomTabListener<Fragment>(declinedFragment));
		actionBar.addTab(tab);
		tab = actionBar.newTab().setText(R.string.maybe).setTabListener(new CustomTabListener<Fragment>(maybeFragment));
		actionBar.addTab(tab);

		if (Constants.TAB_INDEX_WALL.equals(tabIndex)) {
			actionBar.setSelectedNavigationItem(1);
		}
		actionBar.setSelectedNavigationItem(0);

	}
}