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

package com.bluebitapps.fbclientbase.checkins;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.photos.UploadPhotoActivity;
import com.bluebitapps.fbclientbase.photos.UploadPhotoService;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

public class PostCheckinActivity extends BaseThemedActivity {

	public static final String MESSAGE_KEY = "message_key";
	public static final String PLACE_NAME_KEY = "name_key";
	public static final String PLACE_ID_KEY = "id_key";
	public static final String PLACE_LOCATION_KEY = "location_key";
	public static final String LATITUDE_KEY = "latitude_key";
	public static final String LONGITUDE_KEY = "longitude_key";
	public static final String PRIVACY_SETTING_KEY = "privacy setting key";

	private String mPlaceName;
	private String mPlaceId;
	private String mPlaceLocation;
	private String mLatitude;
	private String mLongitude;
	
	private EditText mEditText;
	private String mMessage;

	private Spinner mPrivacySpinner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_checkin);
		mEditText = (EditText)findViewById(R.id.message);
		
		View view = findViewById(R.id.root);
		setThemeAndConfigureActionBar(view);
		
		Bundle bundle = getIntent().getExtras();
		
		mPlaceName = bundle.getString(PLACE_NAME_KEY);
		mPlaceId = bundle.getString(PLACE_ID_KEY);
		mPlaceLocation = bundle.getString(PLACE_LOCATION_KEY);
		mLatitude = bundle.getString(LATITUDE_KEY);
		mLongitude = bundle.getString(LONGITUDE_KEY);

		mPrivacySpinner = (Spinner) findViewById(R.id.privacySpinner);
		TextView heading = (TextView)findViewById(R.id.heading);
		String str = getResources().getString(R.string.checkin) + ": " + mPlaceName;
		heading.setText(str);
		
		mEditText = (EditText) findViewById(R.id.message);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(R.string.post).setTitle(R.string.post).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}

		
		if (item.getTitle().toString().equalsIgnoreCase(getResources().getString(R.string.post))) {
			
			String selection = mPrivacySpinner.getSelectedItem().toString();
			String privacySetting = "";

			if (getResources().getString(R.string.everyone).equalsIgnoreCase(selection)) {
				privacySetting = "EVERYONE";
			} else if (getResources().getString(R.string.friends_of_friends).equalsIgnoreCase(selection)) {
				privacySetting = "FRIENDS_OF_FRIENDS";
			} else if (getResources().getString(R.string.myself).equalsIgnoreCase(selection)) {
				privacySetting = "SELF";
			} else {
				privacySetting = "ALL_FRIENDS";
			}

			OutputUtil.showCrouton(this, getResources().getString(R.string.youll_receive_a_notification_when_your_checkin_has_been_posted));
			OutputUtil.showCrouton(this, getResources().getString(R.string.returning_to_previous_screen));
			
			Intent intent = new Intent(this, PostCheckinService.class);
			intent.putExtra(MESSAGE_KEY, mEditText.getText().toString());
			intent.putExtra(PLACE_NAME_KEY, mPlaceName);
			intent.putExtra(PLACE_ID_KEY, mPlaceId);
			intent.putExtra(mPlaceLocation, mPlaceLocation);
			intent.putExtra(LATITUDE_KEY, mLatitude);
			intent.putExtra(LONGITUDE_KEY, mLongitude);
			intent.putExtra(PRIVACY_SETTING_KEY, privacySetting);
			startService(intent);

			Timer timer = new Timer();
			FinishActivityTask task = new FinishActivityTask();
			timer.schedule(task, 4000);
			
			return true;
		} else {
			return false;
		}
	}
	
	private void hideKeyBoard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
	}
	
	class FinishActivityTask extends TimerTask {

		@Override
		public void run() {
			hideKeyBoard();
			PostCheckinActivity.this.finish();
		}

	}
}