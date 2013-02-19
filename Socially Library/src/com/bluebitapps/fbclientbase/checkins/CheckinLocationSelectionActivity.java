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
import android.view.MenuItem;
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
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;

public class CheckinLocationSelectionActivity extends BaseThemedActivity implements OnItemClickListener {

	private Handler mHandler;
	private JSONObject location;

	private ListView placesList;
	private LocationManager mLocationManager;
	private MyLocationListener mLocationListener;

	private static JSONArray mJsonArray;

	private LoadingView mLoadingView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHandler = new Handler();
		location = new JSONObject();
		setContentView(R.layout.image_list);

		View view = (View) findViewById(R.id.root);
		setThemeAndConfigureActionBar(view);

		mLoadingView = (LoadingView) findViewById(R.id.loadingView);

	}

	@Override
	public void onResume() {
		super.onResume();
		getLocation();
	}

	public void getLocation() {
		/*
		 * launch a new Thread to get new location
		 */

		OutputUtil.showCrouton(CheckinLocationSelectionActivity.this, CheckinLocationSelectionActivity.this.getResources().getString(R.string.fetching_locations));

		new Thread() {
			@Override
			public void run() {
				Looper.prepare();

				if (mLocationManager == null) {
					mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
					new AlertDialog.Builder(CheckinLocationSelectionActivity.this).setTitle(R.string.enable_gps)
							.setMessage(R.string.enable_gps).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
								}
							}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									CheckinLocationSelectionActivity.this.finish();
								}
							}).show();
				}
				Looper.loop();
			}
		}.start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		/*
		 * User returning from the Location settings menu. try to fetch location
		 * again.
		 */
		getLocation();
	}

	/*
	 * Fetch nearby places by providing the search type as 'place' within 1000
	 * mtrs of the provided lat & lon
	 */
	private void fetchPlaces() {
		showLoadingView();
		/*
		 * Source tag: fetch_places_tag
		 */
		Bundle params = new Bundle();
		params.putString("type", "place");
		try {
			params.putString("center", location.getString("latitude") + "," + location.getString("longitude"));
		} catch (JSONException e) {
			OutputUtil.showCrouton(getParent(), "Locations could not be fetched.");
			hideLoadingView();
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

			hideLoadingView();

			try {
				mJsonArray = new JSONObject(response).getJSONArray("data");
				if (mJsonArray == null) {
					OutputUtil.showCrouton(CheckinLocationSelectionActivity.this, "Locations could not be fetched");
					return;
				}
			} catch (JSONException e) {
				Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + PlacesRequestListener.class.getSimpleName() + "." + e.toString());
				OutputUtil.showCrouton(CheckinLocationSelectionActivity.this, "Locations could not be fetched");
				return;
			}
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					placesList = (ListView) findViewById(R.id.image_list_view);
					placesList.setOnItemClickListener(CheckinLocationSelectionActivity.this);
					placesList.setAdapter(new PlacesListAdapter(CheckinLocationSelectionActivity.this));
				}
			});

		}

		public void onFacebookError(FacebookError e) {
			Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + PlacesRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showCrouton(CheckinLocationSelectionActivity.this, "Locations could not be fetched");
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + PlacesRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showCrouton(CheckinLocationSelectionActivity.this, "Locations could not be fetched");
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + PlacesRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showCrouton(CheckinLocationSelectionActivity.this, "Locations could not be fetched");
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + PlacesRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showCrouton(CheckinLocationSelectionActivity.this, "Locations could not be fetched");
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + PlacesRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showCrouton(CheckinLocationSelectionActivity.this, "Locations could not be fetched");
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {

		// pass longitude, latitude, placeName, placeId
		// mJsonArray.get(position).

		String name = null;
		String location = null;
		String latitude = null;
		String longitude = null;
		String id = null;

		try {
			JSONObject jsonObject = mJsonArray.getJSONObject(position);
			id = jsonObject.getString("id");
			name = jsonObject.getString("name");
			latitude = jsonObject.getJSONObject("location").getString("latitude");
			Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + ": latitude: " + latitude);
			longitude = jsonObject.getJSONObject("location").getString("longitude");
			Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + ": longitude: " + longitude);
		} catch (JSONException e) {
			Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "#onItemClick()" + "." + e.toString());
			OutputUtil.showCrouton(CheckinLocationSelectionActivity.this, "Location could not be selected");
		}

		Intent intent = new Intent(CheckinLocationSelectionActivity.this, PostCheckinActivity.class);
		intent.putExtra(PostCheckinActivity.PLACE_NAME_KEY, name);
		intent.putExtra(PostCheckinActivity.PLACE_ID_KEY, id);
		intent.putExtra(PostCheckinActivity.PLACE_LOCATION_KEY, location);
		intent.putExtra(PostCheckinActivity.LATITUDE_KEY, latitude);
		intent.putExtra(PostCheckinActivity.LONGITUDE_KEY, longitude);

		startActivity(intent);
	}

	private void hideLoadingView() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mLoadingView.setVisibility(View.GONE);
			}
		});
	}

	private void showLoadingView() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mLoadingView.setVisibility(View.VISIBLE);
			}
		});
	}

	/**
	 * Definition of the list adapter
	 */
	public class PlacesListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		// Places placesList;

		public PlacesListAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
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
			try {
				jsonObject = mJsonArray.getJSONObject(position);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
			}
			View view = convertView;
			if (convertView == null) {
				view = mInflater.inflate(R.layout.image_list_item, null);
				ViewHolder holder = new ViewHolder();
				holder.name = (TextView) view.findViewById(R.id.titleText);
				holder.image = (ImageView) view.findViewById(R.id.image);
				view.setTag(holder);
			}

			ViewHolder holder = (ViewHolder) view.getTag();
			try {
				if (jsonObject != null) {
					holder.name.setText(jsonObject.getString("name"));
				}
			} catch (JSONException e) {
				holder.name.setText("");
			}

			try {
				if (jsonObject != null) {
					String token = FBClientApplication.getApplication().getFBConnection().getFacebook().getAccessToken();
					String imageUrl = "https://graph.facebook.com/" + jsonObject.getString("id") + "/picture?access_token=" + token;
					getImageLoader().displayImage(imageUrl, holder.image, getImageDisplayOptions());
				}
			} catch (JSONException e) {

			}

			return view;
		}
	}

	class ViewHolder {
		TextView name;
		ImageView image;
	}

	class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location loc) {
			if (loc != null) {
				try {
					location.put("latitude", Double.valueOf(loc.getLatitude()));
					location.put("longitude", Double.valueOf(loc.getLongitude()));
				} catch (JSONException e) {
					Logger.i(CheckinLocationSelectionActivity.class.getSimpleName() + "." + MyLocationListener.class.getSimpleName() + "." + e.toString());
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
