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

package com.bluebitapps.fbclientbase.user;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;
import com.bluebitapps.fbclientbase.base.OnNewsFeedTappedListener;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.newsfeed.NewsFeedItem;

public class UserActivity extends BaseThemedActivity implements OnNewsFeedTappedListener{
	
	private String[] actions = new String[] { "Friend's Wall", "Friend's Info", "Friend's Albums", "Friend's Photos","Friend's Videos" };
	
	private String mUserId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.placeholder);
				
		//Get user id
		Bundle bundle = getIntent().getExtras();
		mUserId = bundle.getString(Constants.USER_ID_KEY);
		
		//Set theme.
		View view = findViewById(R.id.root);
		setThemeAndConfigureActionBar(view);
		
		//Add menu to ActionBar		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, actions);
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ActionBar.OnNavigationListener navigationListener = new ActionBar.OnNavigationListener() {

			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				//Toast.makeText(getActivity().getBaseContext(), "You selected : " + actions[itemPosition], Toast.LENGTH_SHORT).show();
				return true;
			}
		};

		// Set drop-down items and item navigation listener for the action bar.
		getActionBar().setListNavigationCallbacks(adapter, navigationListener);

		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		
		Logger.i(Logger.getClassAndMethod() + " mUserId: " + mUserId);
		
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
	}

	@Override
	public void onNewsFeedTapped(NewsFeedItem item) {
		
	}	
	
}