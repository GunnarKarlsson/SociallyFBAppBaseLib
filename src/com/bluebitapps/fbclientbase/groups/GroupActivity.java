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

package com.bluebitapps.fbclientbase.groups;

import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;
import com.bluebitapps.fbclientbase.base.BaseSlidingMenuActivity.CustomTabListener;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.newsfeed.NewsFeedFragment;
import com.bluebitapps.fbclientbase.profile.ProfileFragment;

public class GroupActivity extends BaseThemedActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabbed_activity);
		
		Bundle bundle = getIntent().getExtras();
		String groupId = bundle.getString(Constants.OBJECT_ID_KEY);
		String title = bundle.getString(Constants.OBJECT_TITLE_KEY);
		String tabIndex = bundle.getString(Constants.TAB_INDEX_KEY);
		
		ViewGroup rootView = (ViewGroup)findViewById(R.id.rootView);

		setThemeAndConfigureActionBar(rootView);
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		Log.i("jan16", Logger.getClassAndMethod() + "groupId: " + groupId);
		
		GroupProfileFragment profileFragment = GroupProfileFragment.newInstance(groupId, Constants.STATE_PROFILE,title,true);
		profileFragment.setRetainInstance(true);
		NewsFeedFragment wallFragment = NewsFeedFragment.newInstance(groupId, Constants.STATE_PROFILE,title,true);
		wallFragment.setRetainInstance(true);
		GroupMembersFragment membersFragment = GroupMembersFragment.newInstance(groupId,title,true);
		membersFragment.setRetainInstance(true);

		ActionBar.Tab tab = actionBar.newTab().setText(R.string.info).setTabListener(new CustomTabListener<ProfileFragment>(profileFragment));
		actionBar.addTab(tab);
		tab = actionBar.newTab().setText(R.string.wall).setTabListener(new CustomTabListener<NewsFeedFragment>(wallFragment));
		actionBar.addTab(tab);
		tab = actionBar.newTab().setText(R.string.members).setTabListener(new CustomTabListener<GroupMembersFragment>(membersFragment));
		actionBar.addTab(tab);

		if (Constants.TAB_INDEX_WALL.equals(tabIndex)) {
			actionBar.setSelectedNavigationItem(1);
		}		

	}
}
