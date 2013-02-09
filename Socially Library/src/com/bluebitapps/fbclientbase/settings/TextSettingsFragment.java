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

package com.bluebitapps.fbclientbase.settings;

import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseSlidingMenuActivity;
import com.bluebitapps.fbclientbase.debug.Logger;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class TextSettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//setHasOptionsMenu(true);

		// Load the preferences from an XML resource
		addPreferencesFromResource(com.bluebitapps.fbclientbase.R.xml.text_preferences);
	}
/*
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.newsfeedmenu, menu);
	}
	*/

	@Override
	public void onResume() {
		Log.i("jan9", Logger.getClassAndMethod());
		super.onResume();
		if (getActivity() != null) {
			Log.i("jan9", Logger.getClassAndMethod() + "getActivity() != null");
			
			
			if(getActivity() instanceof BaseSlidingMenuActivity){				
				BaseSlidingMenuActivity activity = (BaseSlidingMenuActivity)getActivity();
				activity.setIsShowingTextSettings(true);
			}
			
			if (getActivity().getActionBar() != null) {
				getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
				getActivity().getActionBar().setDisplayShowTitleEnabled(true);
				getActivity().getActionBar().setTitle("TextSettings");
				getActivity().getActionBar().setSubtitle(null);
			}
		}
	}

	/*
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Logger.i(Logger.getClassAndMethod());
		//super.onOptionsItemSelected(item);

		// Can't handle this in the activity since then onOptionsItemSelected in
		// the fragment doesn't get called.
		if (item.getItemId() == android.R.id.home && getActivity() != null) {

			getActivity().getFragmentManager().beginTransaction().remove(this).commit();
			// getActivity().overridePendingTransition(R.anim.slide_in_left,
			// R.anim.slide_out_right);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	*/

}