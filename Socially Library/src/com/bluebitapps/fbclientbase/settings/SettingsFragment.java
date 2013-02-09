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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.utils.OutputUtil;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(com.bluebitapps.fbclientbase.R.xml.preferences);

		findPreference("foo_bar_pref");
		Preference pref = (Preference) findPreference("notiftype");
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (getActivity() != null) {
					startActivity(new Intent(getActivity(), NotificationsTypePreferenceActivity.class));
				}
				return false;
			}
		});

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResume() {
		Log.i("jan9", Logger.getClassAndMethod());
		super.onResume();
		
		OutputUtil.cancelAllCroutons(getActivity());
		
		if (getActivity() != null) {
			Log.i("jan9", Logger.getClassAndMethod() + "getActivity() != null");
			if (getActivity().getActionBar() != null) {
				//getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
				getActivity().getActionBar().setDisplayShowTitleEnabled(true);
				getActivity().getActionBar().setTitle("Settings");
				getActivity().getActionBar().setSubtitle(null);
			}
		}
	}

}
