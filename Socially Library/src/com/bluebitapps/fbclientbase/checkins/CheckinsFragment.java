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

import java.util.ArrayList;
import java.util.List;

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

import com.bluebitapps.utils.FacebookUtils;
import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.albums.Album;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.photos.ImageGridActivity;
import com.bluebitapps.fbclientbase.place.PlaceProfileActivity;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

public class CheckinsFragment extends BaseNavigationFragment {

	public static final String CHECKIN_SUCCESSFUL = "broadcast checkin successful";
	public static final String CHECKIN_FAILED = "checkin failed";

	private List<Checkin> mCheckins;
	private ListView mListView;
	private ItemAdapter mAdapter;
	private CheckinsDataUpdateReceiver mDataUpdateReceiver;
	private LoadingView mLoadingView;
	private String mUserId;
	private boolean isFirstDataRequest;

	private class CheckinsDataUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			Logger.i(CheckinsFragment.class.getSimpleName() + "." + CheckinsDataUpdateReceiver.class.getSimpleName());

			if (CheckinsService.REFRESH_CHECKINS_DATA_SUCCESS.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + CheckinsService.REFRESH_CHECKINS_DATA_SUCCESS);

				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();

				getCheckins();
			}

			if (CheckinsService.REFRESH_CHECKINS_DATA_FAIL.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + "." + CheckinsService.REFRESH_CHECKINS_DATA_FAIL);
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();

				if(getActivity()!=null){					
					OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.checkins_could_not_be_retrieved));
				}
				

			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Logger.i("CheckinsFragment#onResume()");
		if (mDataUpdateReceiver == null) {
			mDataUpdateReceiver = new CheckinsDataUpdateReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(CheckinsService.REFRESH_CHECKINS_DATA_SUCCESS);
			intentFilter.addAction(CheckinsService.REFRESH_CHECKINS_DATA_FAIL);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			getActivity().registerReceiver(mDataUpdateReceiver, intentFilter);
		}
		startRefreshMenuItemAnimation();
		getCheckins();
	}

	@Override
	public void onPause() {
		super.onPause();
		Logger.i("CheckinsFragment#onPause()");
		if (mDataUpdateReceiver != null) {
			getActivity().unregisterReceiver(mDataUpdateReceiver);
			// An activity can be resumed after pausing, so the activity may not
			// register the listener in the second call to onResume.
			mDataUpdateReceiver = null;
		}
	}

	public static final CheckinsFragment newInstance(String objectId) {
		CheckinsFragment f = new CheckinsFragment();
		Bundle bundle = new Bundle();
		Logger.i(CheckinsFragment.class.getSimpleName() + "#newInstance: objectId:" + objectId);
		bundle.putString(Constants.OBJECT_ID_KEY, objectId);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Logger.i(Logger.getClassAndMethod());
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setHasOptionsMenu(true);
		isFirstDataRequest = true;
		prepareRefreshMenuItemAnimation();

		Bundle bundle = getArguments();
		mUserId = bundle.getString(Constants.OBJECT_ID_KEY);

		setTitle("Checkins");

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.image_list);
		mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);

		mCheckins = new ArrayList<Checkin>();

		mListView = (ListView) vg.findViewById(R.id.image_list_view);

		mAdapter = new ItemAdapter();

		mListView.setAdapter(mAdapter);

		return vg;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

				Intent intent = new Intent(getActivity(), PlaceProfileActivity.class);
				Log.i("jan9", Logger.getClassAndMethod() + "object id: " + mCheckins.get(position).getPlaceId());
				intent.putExtra(Constants.OBJECT_ID_KEY, mCheckins.get(position).getPlaceId());
				intent.putExtra(Constants.OBJECT_TITLE_KEY, mCheckins.get(position).getPlaceName());
				intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
				getActivity().startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

			}
		});
	}

	@Override
	public void onRefresh() {
		Logger.i(Logger.getClassAndMethod());

		getCheckinsFromFB();
	}

	private void getCheckins() {

		getCheckinsFromDb();

		if (isFirstDataRequest) {

			if (mCheckins.size() > 0) {
				mLoadingView.setVisibility(View.GONE);
			}
			getActivity().invalidateOptionsMenu();
			getCheckinsFromFB();
		} else {
			if (mCheckins.size() < 1) {
				if(getActivity()!=null){					
					OutputUtil.showCrouton(getActivity(), getActivity().getResources().getString(R.string.no_checkins_available));
				}
			}
			mLoadingView.setVisibility(View.GONE);
			getActivity().invalidateOptionsMenu();
		}
	}

	private void getCheckinsFromDb() {
		mCheckins.clear();

		Cursor c = ((FBClientApplication) getActivity().getApplication()).getCheckinsData().getCheckins();

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					Checkin checkin = new Checkin();
					checkin.setId(c.getString(c.getColumnIndex(CheckinsData.C_ID)));
					checkin.setFromId(c.getString(c.getColumnIndex(CheckinsData.C_FROM_ID)));
					checkin.setFromName(c.getString(c.getColumnIndex(CheckinsData.C_FROM_NAME)));

					checkin.setPlaceId(c.getString(c.getColumnIndex(CheckinsData.C_PLACE_ID)));
					checkin.setPlaceName(c.getString(c.getColumnIndex(CheckinsData.C_PLACE_NAME)));
					checkin.setCity(c.getString(c.getColumnIndex(CheckinsData.C_CITY)));
					checkin.setCountry(c.getString(c.getColumnIndex(CheckinsData.C_COUNTRY)));

					checkin.setMessage(c.getString(c.getColumnIndex(CheckinsData.C_MESSAGE)));
					checkin.setCreatedTime(c.getString(c.getColumnIndex(CheckinsData.C_CREATED_TIME)));
					checkin.setLongitude(c.getString(c.getColumnIndex(CheckinsData.C_LONGITUDE)));
					checkin.setLatitude(c.getString(c.getColumnIndex(CheckinsData.C_LATITUDE)));

					mCheckins.add(checkin);

				} while (c.moveToNext());
			}
		}
		if (c != null) {
			c.close();
		}

		mAdapter.notifyDataSetChanged();

	}

	private void getCheckinsFromFB() {
		Intent intent = new Intent(getActivity(), CheckinsService.class);
		intent.putExtra(Constants.OBJECT_ID_KEY, mUserId);
		getActivity().startService(intent);
	}

	class ItemAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView fromName;
			public ImageView fromPicture;
			public TextView placeName;
			public ImageView placePicture;
			public TextView createdTime;
			public TextView message;
		}

		@Override
		public int getCount() {
			if (mCheckins != null) {
				return mCheckins.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return mCheckins.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			View view = convertView;

			if (mCheckins != null) {
				final ViewHolder holder;
				Checkin checkin = mCheckins.get(position);

				if (convertView == null && getActivity() != null) {
					view = getActivity().getLayoutInflater().inflate(R.layout.checkin_item, null);

					holder = new ViewHolder();

					holder.fromName = (TextView) view.findViewById(R.id.fromName);
					configFromText(holder.fromName);

					holder.createdTime = (TextView) view.findViewById(R.id.createdTime);
					configTimeText(holder.createdTime);

					holder.fromPicture = (ImageView) view.findViewById(R.id.fromPicture);

					holder.placeName = (TextView) view.findViewById(R.id.placeName);
					configBodyText(holder.placeName);

					holder.message = (TextView) view.findViewById(R.id.message);
					configText(holder.message);

					view.setTag(holder);
				} else
					holder = (ViewHolder) view.getTag();

				holder.fromName.setText(checkin.getFromName());
				holder.message.setText(checkin.getMessage());
				holder.createdTime.setText(FacebookUtils.convertFacebookCreatedTimeToRelativeTime(checkin.getCreatedTime(), getActivity()));

				String place = checkin.getPlaceName() + (StringUtil.notEmpty(checkin.getCity()) ? ", " + checkin.getCity() : "")
						+ (StringUtil.notEmpty(checkin.getCountry()) ? ", " + checkin.getCountry() : "");
				holder.placeName.setText(place);

				String token = getApplication().getFBConnection().getFacebook().getAccessToken();
				String fromImageQuery = "https://graph.facebook.com/" + checkin.getFromId() + "/picture?access_token=" + token;

				getImageLoader().displayImage(fromImageQuery, holder.fromPicture, getImageDisplayOptions());

				String placeImageQuery = "https://graph.facebook.com/" + checkin.getPlaceId() + "/picture?access_token=" + token;

				getImageLoader().displayImage(placeImageQuery, holder.placePicture, getImageDisplayOptions());

				if (!StringUtil.notEmpty(checkin.getMessage())) {
					holder.message.setVisibility(View.GONE);
				}
			}

			return view;
		}
	}

}