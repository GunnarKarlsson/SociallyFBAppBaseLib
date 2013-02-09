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

package com.bluebitapps.fbclientbase.friendrequests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bluebitapps.utils.FacebookUtils;
import com.bluebitapps.utils.ListViewUtil;
import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

public class FriendRequestsFragment extends BaseNavigationFragment {

	private static final String INSTANCE_STATE_KEY = "instance state key";

	private ArrayList<FriendRequest> mFriendRequests;
	private ListView mListView;
	private LoadingView mLoadingView;
	private ItemAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		mFriendRequests = new ArrayList<FriendRequest>();

		if (savedInstanceState != null && savedInstanceState.getParcelableArrayList(INSTANCE_STATE_KEY) != null) {
			//mFriendRequests = savedInstanceState.getParcelableArrayList(INSTANCE_STATE_KEY);
		} 

		setHasOptionsMenu(true);
		
		setTitle("Friend Requests");
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.item_list);

		mListView = (ListView) vg.findViewById(R.id.list);
		mAdapter = new ItemAdapter();
		mListView.setAdapter(mAdapter);
		mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);

		return vg;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mFriendRequests != null && mFriendRequests.size() > 0) {
			outState.putParcelableArrayList(INSTANCE_STATE_KEY, mFriendRequests);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		if(mFriendRequests.size() < 1){
			getFriendRequests();
		}
	}

	@Override
	protected void onRefresh() {
		//super.onRefresh();
		Log.i("frtest2", "OnRefresh()");
		getFriendRequests();
	}

	private void getFriendRequests() {
		final String query1 = "SELECT uid_from, time, message, unread FROM friend_request WHERE uid_to = me()";
		final String query2 = "select name, uid from user where uid IN (select uid_from FROM #query1)";

		final JSONObject jsonQueries = new JSONObject() {
			{
				try {
					put("query1", query1);
					put("query2", query2);
				} catch (Exception e) {
					Logger.i(FriendRequestsFragment.class.getSimpleName() + "#getFriendRequests: jsonQueries. " + e.toString());
				}
			}
		};

		Bundle params = new Bundle();
		params.putString("method", "fql.multiquery");
		params.putString("queries", jsonQueries.toString());
		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(null, params, new FriendRequestListener());
	}

	private class FriendRequestListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {

			final List<FriendRequest> requests = new ArrayList<FriendRequest>();

			try {
				JSONArray a = new JSONArray(response);
			    Log.i("frtest2", response.toString());
				JSONObject friendRequestsObj = a.getJSONObject(0);
				JSONObject userNamesObj = a.getJSONObject(1);
				JSONArray friendRequestsJsonArray = friendRequestsObj.getJSONArray("fql_result_set");
				JSONArray userNamesJsonArray = userNamesObj.getJSONArray("fql_result_set");

				HashMap<String, String> userNamesMap = new HashMap<String, String>();

				if (userNamesJsonArray.length() > 0) {

					// Place user names in map
					for (int i = 0; i < userNamesJsonArray.length(); i++) {
						String uid = "";
						String name = "";
						if (userNamesJsonArray.getJSONObject(i).has("uid")) {
							uid = userNamesJsonArray.getJSONObject(i).getString("uid");
						}
						if (userNamesJsonArray.getJSONObject(i).has("name")) {
							name = userNamesJsonArray.getJSONObject(i).getString("name");
						}

						userNamesMap.put(uid, name);
					}
				}

				// Create friend request list an add names from map to list
				if (friendRequestsJsonArray.length() > 0) {
					for (int i = 0; i < friendRequestsJsonArray.length(); i++) {
						JSONObject obj = friendRequestsJsonArray.getJSONObject(i);
						FriendRequest friendRequest = FriendRequest.fromJSON(obj);

						if (userNamesMap.containsKey(friendRequest.getFromUid())) {
							String name = userNamesMap.get(friendRequest.getFromUid());
							friendRequest.setFromName(name);
						}

						requests.add(friendRequest);
					}
				}

			} catch (JSONException e) {
				Logger.i(FriendRequestsFragment.class.getSimpleName() + "." + FriendRequestListener.class.getSimpleName() + "." + e.toString());
				OutputUtil.showCrouton(getActivity(), "Friends request data could not be retrieved");
			}

			if (getActivity() != null) {

				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {

						if (mFriendRequests != null) {
							mFriendRequests.clear();
							mFriendRequests.addAll(requests);
							Log.i("frtest2", Integer.toString(mFriendRequests.size()));
							mAdapter.notifyDataSetChanged();
							
							String requestWord = mFriendRequests.size()==1?"request":"requests";
							String str = mFriendRequests.size() + " " + requestWord;
							getActivity().getActionBar().setSubtitle(str);
						}

						//if (mListView != null) {
							//mListView.setVisibility(View.VISIBLE);
						//}

						if (mLoadingView != null) {
							mLoadingView.setVisibility(View.GONE);
						}

						//if (mAdapter != null) {
						//}

						if (mFriendRequests.size() < 1) {
							OutputUtil.showCrouton(getActivity(), "No pending friend requests");
						}
						
						stopRefreshMenuItemAnimation();
						//getActivity().invalidateOptionsMenu();
						

					}
				});
			} else {
				Logger.i(FriendRequestsFragment.class.getSimpleName() + FriendRequestListener.class.getSimpleName() + ".getActivity()==null");
				OutputUtil.showCrouton(getActivity(), "Friends request data could not be retrieved");
			}

		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
		}
	}

	class ItemAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView fromName;
			public ImageView fromPicture;
			public Button button;
		}

		@Override
		public int getCount() {
			if (mFriendRequests != null) {
				return mFriendRequests.size();
			} else {
				return 0;
			}
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

			if (mFriendRequests != null) {

				FriendRequest request = mFriendRequests.get(position);
				if (convertView == null) {
					view = getActivity().getLayoutInflater().inflate(R.layout.friend_request_item, null);

					holder = new ViewHolder();
					holder.fromName = (TextView) view.findViewById(R.id.userName);
					configBodyText(holder.fromName);
					
					holder.fromPicture = (ImageView) view.findViewById(R.id.userImage);
					
					holder.button = (Button) view.findViewById(R.id.respondButton);

					view.setTag(holder);
				} else
					holder = (ViewHolder) view.getTag();

				holder.fromName.setText(request.getFromName());

				getImageLoader().displayImage(request.getProfilePicture(), holder.fromPicture, getImageDisplayOptions());

				holder.button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String url = "https://m.facebook.com/" + FBClientApplication.getApplication().getFBConnection().getUserId() + "/friends#!/friends";
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
						startActivity(intent);
					}
				});
			}

			return view;
		}
	}
}