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

import android.os.Bundle;

import com.bluebitapps.fbclientbase.base.BaseThemedActivity;

public class TextSettingsActivity extends BaseThemedActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		
		TextSettingsFragment fragment = new TextSettingsFragment();
		android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.replace(android.R.id.content, fragment).commit();
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		/*
		if(getActionBar()!=null){
			getActionBar().setDisplayShowTitleEnabled(true);
			getActionBar().setTitle("TextSettings");
		}
		*/
	}

}
