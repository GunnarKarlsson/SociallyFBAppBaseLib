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

package com.bluebitapps.fbclientbase.friends;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;
import com.bluebitapps.utils.ClearableEditText;
import com.bluebitapps.utils.ClearableEditText.OnClearClickListener;
import com.bluebitapps.utils.InputUtil;
import com.bluebitapps.utils.OutputUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.google.android.gms.ads.AdView;

public class FriendsFragment extends BaseNavigationFragment implements OnClearClickListener {

	public static final String STATE_SEARCH = "state search";
	private String mSearchString;

	private List<Friend> mFriends;
	private List<Friend> mOriginalFriendsList;
	private ListView mListView;
	private ItemAdapter mAdapter;
	private FriendsDataUpdateReceiver mDataUpdateReceiver;
	private LoadingView mLoadingView;
	private ClearableEditText mEditText;
	private CustomTextWatcher mTextWatcher;
	private boolean isFirstDataRequest;

	private class FriendsDataUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("jan17", Logger.getClassAndMethod());

			if (FriendsService.REFRESH_FRIENDS_DATA_SUCCESS.equals(intent.getAction())) {

				Logger.i(FriendsFragment.class.getSimpleName() + "." + FriendsDataUpdateReceiver.class.getSimpleName() + "." + FriendsService.REFRESH_FRIENDS_DATA_SUCCESS);

				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();

				getFriends();
			}

			if (FriendsService.REFRESH_FRIENDS_DATA_FAIL.equals(intent.getAction())) {
				Log.i("jan17", Logger.getClassAndMethod());

				Logger.i(FriendsFragment.class.getSimpleName() + "." + FriendsDataUpdateReceiver.class.getSimpleName() + "." + FriendsService.REFRESH_FRIENDS_DATA_FAIL);

				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();
				if (getActivity() != null) {
					OutputUtil.showCrouton(getActivity(), getActivity().getResources().getString(R.string.friends_list_could_not_be_retrieved));
				}
			}
		}
	}

	public static final FriendsFragment newInstance(String objectId, String state, String searchString) {

		FriendsFragment f = new FriendsFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.OBJECT_ID_KEY, objectId);
		bundle.putString(Constants.STATE_KEY, state);
		bundle.putString(Constants.SEARCH_STRING, searchString);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i("jan17", Logger.getClassAndMethod());
		if (mDataUpdateReceiver == null) {
			mDataUpdateReceiver = new FriendsDataUpdateReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(FriendsService.REFRESH_FRIENDS_DATA_SUCCESS);
			intentFilter.addAction(FriendsService.REFRESH_FRIENDS_DATA_FAIL);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			if (getActivity() != null) {
				getActivity().registerReceiver(mDataUpdateReceiver, intentFilter);
			}
		}
		startRefreshMenuItemAnimation();
		getFriends();
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i("jan17", Logger.getClassAndMethod());
		if (mDataUpdateReceiver != null && getActivity() != null) {
			getActivity().unregisterReceiver(mDataUpdateReceiver);
			// An activity can be resumed after pausing, so the activity may not
			// register the listener in the second call to onResume.
			mDataUpdateReceiver = null;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("jan17", Logger.getClassAndMethod());

		// setRetainInstance(true);
		setHasOptionsMenu(true);
		isFirstDataRequest = true;
		prepareRefreshMenuItemAnimation();

		// FBClientApplication.getApplication().getBackStack().push(SectionManager.STATE_FRIENDS);

		if (getArguments() != null) {
			setState(getArguments().getString(Constants.STATE_KEY));
			mSearchString = getArguments().getString(Constants.SEARCH_STRING);
		}

		if(getActivity()!=null){
			String title = getActivity().getResources().getString(R.string.friends);
			setTitle(title);
		}
		

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.image_list_search);

		mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);

		mEditText = (ClearableEditText) vg.findViewById(R.id.edit_text);
		mEditText.setOnClearClickListener(this);
		mTextWatcher = new CustomTextWatcher();
		mEditText.addTextChangedListener(mTextWatcher);

		mListView = (ListView) vg.findViewById(R.id.image_list_view);

		mFriends = new ArrayList<Friend>();
		mAdapter = new ItemAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setTextFilterEnabled(true);

		return vg;
	}

	@Override
	public void onDestroy() {
		Log.i("jan17", Logger.getClassAndMethod());
		if (mEditText != null && mTextWatcher != null) {
			mEditText.removeTextChangedListener(mTextWatcher);
		}
		super.onDestroy();
	}

	@Override
	public void onRefresh() {
		Log.i("jan17", Logger.getClassAndMethod());
		Logger.i(Logger.getClassAndMethod());
		getFriendsFromFB();
	}

	private void getFriends() {

		//NPE Crash on Kindle Fire testing
		if(mFriends==null){
			mFriends = new ArrayList<Friend>();
		}
		
		mFriends.clear();

		if (getState() != null && getState().equals(STATE_SEARCH)) {

			if (getActivity() != null) {
				ActionBar actionBar = getActivity().getActionBar();
				if (actionBar != null) {
					actionBar.setTitle(getActivity().getResources().getString(R.string.search));
					actionBar.setSubtitle(null);
				}
			}

			doSearch();

		} else {

			getFriendsFromDatabase();

			if (isFirstDataRequest) {
				Log.i("jan17", Logger.getClassAndMethod() + " isFirstDataRequest==true");
				if (mFriends.size() > 0) {
					mLoadingView.setVisibility(View.GONE);
				}

				if (getActivity() != null) {
					getActivity().invalidateOptionsMenu();
				}

				getFriendsFromFB();
			} else {
				Log.i("jan17", Logger.getClassAndMethod() + "isFirstDataRequest==false");
				if (mFriends.size() < 1) {
					if(getActivity()!=null){						
						OutputUtil.showCrouton(getActivity(), getActivity().getResources().getString(R.string.no_friends_data_available));
					}
				}
				mLoadingView.setVisibility(View.GONE);

				if (getActivity() != null) {
					getActivity().invalidateOptionsMenu();
				}
			}

		}
	}

	private void getFriendsFromDatabase() {
		Log.i("jan17", Logger.getClassAndMethod());

		if (getActivity() == null) {
			return;
		}

		Cursor c = ((FBClientApplication) getActivity().getApplication()).getFriendsData().getFriends();

		if (c != null) {
			if (c.moveToFirst()) {
				do {

					String id = c.getString(c.getColumnIndex(FriendsData.C_ID));
					String name = c.getString(c.getColumnIndex(FriendsData.C_NAME));

					Friend friend = new Friend();
					friend.setId(id);
					friend.setName(name);
					mFriends.add(friend);

				} while (c.moveToNext());
			}
		}
		if (c != null) {

			c.close();
		}

		mAdapter.notifyDataSetChanged();

		if (getActivity() != null) {
			
			
			String friendsString = getActivity().getResources().getString(R.string.friends_lowercase);
			String str = mFriends.size() + " " + friendsString;
			getActivity().getActionBar().setSubtitle(str);
		}

		mOriginalFriendsList = new ArrayList<Friend>(mFriends);

	}

	private void getFriendsFromFB() {
		Log.i("jan17", Logger.getClassAndMethod());

		if (getActivity() == null) {
			return;
		}

		getActivity().startService(new Intent(getActivity(), FriendsService.class));
	}

	class CustomTextWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
			// Do nothing
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// Do nothing
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			mAdapter.getFilter().filter(s);
			mAdapter.notifyDataSetChanged();
		}
	}

	class ItemAdapter extends BaseAdapter implements Filterable {

		private class ViewHolder {
			public TextView name;
			public ImageView picture;
		}

		@Override
		public int getCount() {
			if (mFriends != null) {
				return mFriends.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return mFriends.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			View view = convertView;
			final ViewHolder holder;

			if (mFriends != null) {

				if (convertView == null) {
					view = getActivity().getLayoutInflater().inflate(R.layout.friend_item, null);

					holder = new ViewHolder();
					holder.picture = (ImageView) view.findViewById(R.id.image);
					holder.name = (TextView) view.findViewById(R.id.friendName);
					configText(holder.name);

					view.setTag(holder);
				} else
					holder = (ViewHolder) view.getTag();

				final Friend friend = mFriends.get(position);

				getImageLoader().displayImage(friend.getPicture(getApplication()), holder.picture, getImageDisplayOptions());

				holder.name.setText(friend.getName());

				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						Intent intent = new Intent(Constants.REQUEST_PROFILE_ACTIVITY);
						intent.putExtra(Constants.OBJECT_ID_KEY, friend.getId());
						intent.putExtra(Constants.OBJECT_TITLE_KEY, friend.getName());
						intent.putExtra(Constants.USER_TYPE_KEY, Constants.USER_TYPE_FRIEND);
						intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);

						if (getActivity() != null) {
							getActivity().sendBroadcast(intent);
						}

					}
				});

			}
			return view;
		}

		@Override
		public Filter getFilter() {

			Filter filter = new Filter() {

				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					mFriends = (List<Friend>) results.values;
					notifyDataSetChanged();
				}

				@Override
				protected FilterResults performFiltering(CharSequence constraint) {

					List<Friend> friendsMatchingSearch = new ArrayList<Friend>();

					FilterResults results = new FilterResults();
					friendsMatchingSearch.clear();

					if (constraint == null || constraint.length() == 0) {
						if (mFriends != null) {

							results.count = mFriends.size();
							results.values = mFriends;

							friendsMatchingSearch = mFriends;
						}
					} else {
						constraint = constraint.toString().toLowerCase();
						if (mFriends != null) {

							for (int i = 0; i < mFriends.size(); i++) {
								String name = mFriends.get(i).getName().toLowerCase();
								if (name.toLowerCase().startsWith(constraint.toString())) {
									friendsMatchingSearch.add(mFriends.get(i));
								}
							}

							results.count = friendsMatchingSearch.size();
							results.values = friendsMatchingSearch;
						}
					}

					return results;
				}
			};

			return filter;
		}
	}

	@Override
	public void onClearButtonClicked() {
		Logger.i(Logger.getClassAndMethod());
		if (mOriginalFriendsList != null) {
			mFriends = new ArrayList<Friend>(mOriginalFriendsList);
		}
		mAdapter.notifyDataSetChanged();
		InputUtil.hideKeyboard(getActivity());
	}

	private void doSearch() {
		Bundle params = new Bundle();
		String typeString = "user";
		params.putString("type", typeString);
		params.putString("q", mSearchString);
		String query = "search";
		FBClientApplication.getApplication().getFBConnection().getAsyncFacebookRunner().request(query, params, new SearchListener());

	}

	private class SearchListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {

			ArrayList<Friend> users = new ArrayList<Friend>();
			try {

				final JSONObject dataJsonObject = new JSONObject(response);
				final JSONArray jsonArray = dataJsonObject.getJSONArray("data");

				// int usersCount = jsonArray.length();

				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject obj = jsonArray.getJSONObject(i);

						Friend user = Friend.fromJSON(obj);
						users.add(user);
					}
				}

				final ArrayList<Friend> tempUsers = new ArrayList<Friend>(users);

				if (tempUsers.size() < 1) {
					OutputUtil.showNoResultsMessage(getActivity(), mLoadingView);
				} else {

					if (getActivity() != null) {

						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mFriends = tempUsers;
								if (mAdapter != null) {
									mAdapter.notifyDataSetChanged();
								}
								if (mLoadingView != null) {
									mLoadingView.setVisibility(View.GONE);
								}
							}
						});
					}
				}

			} catch (JSONException e) {
				Logger.i(Logger.getClassAndMethod() + e.toString());
				OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
			}

		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);

		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
		}

	}
}
