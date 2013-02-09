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

package com.bluebitapps.fbclientbase.events;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluebitapps.utils.OutputUtil;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseFragment;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.profile.ProfileActivity;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;
import com.bluebitapps.fbclientbase.user.UserActivity;

public class EventUserListFragment extends BaseNavigationFragment {

	private static final String EVENT_ID_KEY = "event id key";

	private ArrayList<EventUser> mUsers;
	private LoadingView mLoadingView;
	private ListView mListView;
	private ItemAdapter mAdapter;

	public static final EventUserListFragment newInstance(String eventListState, String eventId, String title, boolean refreshOnlyMenuFlag) {
		EventUserListFragment f = new EventUserListFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.STATE_KEY, eventListState);
		bundle.putString(Constants.OBJECT_TITLE_KEY, title);
		bundle.putString(EventUserListFragment.EVENT_ID_KEY, eventId);
		bundle.putBoolean(FLAG_HAS_ONLY_REFRESH_MENU_ITEM_IN_ACTIONBAR, refreshOnlyMenuFlag);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();
		setState(bundle.getString(Constants.STATE_KEY));
		setObjectId(bundle.getString(EventUserListFragment.EVENT_ID_KEY));
		setTitle(bundle.getString(Constants.OBJECT_TITLE_KEY));
		Logger.i(EventUserListFragment.class.getSimpleName() + "#onCreated. State: " + getState());

		setHasOptionsMenu(true);

		mUsers = new ArrayList<EventUser>();

		getEventUsers();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.image_list);
		mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);
		mListView = (ListView) vg.findViewById(R.id.image_list_view);
		//TextView padding = new TextView(getActivity());
		//padding.setHeight(getResources().getDimensionPixelOffset(R.dimen.item_list_padding));
		//mListView.addHeaderView(padding);
		//mListView.addFooterView(padding);
		mAdapter = new ItemAdapter();
		mListView.setAdapter(mAdapter);

		return vg;
	}

	private void getEventUsers() {

		startRefreshMenuItemAnimation();

		Logger.i("EventUserListFragment#getEventUsers()");

		if (getActivity() == null) {
			return;
		}
		AsyncFacebookRunner runner = ((FBClientApplication) getActivity().getApplication()).getFBConnection().getAsyncFacebookRunner();
		UserListRequestListener listener = new UserListRequestListener();
		if (getState().equalsIgnoreCase(Constants.STATE_EVENT_INVITED)) {
			runner.request(getObjectId() + "/invited", listener);
		} else if (getState().equalsIgnoreCase(Constants.STATE_EVENT_DECLINED)) {
			runner.request(getObjectId() + "/declined", listener);
		} else if (getState().equalsIgnoreCase(Constants.STATE_EVENT_ATTENDING)) {
			runner.request(getObjectId() + "/attending", listener);
		} else if (getState().equalsIgnoreCase(Constants.STATE_EVENT_MAYBE)) {
			runner.request(getObjectId() + "/maybe", listener);
		}
	}

	private class UserListRequestListener implements RequestListener {

		ArrayList<EventUser> asyncUsers;

		@Override
		public void onComplete(String response, Object state) {
			try {

				asyncUsers = new ArrayList<EventUser>();

				final JSONObject dataJsonObject = new JSONObject(response);
				final JSONArray jsonArray = dataJsonObject.getJSONArray("data");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject obj = jsonArray.getJSONObject(i);
					EventUser eventUser = EventUser.fromJSON(obj);
					asyncUsers.add(eventUser);
				}

				if (getActivity() != null) {

					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mUsers = asyncUsers;
							setItems();
							stopRefreshMenuItemAnimation();
						}
					});
				} else {
					Logger.i(EventUserListFragment.class.getSimpleName() + "." + UserListRequestListener.class.getSimpleName() + ".getActivity() == null");
					OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
					stopRefreshMenuItemAnimation();
				}

			} catch (JSONException e) {
				Logger.i(EventUserListFragment.class.getSimpleName() + "." + UserListRequestListener.class.getSimpleName() + "." + e.toString());
				OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
				stopRefreshMenuItemAnimation();
			}

		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(EventUserListFragment.class.getSimpleName() + "." + UserListRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
			stopRefreshMenuItemAnimation();
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(EventUserListFragment.class.getSimpleName() + "." + UserListRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
			stopRefreshMenuItemAnimation();
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(EventUserListFragment.class.getSimpleName() + "." + UserListRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
			stopRefreshMenuItemAnimation();
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(EventUserListFragment.class.getSimpleName() + "." + UserListRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
			stopRefreshMenuItemAnimation();
		}
	}

	private void setItems() {

		mLoadingView.setVisibility(View.GONE);

		if (mUsers == null || mUsers.size() < 1) {
			OutputUtil.showCrouton(getActivity(), "This list is empty");
			

		} else {

			mAdapter.notifyDataSetChanged();
		}
	}

	class ItemAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView name;
			public ImageView picture;
			public TextView rsvpStatus;
		}

		@Override
		public int getCount() {
			if (mUsers != null) {
				return mUsers.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return mUsers.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			View view = convertView;
			final ViewHolder holder;

			if (mUsers != null) {

				if (convertView == null) {
					view = getActivity().getLayoutInflater().inflate(R.layout.friend_item, null);

					holder = new ViewHolder();
					holder.picture = (ImageView) view.findViewById(R.id.image);
					holder.name = (TextView) view.findViewById(R.id.friendName);
					// holder.rsvpStatus = (TextView)
					// view.findViewById(R.id.rsvpStatus);

					view.setTag(holder);
				} else
					holder = (ViewHolder) view.getTag();

				final EventUser eventUser = mUsers.get(position);

				getImageLoader().displayImage(eventUser.getPicture(getApplication()), holder.picture, getImageDisplayOptions());

				holder.name.setText(eventUser.getName());
				configText(holder.name);
				// holder.rsvpStatus.setText(eventUser.getRsvpStatus());

				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						/*
						 * Logger.i("FriendsFragment id: " + eventUser.getId());
						 * Intent intent = new Intent(getActivity(),
						 * UserActivity.class);
						 * intent.putExtra(Constants.USER_ID_KEY,
						 * eventUser.getId()); startActivity(intent);
						 * getActivity
						 * ().overridePendingTransition(R.anim.slide_in_right,
						 * R.anim.slide_out_left);
						 */

						Intent intent = new Intent(getActivity(), ProfileActivity.class);
						intent.putExtra(Constants.OBJECT_ID_KEY, eventUser.getId());
						intent.putExtra(BaseThemedActivity.CLEAR_TOP_ON_HOME_SELECTED, true);
						getActivity().startActivity(intent);
					}
				});
			}

			return view;
		}
	}
}