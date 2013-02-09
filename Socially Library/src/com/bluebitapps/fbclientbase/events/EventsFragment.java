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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bluebitapps.utils.FacebookUtils;
import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
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

public class EventsFragment extends BaseNavigationFragment {

	private static final String STATE_SEARCH = "state search";

	private String mSearchString;
	private List<Event> mEvents;
	private ListView mListView;
	private ItemAdapter mAdapter;
	private EventDataUpdateReceiver mDataUpdateReceiver;
	private LoadingView mLoadingView;
	private boolean isFirstDataRequest;

	private class EventDataUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			Log.i("jan16", Logger.getClassAndMethod());

			if (EventsService.REFRESH_EVENTS_DATA_SUCCESS.equals(intent.getAction())) {
				Logger.i(EventsFragment.class.getSimpleName() + "." + EventDataUpdateReceiver.class.getSimpleName() + "." + EventsService.REFRESH_EVENTS_DATA_SUCCESS);
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();
				getEvents();
			}

			if (EventsService.REFRESH_EVENTS_DATA_FAIL.equals(intent.getAction())) {
				Logger.i(EventsFragment.class.getSimpleName() + "." + EventDataUpdateReceiver.class.getSimpleName() + "." + EventsService.REFRESH_EVENTS_DATA_FAIL);
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();
				if (getActivity() != null) {
					OutputUtil.showCrouton(getActivity(), "Latest events could not be fetched");
				}
			}
		}
	}

	public static final EventsFragment newInstance(String state, String searchString) {
		EventsFragment f = new EventsFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.STATE_KEY, state);
		bundle.putString(Constants.SEARCH_STRING, searchString);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.i("jan16", Logger.getClassAndMethod());

		if (mDataUpdateReceiver == null) {
			mDataUpdateReceiver = new EventDataUpdateReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(EventsService.REFRESH_EVENTS_DATA_SUCCESS);
			intentFilter.addAction(EventsService.REFRESH_EVENTS_DATA_FAIL);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			if (getActivity() != null) {
				getActivity().registerReceiver(mDataUpdateReceiver, intentFilter);
			}
		}

		startRefreshMenuItemAnimation();
		getEvents();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mDataUpdateReceiver != null && getActivity() != null) {
			getActivity().unregisterReceiver(mDataUpdateReceiver);
			mDataUpdateReceiver = null;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i("jan16", Logger.getClassAndMethod());
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setHasOptionsMenu(true);
		isFirstDataRequest = true;
		prepareRefreshMenuItemAnimation();

		setState(getArguments().getString(Constants.STATE_KEY));
		mSearchString = getArguments().getString(Constants.SEARCH_STRING);

		setTitle("Events");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.image_list);
		mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);

		mListView = (ListView) vg.findViewById(R.id.image_list_view);
		// TextView padding = new TextView(getActivity());
		// padding.setHeight(getResources().getDimensionPixelOffset(R.dimen.item_list_padding));
		// mListView.addHeaderView(padding);
		// mListView.addFooterView(padding);

		mAdapter = new ItemAdapter();

		mListView.setAdapter(mAdapter);

		mEvents = new ArrayList<Event>();

		return vg;
	}

	@Override
	public void onRefresh() {
		Logger.i(Logger.getClassAndMethod());
		getEventsFromFB();
	}

	private void getEvents() {
		Log.i("jan16", Logger.getClassAndMethod());

		if (getState() != null && getState().equals(STATE_SEARCH)) {
			OutputUtil.showCrouton(getActivity(), "Searching for Events");
			doSearch();
		} else {

			getEventsFromDatabase();

			Log.i("jan16", Logger.getClassAndMethod() + "after events retrieved from db. mEvents.size(): " + mEvents.size());

			if (isFirstDataRequest) {

				if (mEvents.size() > 0) {
					mLoadingView.setVisibility(View.GONE);
				}
				if (getActivity() != null) {
					getActivity().invalidateOptionsMenu();
				}

				Log.i("jan16", Logger.getClassAndMethod() + "isFirstDataRequest");

				getEventsFromFB();

			} else {
				if (mEvents.size() < 1) {
					OutputUtil.showCrouton(getActivity(), "No events available");
				}

				Log.i("jan16", Logger.getClassAndMethod() + "is not first datarequest");

				mLoadingView.setVisibility(View.GONE);

				if (getActivity() != null) {
					getActivity().invalidateOptionsMenu();
				}
			}

		}
	}

	private void getEventsFromDatabase() {

		mEvents.clear();

		if (getActivity() == null) {
			return;
		}
		Cursor c = ((FBClientApplication) getActivity().getApplication()).getEventsData().getEvents();

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					String id = c.getString(c.getColumnIndex(EventsData.C_ID));
					String name = c.getString(c.getColumnIndex(EventsData.C_NAME));
					String startTime = c.getString(c.getColumnIndex(EventsData.C_START_TIME));
					String endTime = c.getString(c.getColumnIndex(EventsData.C_END_TIME));
					String rsvpStatus = c.getString(c.getColumnIndex(EventsData.C_RSVP_STATUS));

					Event event = new Event();
					event.setId(id);
					event.setName(name);
					event.setStartTime(startTime);
					event.setEndTime(endTime);
					event.setRsvpStatus(rsvpStatus);

					if (StringUtil.notEmpty(event.getRsvpStatus()) && "attending".equals(event.getRsvpStatus())) {
						mEvents.add(event);
					}

				} while (c.moveToNext());
			}
		}

		if (c != null) {
			c.close();
		}

		if (getActivity() != null) {
			String eventWord = mEvents.size() == 1 ? "event" : "events";
			String str = mEvents.size() + " " + eventWord;
			getActivity().getActionBar().setSubtitle(str);
		}

		// mAdapter.notifyDataSetChanged();
	}

	private void getEventsFromFB() {
		if (getActivity() == null) {
			return;
		}
		getActivity().startService(new Intent(getActivity(), EventsService.class));
	}

	class ItemAdapter extends BaseAdapter {

		private class ViewHolder {
			public ViewGroup container;
			public ImageView picture;
			public TextView name;
			public TextView startTime;
			public TextView endTime;
			public TextView rsvpStatus;
		}

		@Override
		public int getCount() {
			if (mEvents != null) {
				return mEvents.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return mEvents.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			View view = convertView;
			final ViewHolder holder;

			if (mEvents != null) {

				final Event event = mEvents.get(position);

				if (convertView == null) {
					view = getActivity().getLayoutInflater().inflate(R.layout.event_row, null);

					holder = new ViewHolder();
					holder.container = (ViewGroup) view.findViewById(R.id.rootView);
					holder.picture = (ImageView) view.findViewById(R.id.eventPicture);

					holder.name = (TextView) view.findViewById(R.id.eventName);
					configText(holder.name);

					holder.startTime = (TextView) view.findViewById(R.id.eventStartTime);
					configText(holder.startTime);

					holder.endTime = (TextView) view.findViewById(R.id.eventEndTime);
					configText(holder.endTime);

					holder.rsvpStatus = (TextView) view.findViewById(R.id.eventRsvpStatus);
					configText(holder.rsvpStatus);

					view.setTag(holder);
				} else
					holder = (ViewHolder) view.getTag();

				holder.name.setText(mEvents.get(position).getName());

				if (mEvents.get(position).getStartTime().contains(":")) {

					String startTime = (String) FacebookUtils.convertFacebookEventTimeToRelativeTime(mEvents.get(position).getStartTime());
					holder.startTime.setText(startTime);

					String endTime = (String) FacebookUtils.convertFacebookEventTimeToRelativeTime(mEvents.get(position).getEndTime());
					holder.endTime.setText(endTime);
				} else {
					String startTime = (String) FacebookUtils.convertInvitedToEventTimeStamp(mEvents.get(position).getStartTime());
					holder.startTime.setText(startTime);
					String endTime = (String) FacebookUtils.convertInvitedToEventTimeStamp(mEvents.get(position).getEndTime());
					holder.endTime.setText(endTime);
				}

				if (StringUtil.notEmpty(mEvents.get(position).getRsvpStatus())) {

					holder.rsvpStatus.setText(mEvents.get(position).getRsvpStatus());
				}

				String token = getApplication().getFBConnection().getFacebook().getAccessToken();

				String query = "https://graph.facebook.com/" + event.getId() + "/picture?access_token=" + token;

				getImageLoader().displayImage(query, holder.picture, getImageDisplayOptions());

				holder.container.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						Intent intent = new Intent(getActivity(), EventActivity.class);
						intent.putExtra(Constants.OBJECT_ID_KEY, mEvents.get(position).getId());
						intent.putExtra(Constants.OBJECT_TITLE_KEY, mEvents.get(position).getName());
						getActivity().startActivity(intent);
						getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

					}
				});
			}

			return view;
		}
	}

	private void doSearch() {
		Bundle params = new Bundle();
		String typeString = "event";
		params.putString("type", typeString);
		params.putString("q", mSearchString);
		String query = "search";
		FBClientApplication.getApplication().getFBConnection().getAsyncFacebookRunner().request(query, params, new SearchListener());
	}

	private class SearchListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {

			ArrayList<Event> events = new ArrayList<Event>();
			try {

				final JSONObject dataJsonObject = new JSONObject(response);
				final JSONArray jsonArray = dataJsonObject.getJSONArray("data");

				Logger.i("Number of events: " + jsonArray.length());

				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject obj = jsonArray.getJSONObject(i);

						Event event = Event.fromJSON(obj);
						events.add(event);
					}
				}

				final ArrayList<Event> tempEvents = new ArrayList<Event>(events);

				if (tempEvents.size() < 1) {
					OutputUtil.showCrouton(getActivity(), "No search results found");
				} else {

					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							mEvents = tempEvents;
							mAdapter.notifyDataSetChanged();
							mLoadingView.setVisibility(View.GONE);
						}
					});
				}

			} catch (JSONException e) {
				OutputUtil.showCrouton(getActivity(), "Search results could not be fetched");
			}

		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i("EventsFragment: " + e.toString());
			OutputUtil.showCrouton(getActivity(), "Search results could not be fetched");

		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i("EventsFragment: " + e.toString());
			OutputUtil.showCrouton(getActivity(), "Search results could not be fetched");

		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i("EventsFragment: " + e.toString());
			OutputUtil.showCrouton(getActivity(), "Search results could not be fetched");

		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i("EventsFragment: " + e.toString());
			OutputUtil.showCrouton(getActivity(), "Search results could not be fetched");
		}

	}

}