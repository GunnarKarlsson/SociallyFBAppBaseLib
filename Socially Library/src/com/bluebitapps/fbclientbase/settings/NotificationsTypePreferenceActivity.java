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

import com.bluebitapps.fbclientbase.MainActivity;
import com.bluebitapps.fbclientbase.R;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

public class NotificationsTypePreferenceActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   
	   
	   getActionBar().setDisplayHomeAsUpEnabled(true);
	   getActionBar().setDisplayShowTitleEnabled(true);
	   getActionBar().setTitle("Notification Type Settings");
	   
	   addPreferencesFromResource(com.bluebitapps.fbclientbase.R.xml.notification_type_preference);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (keyCode == KeyEvent.KEYCODE_BACK) {
				this.finish();
			    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
				finish();
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
