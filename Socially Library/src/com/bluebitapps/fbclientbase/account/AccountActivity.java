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

package com.bluebitapps.fbclientbase.account;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.MainActivity;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.debug.Logger;

/*
 * First account tries SSO, and if no offical FB app, does full Auth 2.0.
 * The first account is assigned attribute 'MainAccount'.
 * Subsequent accounts login with Auth 2.0, not SSO.
 * 
 * @author Gunnar Karlsson
 *
 */

public class AccountActivity extends Activity {

	private FBClientApplication mApplication;
	private List<Account> mAccounts;
	private DisplayImageOptions mOptions;
	private ListView mListView;
	private AccountAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.account_activity);

		getActionBar().hide();

		mApplication = (FBClientApplication) getApplication();

		mListView = (ListView) findViewById(R.id.accountListView);
		View accountAdder = (View) getLayoutInflater().inflate(R.layout.account_add_new, mListView, false);
		accountAdder.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doAuth();
			}
		});

		mListView.addFooterView(accountAdder);
		mAccounts = new ArrayList<Account>();
		mAdapter = new AccountAdapter();
		mListView.setAdapter(mAdapter);

		mOptions = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc().build();

		showList();
	}

	private void showList() {

		// read from db and add to list
		final List<Account> accounts = new ArrayList<Account>();

		Cursor c = mApplication.getAccountData().getAccounts();

		if (c.moveToFirst()) {

			mAccounts.clear();

			do {
				Account account = new Account();
				account.set(c);
				accounts.add(account);

			} while (c.moveToNext());
		} else {
			return;
		}

		c.close();

		AccountActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mAccounts = accounts;
				mAdapter.notifyDataSetChanged();

			}
		});

	}

	private void doAuth() {
		Logger.i(AccountActivity.class.getSimpleName() + "#doAuth");
		// if the account database is empty, set up a new account with SSO
		String[] permissions = mApplication.getFBConnection().getPermissions();

		if (mApplication.getFBConnection().getAccountCount() < 1) {
			Logger.i(AccountActivity.class.getSimpleName() + "#doAuth: first account");
			mApplication.getFBConnection().getFacebook().authorize(this, permissions, new AuthListener());
		} else {
			Logger.i(AccountActivity.class.getSimpleName() + "#doAuth: not first account");
			mApplication.getFBConnection().getFacebook().authorize(this, permissions, Facebook.FORCE_DIALOG_AUTH, new AuthListener());
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		Logger.i(AccountActivity.class.getSimpleName() + "#onActivityResult()");
		mApplication.getFBConnection().getFacebook().authorizeCallback(requestCode, resultCode, data);
		Long expires = mApplication.getFBConnection().getFacebook().getAccessExpires();
		String accessToken = mApplication.getFBConnection().getFacebook().getAccessToken();
		Logger.i("expires: " + expires.toString() + ", access token: " + accessToken.toString());
	}

	private class AuthListener implements DialogListener {

		@Override
		public void onComplete(Bundle values) {
			Logger.i(AccountActivity.class.getSimpleName() + "#LoginDialogListener" + "#onComplete");

			mApplication.getFBConnection().getAsyncFacebookRunner().request("me", new UserProfileRequestListener());

		}

		@Override
		public void onFacebookError(FacebookError e) {
			Logger.i(AccountActivity.class.getSimpleName() + "#LoginDialogListener" + "#onFacebookError " + e.toString());
		}

		@Override
		public void onError(DialogError e) {
			Logger.i(AccountActivity.class.getSimpleName() + "#LoginDialogListener" + "#onError " + e.toString());

		}

		@Override
		public void onCancel() {
			Logger.i(AccountActivity.class.getSimpleName() + "#LoginDialogListener" + "#onCancel");
		}
	}

	private class UserProfileRequestListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {
			try {
				Logger.i(UserProfileRequestListener.class.getSimpleName() + "#onComplete()");
				Logger.i("Response: " + response.toString());
				JSONObject json = Util.parseJson(response);
				String userId = json.getString("id");
				String userName = json.getString("name");

				mApplication.getFBConnection().saveUserIdInPersistentMemory(userId);
				mApplication.getFBConnection().saveAccount(userId, userName);

				startActivity(new Intent(AccountActivity.this, MainActivity.class));

				Logger.i("userId: " + userId);

			} catch (JSONException e) {
				Logger.i(UserProfileRequestListener.class.getSimpleName() + e.toString());
			} catch (FacebookError e) {
				Logger.i(UserProfileRequestListener.class.getSimpleName() + e.toString());
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(UserProfileRequestListener.class.getSimpleName() + e.toString());
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(UserProfileRequestListener.class.getSimpleName() + e.toString());
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(UserProfileRequestListener.class.getSimpleName() + e.toString());
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(UserProfileRequestListener.class.getSimpleName() + e.toString());
		}

	}

	class AccountAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView name;
			public ImageView profilePicture;
		}

		@Override
		public int getCount() {
			return mAccounts.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			View view = convertView;
			final ViewHolder holder;
			if (convertView == null) {
				view = getLayoutInflater().inflate(R.layout.account, null);

				holder = new ViewHolder();
				holder.name = (TextView) view.findViewById(R.id.accountName);
				holder.profilePicture = (ImageView) view.findViewById(R.id.accountProfilePicture);
				view.setTag(holder);
			} else
				holder = (ViewHolder) view.getTag();

			holder.name.setText(mAccounts.get(position).getName());

			//((FBClientApplication) getApplication()).getImageLoader().displayImage(mAccounts.get(position).getProfilePicture(), holder.profilePicture, mOptions);
			return view;
		}
	}

}