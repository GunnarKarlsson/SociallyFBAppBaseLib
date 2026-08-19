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

package com.bluebitapps.fbclientbase.groups;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bluebitapps.utils.OutputUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

public class GroupsFragment extends BaseNavigationFragment {

	public static final String STATE_SEARCH = "state search";

	private String mSearchString;
	private List<Group> mGroups;
	private ListView mListView;
	private ItemAdapter mAdapter;
	private GroupDataUpdateReceiver mDataUpdateReceiver;
	private LoadingView mLoadingView;
	private boolean isFirstDataRequest;

	private class GroupDataUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			Logger.i(Logger.getClassAndMethod());

			if (GroupsService.REFRESH_GROUPS_DATA_SUCCESS.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + GroupsService.REFRESH_GROUPS_DATA_SUCCESS);
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();
				getGroups();
			}

			if (GroupsService.REFRESH_GROUPS_DATA_FAIL.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + GroupsService.REFRESH_GROUPS_DATA_FAIL);
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();
				if (getActivity() != null) {
					OutputUtil.showCrouton(getActivity(), getActivity().getResources().getString(R.string.data_not_available));
				}
			}
		}
	}

	public static final GroupsFragment newInstance(String state, String searchString) {
		GroupsFragment f = new GroupsFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.STATE_KEY, state);
		bundle.putString(Constants.SEARCH_STRING, searchString);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mDataUpdateReceiver == null && getActivity() != null) {
			mDataUpdateReceiver = new GroupDataUpdateReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(GroupsService.REFRESH_GROUPS_DATA_SUCCESS);
			intentFilter.addAction(GroupsService.REFRESH_GROUPS_DATA_FAIL);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			getActivity().registerReceiver(mDataUpdateReceiver, intentFilter);
		}

		startRefreshMenuItemAnimation();
		getGroups();
	}

	@Override
	public void onPause() {
		super.onPause();
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

		setRetainInstance(true);
		setHasOptionsMenu(true);
		isFirstDataRequest = true;
		prepareRefreshMenuItemAnimation();

		if (getArguments() != null) {
			setState(getArguments().getString(Constants.STATE_KEY));
			mSearchString = getArguments().getString(Constants.SEARCH_STRING);
		}
		Logger.i(Logger.getClassAndMethod() + "mSearchString: " + mSearchString);

		if (getActivity() != null) {
			String title = getActivity().getResources().getString(R.string.groups);
			setTitle(title);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup vg = null;
		if (getActivity() != null) {
			vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.image_list);
			mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);
			mGroups = new ArrayList<Group>();
			mListView = (ListView) vg.findViewById(R.id.image_list_view);
			mAdapter = new ItemAdapter();
			mListView.setAdapter(mAdapter);
		}
		return vg;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

				if (getActivity() != null) {

					Intent intent = new Intent(getActivity(), GroupActivity.class);
					intent.putExtra(Constants.OBJECT_ID_KEY, mGroups.get(position).getId());
					intent.putExtra(Constants.OBJECT_TITLE_KEY, mGroups.get(position).getName());
					intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
					getActivity().startActivity(intent);
					getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				}

			}
		});
	}

	@Override
	public void onRefresh() {
		Logger.i(Logger.getClassAndMethod());
		startRefreshMenuItemAnimation();
		getGroupsFromFB();
	}

	private void getGroups() {
		startRefreshMenuItemAnimation();

		mGroups.clear();

		if (getState() != null && getState().equals(STATE_SEARCH)) {
			if (getActivity() != null) {
				OutputUtil.showCrouton(getActivity(), getActivity().getResources().getString(R.string.searching));
			}
			doSearch();
		} else {

			if (getActivity() == null) {
				return;
			}

			Cursor c = ((FBClientApplication) getActivity().getApplication()).getGroupsData().getGroups();

			if (c != null) {
				if (c.moveToFirst()) {
					do {

						String id = c.getString(c.getColumnIndex(GroupsData.C_ID));
						String name = c.getString(c.getColumnIndex(GroupsData.C_NAME));
						String version = c.getString(c.getColumnIndex(GroupsData.C_VERSION));
						String unread = c.getString(c.getColumnIndex(GroupsData.C_UNREAD));
						String bookmarkOrder = c.getString(c.getColumnIndex(GroupsData.C_BOOKMARK_ORDER));

						Group group = new Group();
						group.setId(id);
						group.setName(name);
						group.setVersion(version);
						group.setUnread(unread);
						group.setBookmarkOrder(bookmarkOrder);

						mGroups.add(group);

					} while (c.moveToNext());
				}
			}
			if (c != null) {

				c.close();
			}

			if (getActivity() != null) {
				String groupString = getActivity().getResources().getString(R.string.group_lowercase);
				String groupsString = getActivity().getResources().getString(R.string.groups_lowercase);
				String groupWord = mGroups.size() == 1 ? groupString : groupsString;
				String str = mGroups.size() + " " + groupWord;
				getActivity().getActionBar().setSubtitle(str);
			}

			mAdapter.notifyDataSetChanged();

			if (getActivity() != null) {
				getActivity().invalidateOptionsMenu();
			}

			mLoadingView.setVisibility(View.GONE);

			if (isFirstDataRequest) {
				if (mGroups.size() > 0) {
					mLoadingView.setVisibility(View.GONE);
				}
				getGroupsFromFB();
			} else {
				if (mGroups.size() < 1) {
					mLoadingView.setVisibility(View.GONE);
					if (getActivity() != null) {
						OutputUtil.showCrouton(getActivity(), getActivity().getResources().getString(R.string.data_not_available));
					}
				}

			}
		}
	}

	private void getGroupsFromFB() {
		if (getActivity() == null) {
			return;
		}
		getActivity().startService(new Intent(getActivity(), GroupsService.class));
	}

	class ItemAdapter extends BaseAdapter {

		private class ViewHolder {
			public ImageView picture;
			public TextView name;
		}

		@Override
		public int getCount() {
			if (mGroups != null) {
				return mGroups.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return mGroups.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			View view = convertView;
			final ViewHolder holder;

			if (mGroups != null) {

				Group group = mGroups.get(position);
				if (convertView == null && getActivity() != null) {
					view = getActivity().getLayoutInflater().inflate(R.layout.group_item, null);

					holder = new ViewHolder();

					holder.name = (TextView) view.findViewById(R.id.groupName);
					configText(holder.name);

					holder.picture = (ImageView) view.findViewById(R.id.image);

					view.setTag(holder);
				} else
					holder = (ViewHolder) view.getTag();

				holder.name.setText(mGroups.get(position).getName());

				String token = getApplication().getFBConnection().getFacebook().getAccessToken();

				String query = "https://graph.facebook.com/" + group.getId() + "/picture?access_token=" + token;

				getImageLoader().displayImage(query, holder.picture, getImageDisplayOptions());

			}
			return view;
		}
	}

	private void doSearch() {
		Bundle params = new Bundle();
		String typeString = "group";
		params.putString("type", typeString);
		params.putString("q", mSearchString);
		String query = "search";
		FBClientApplication.getApplication().getFBConnection().getAsyncFacebookRunner().request(query, params, new SearchListener());
	}

	private class SearchListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {

			ArrayList<Group> groups = new ArrayList<Group>();
			try {

				final JSONObject dataJsonObject = new JSONObject(response);
				final JSONArray jsonArray = dataJsonObject.getJSONArray("data");

				Logger.i("Number of groups: " + jsonArray.length());

				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject obj = jsonArray.getJSONObject(i);

						Group group = Group.fromJSON(obj);
						groups.add(group);
					}
				}

				final ArrayList<Group> tempGroups = new ArrayList<Group>(groups);

				if (tempGroups.size() < 1) {
					if (getActivity() != null) {
						OutputUtil.showCrouton(getActivity(), getActivity().getResources().getString(R.string.data_not_available));
					}
				} else {

					if (getActivity() != null) {

						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {

								mGroups = tempGroups;

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