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

package com.bluebitapps.fbclientbase.statusupdate;

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
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;

import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;
import com.bluebitapps.fbclientbase.debug.Logger;

public class PostStatusUpdateActivity extends BaseThemedActivity {

	private EditText mMessageEditText;
	private EditText mUrlEditText;
	private EditText mUrlNameEditText;
	private ViewGroup mRoot;
	private Spinner mPrivacySpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Logger.i(Logger.getClassAndMethod());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_status_update);
		mMessageEditText = (EditText) findViewById(R.id.message);
		mUrlEditText = (EditText) findViewById(R.id.link);
		mUrlNameEditText = (EditText) findViewById(R.id.linkName);
		mPrivacySpinner = (Spinner) findViewById(R.id.privacySpinner);
		mRoot = (ViewGroup) findViewById(R.id.root);
		View view = findViewById(R.id.root);
		setThemeAndConfigureActionBar(view);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		mMessageEditText.clearFocus();
		mRoot.requestFocus();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(getResources().getString(R.string.post)).setTitle(R.string.post).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return true;//
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		Logger.i(Logger.getClassAndMethod());

		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}

		if (item.getTitle().toString().equalsIgnoreCase(getResources().getString(R.string.post))) {
			Logger.i(PostStatusUpdateActivity.class.getSimpleName() + "onOptionsItemSelected" + "Post");

			if (!StringUtil.notEmpty(mMessageEditText.getText().toString())) {
				OutputUtil.showCrouton(this, getResources().getString(R.string.enter_a_message));
				return false;
			}

			// Crouton.makeText(this, "Posting your status update",
			// Style.INFO).show();
			OutputUtil.showCrouton(this, getResources().getString(R.string.youll_receive_a_notification_when_your_message_has_been_posted));
			OutputUtil.showCrouton(this, getResources().getString(R.string.returning_to_previous_screen));

			String selection = mPrivacySpinner.getSelectedItem().toString();
			String privacySetting = "";

			Log.i("feb6", Logger.getClassAndMethod() + selection);
			
			if (getResources().getString(R.string.everyone).equalsIgnoreCase(selection)) {
				privacySetting = "EVERYONE";
			} else if (getResources().getString(R.string.friends_of_friends).equalsIgnoreCase(selection)) {
				privacySetting = "FRIENDS_OF_FRIENDS";
			} else if (getResources().getString(R.string.myself).equalsIgnoreCase(selection)) {
				privacySetting = "SELF";
			} else {
				privacySetting = "ALL_FRIENDS";
			}
			
			Log.i("feb" , Logger.getClassAndMethod() + privacySetting);

			Intent intent = new Intent(this, PostStatusUpdateService.class);
			Logger.i(Logger.getClassAndMethod() + "mEditText.getText().toString(): " + mMessageEditText.getText().toString());
			intent.putExtra(PostStatusUpdateService.MESSAGE_KEY, mMessageEditText.getText().toString());
			intent.putExtra(PostStatusUpdateService.LINK_KEY, mUrlEditText.getText().toString());
			intent.putExtra(PostStatusUpdateService.LINK_NAME_KEY, mUrlNameEditText.getText().toString());
			intent.putExtra(PostStatusUpdateService.PRIVACY_KEY, privacySetting);
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
			PostStatusUpdateActivity.this.finish();
		}
	}
}