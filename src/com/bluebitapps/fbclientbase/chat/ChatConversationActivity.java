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

package com.bluebitapps.fbclientbase.chat;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;
import com.bluebitapps.fbclientbase.debug.Logger;

public class ChatConversationActivity extends BaseThemedActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.tabbed_activity);
		
		ViewGroup rootView = (ViewGroup) findViewById(R.id.rootView);
		setThemeAndConfigureActionBar(rootView);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		
		Bundle extras = getIntent().getExtras();
		String chatUserJabberId = extras.getString(Constants.CHAT_USER_JABBER_ID_KEY);
		String chatUserName = extras.getString(Constants.CHAT_USER_NAME_KEY);
		
		ChatConversationFragment fragment = new ChatConversationFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.CHAT_USER_JABBER_ID_KEY, chatUserJabberId);
		bundle.putString(Constants.CHAT_USER_NAME_KEY, chatUserName);
		bundle.putBoolean(BaseNavigationFragment.FLAG_HAS_ONLY_REFRESH_MENU_ITEM_IN_ACTIONBAR, true);
		fragment.setArguments(bundle);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.replace(android.R.id.content, fragment).commit();
	}
}