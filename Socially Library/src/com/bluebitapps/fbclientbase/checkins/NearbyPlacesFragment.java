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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
import com.facebook.android.FacebookError;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseFragment;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.checkins.CheckinLocationSelectionActivity.MyLocationListener;
import com.bluebitapps.fbclientbase.checkins.CheckinLocationSelectionActivity.PlacesListAdapter;
import com.bluebitapps.fbclientbase.checkins.CheckinLocationSelectionActivity.PlacesRequestListener;
import com.bluebitapps.fbclientbase.checkins.CheckinLocationSelectionActivity.ViewHolder;
import com.bluebitapps.fbclientbase.checkins.CheckinsFragment.ItemAdapter;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.events.EventActivity;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.place.PlaceProfileActivity;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

public class NearbyPlacesFragment extends BaseNavigationFragment {

	private LocationManager mLocationManager;
	private MyLocationListener mLocationListener;
	private JSONObject mLocation;
	private static JSONArray mJsonArray;
	private ListView mListView;
	private LoadingView mLoadingView;
	private ArrayList<Checkin> mCheckins;
	private Handler mHandler;

	@Override
	public void onRefresh() {
		Logger.i(Logger.getClassAndMethod());
		getLocation();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setHasOptionsMenu(true);
		prepareRefreshMenuItemAnimation();

		mHandler = new Handler();
		mLocation = new JSONObject();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.image_list);
		mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);

		mCheckins = new ArrayList<Checkin>();
		mListView = (ListView) vg.findViewById(R.id.image_list_view);
		//TextView padding = new TextView(getActivity());
		//padding.setHeight(getResources().getDimensionPixelOffset(R.dimen.item_list_padding));
		//mListView.addHeaderView(padding);
		//mListView.addFooterView(padding);

		return vg;
	}

	@Override
	public void onResume() {
		super.onResume();
		getLocation();

		if (getActivity() != null) {
			getActivity().getActionBar().setTitle("Checkins");
			getActivity().getActionBar().setSubtitle("Nearby Locations");
		}
	}

	public void getLocation() {
		/*
		 * launch a new Thread to get new location
		 */
		//
		//OutputUtil.showCrouton(getActivity(), "Fetching locations...");

		new Thread() {
			@Override
			public void run() {
				Looper.prepare();

				if (mLocationManager == null) {
					mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
				}

				if (mLocationListener == null) {
					mLocationListener = new MyLocationListener();
				}

				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_COARSE);
				String provider = mLocationManager.getBestProvider(criteria, true);
				if (provider != null && mLocationManager.isProviderEnabled(provider)) {
					mLocationManager.requestLocationUpdates(provider, 1, 0, mLocationListener, Looper.getMainLooper());
				} else {
					/*
					 * GPS not enabled, prompt user to enable GPS in the
					 * Location menu
					 */
					new AlertDialog.Builder(getActivity()).setTitle("Enable GPS").setMessage("Enable GPS").setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO: message if user doesn;t turn on gps.
						}
					}).show();
				}
				Looper.loop();
			}
		}.start();
	}

	class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location loc) {
			if (loc != null) {
				try {
					mLocation.put("latitude", Double.valueOf(loc.getLatitude()));
					mLocation.put("longitude", Double.valueOf(loc.getLongitude()));
				} catch (JSONException e) {
					Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + MyLocationListener.class.getSimpleName() + "." + e.toString());
					stopRefreshMenuItemAnimation();
				}
				mLocationManager.removeUpdates(this);
				fetchPlaces();
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + MyLocationListener.class.getSimpleName() + "#onProviderDisabled");
		}

		@Override
		public void onProviderEnabled(String provider) {
			Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + MyLocationListener.class.getSimpleName() + "#onProviderEnabled");
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + MyLocationListener.class.getSimpleName() + "#onStatusChanged");
		}
	}

	private void fetchPlaces() {
		Bundle params = new Bundle();
		params.putString("type", "place");
		try {
			params.putString("center", mLocation.getString("latitude") + "," + mLocation.getString("longitude"));
		} catch (JSONException e) {
			OutputUtil.showCrouton(getActivity(), "Locations could not be fetched.");
			return;
		}
		params.putString("distance", "1000");
		FBClientApplication.getApplication().getFBConnection().getAsyncFacebookRunner().request("search", params, new PlacesRequestListener());
	}

	/*
	 * Callback after places are fetched.
	 */
	public class PlacesRequestListener implements RequestListener {

		@Override
		public void onComplete(final String response, final Object state) {
			Log.d("Facebook-FbAPIs", "Got response: " + response);

			try {
				mJsonArray = new JSONObject(response).getJSONArray("data");
				if (mJsonArray == null) {
					OutputUtil.showCrouton(getActivity(), "Locations could not be fetched");
					stopRefreshMenuItemAnimation();
					return;
				}
			} catch (JSONException e) {
				Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + PlacesRequestListener.class.getSimpleName() + "." + e.toString());
				OutputUtil.showCrouton(getActivity(), "Locations could not be fetched");
				stopRefreshMenuItemAnimation();
				return;
			}
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mListView.setAdapter(new PlacesListAdapter(getActivity()));
					mLoadingView.setVisibility(View.GONE);
					stopRefreshMenuItemAnimation();
				}
			});

		}

		public void onFacebookError(FacebookError e) {
			Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + PlacesRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
			stopRefreshMenuItemAnimation();
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + PlacesRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
			stopRefreshMenuItemAnimation();
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + PlacesRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
			stopRefreshMenuItemAnimation();
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + PlacesRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
			stopRefreshMenuItemAnimation();
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + PlacesRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
			stopRefreshMenuItemAnimation();
		}
	}

	public class PlacesListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		// Places placesList;

		public PlacesListAdapter(Context context) {
			if (context != null) {
				mInflater = LayoutInflater.from(context);
			}
		}

		@Override
		public int getCount() {
			if (mJsonArray != null) {
				return mJsonArray.length();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			JSONObject jsonObject = null;

			String placeId = "";
			String title = "";

			try {
				jsonObject = mJsonArray.getJSONObject(position);
				placeId = jsonObject.getString("id");
				title = jsonObject.getString("name");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
			}

			View view = convertView;

			if (convertView == null) {
				view = mInflater.inflate(R.layout.image_list_item, null);
				ViewHolder holder = new ViewHolder();
				holder.name = (TextView) view.findViewById(R.id.titleText);
				holder.image = (ImageView) view.findViewById(R.id.image);
				holder.container = (ViewGroup) view.findViewById(R.id.container);
				view.setTag(holder);
			}

			ViewHolder holder = (ViewHolder) view.getTag();
			if (StringUtil.notEmpty(placeId)) {
				String token = getApplication().getFBConnection().getFacebook().getAccessToken();
				String imageUrl = "https://graph.facebook.com/" + placeId + "/picture?access_token=" + token;
				getImageLoader().displayImage(imageUrl, holder.image, getImageDisplayOptions());
			}

			if (StringUtil.notEmpty(title)) {
				holder.name.setText(title);
				configText(holder.name);
			}

			final String fbid = placeId;
			final String name = title;

			holder.container.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (StringUtil.notEmpty(fbid)) {

						Intent intent = new Intent(getActivity(), PlaceProfileActivity.class);
						intent.putExtra(Constants.OBJECT_ID_KEY, fbid);
						Log.i("jan16", "fbid: " + fbid);
						intent.putExtra(Constants.OBJECT_TITLE_KEY, name);
						Log.i("jan16", "name: " + name);
						intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
						getActivity().startActivity(intent);
						getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
					}
				}
			});

			return view;
		}

	}

	class ViewHolder {
		TextView name;
		ImageView image;
		ViewGroup container;
	}

}