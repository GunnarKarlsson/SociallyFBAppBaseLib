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

package com.bluebitapps.fbclientbase.albums;

import java.util.ArrayList;
import java.util.List;

import android.app.FragmentTransaction;
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
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.photos.ImageGridActivity;
import com.bluebitapps.fbclientbase.photos.ImageGridFragment;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

public class AlbumsFragment extends BaseNavigationFragment {

	private List<Album> mAlbums;
	private ListView mListView;
	private ItemAdapter mAdapter;
	private LoadingView mLoadingView;
	private DataUpdateReceiver mDataUpdateReceiver;
	private boolean isFirstDataRequest;

	private class DataUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			Logger.i(Logger.getClassAndMethod());

			if (AlbumsService.REFRESH_ALBUMS_DATA_SUCCESS.equals(intent.getAction())) {

				Logger.i(Logger.getClassAndMethod() + AlbumsService.REFRESH_ALBUMS_DATA_SUCCESS);

				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();

				getAlbums();
			}

			if (AlbumsService.REFRESH_ALBUMS_DATA_FAIL.equals(intent.getAction())) {

				Logger.i(Logger.getClassAndMethod() + AlbumsService.REFRESH_ALBUMS_DATA_FAIL);

				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();

				OutputUtil.showCrouton(getActivity(), "Albums could not be retrieved");

			}
		}
	}

	public static final AlbumsFragment newInstance(String objectId, String state, String title, boolean refreshOnlyMenuFlag) {
		AlbumsFragment f = new AlbumsFragment();
		Bundle bundle = new Bundle();
		Logger.i(AlbumsFragment.class.getSimpleName() + ".newInstance: objectId:" + objectId);
		Logger.i(AlbumsFragment.class.getSimpleName() + ".newInstance: state" + state);
		bundle.putString(Constants.OBJECT_ID_KEY, objectId);
		bundle.putString(Constants.OBJECT_TITLE_KEY, title);
		bundle.putString(Constants.STATE_KEY, state);
		

		bundle.putBoolean(FLAG_HAS_ONLY_REFRESH_MENU_ITEM_IN_ACTIONBAR, refreshOnlyMenuFlag);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setHasOptionsMenu(true);
		isFirstDataRequest = true;
		prepareRefreshMenuItemAnimation();

		setState(getArguments().getString(Constants.STATE_KEY));
		setObjectId(getArguments().getString(Constants.OBJECT_ID_KEY));
		Logger.i(Logger.getClassAndMethod() + "onCreate getObjectId(): " + getArguments().getString(Constants.OBJECT_ID_KEY));
		
		String title = getArguments().getString(Constants.OBJECT_TITLE_KEY);
		
		if(StringUtil.notEmpty(title)){
			setTitle(title);
		}else{
			setTitle("Albums");			
		}
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup vg = null;

		if (getActivity() != null) {

			vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.image_list);

			mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);
			mAlbums = new ArrayList<Album>();
			mListView = (ListView) vg.findViewById(R.id.image_list_view);
			//TextView padding = new TextView(getActivity());
			//padding.setHeight(getResources().getDimensionPixelOffset(R.dimen.item_list_padding));
			//mListView.addHeaderView(padding);
			//mListView.addFooterView(padding);
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
				startImageGrid(position);
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		if (mDataUpdateReceiver == null) {
			mDataUpdateReceiver = new DataUpdateReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(AlbumsService.REFRESH_ALBUMS_DATA_SUCCESS);
			intentFilter.addAction(AlbumsService.REFRESH_ALBUMS_DATA_FAIL);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			if (getActivity() != null) {
				getActivity().registerReceiver(mDataUpdateReceiver, intentFilter);
			}
		}

		startRefreshMenuItemAnimation();
		getAlbums();

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mDataUpdateReceiver != null) {
			if (getActivity() != null) {
				getActivity().unregisterReceiver(mDataUpdateReceiver);
				mDataUpdateReceiver = null;
			}
		}
	}

	@Override
	public void onRefresh() {
		Logger.i(Logger.getClassAndMethod());
		getAlbumsFromFB();
	}

	private void getAlbums() {

		getAlbumsFromDatabase();

		if (isFirstDataRequest) {

			if (mAlbums.size() > 0) {
				mLoadingView.setVisibility(View.GONE);
			}
			if (getActivity() != null) {
				getActivity().invalidateOptionsMenu();
			}
			getAlbumsFromFB();
		} else {
			if (mAlbums.size() < 1) {
				OutputUtil.showCrouton(getActivity(), "No albums available");
			}
			mLoadingView.setVisibility(View.GONE);
			if (getActivity() != null) {
				getActivity().invalidateOptionsMenu();
			}
		}

	}

	private void getAlbumsFromFB() {
		Logger.i(Logger.getClassAndMethod() + " getObjectId " + getObjectId());

		if (getActivity() != null) {
			Intent intent = new Intent(getActivity(), AlbumsService.class);
			intent.putExtra(Constants.OBJECT_ID_KEY, getObjectId());
			getActivity().startService(new Intent(intent));
		}
	}

	private void getAlbumsFromDatabase() {
		List<Album> albums = new ArrayList<Album>();

		if (getActivity() == null) {
			return;
		}

		Cursor c = ((FBClientApplication) getActivity().getApplication()).getAlbumsData().getAlbumsByUserId(getObjectId());

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					String id = c.getString(c.getColumnIndex(AlbumsData.C_ID));
					String name = c.getString(c.getColumnIndex(AlbumsData.C_NAME));
					String coverPhoto = c.getString(c.getColumnIndex(AlbumsData.C_COVER_PHOTO));
					String count = c.getString(c.getColumnIndex(AlbumsData.C_COUNT));
					String timestamp = c.getString(c.getColumnIndex(AlbumsData.C_UPDATED_TIME));

					Album album = new Album();
					album.setId(id);
					album.setName(name);
					album.setCoverPhoto(coverPhoto);
					album.setCount(count);
					album.setUpdatedTime(timestamp);

					albums.add(album);

				} while (c.moveToNext());
			}
		}
		if(c!=null){			
			c.close();
		}

		if (mAlbums != null) {
			mAlbums.clear();
			mAlbums = albums;
		}
		
		if(getActivity()!=null){
			String albumWord = mAlbums.size() == 1? "album":"albums";
			String str = mAlbums.size() + " " + albumWord;
			getActivity().getActionBar().setSubtitle(str);
		}

		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	private void startImageGrid(int position) {
		if (mAlbums != null && mAlbums.get(position) != null && mAlbums.get(position).getId() != null) {
			
			if(getActivity()!=null){
				//Intent intent = new Intent(Constants.STATE_IMAGE_GRID);
				Intent intent = new Intent(getActivity(), ImageGridActivity.class);
				Log.i("jan9", "album id: " + mAlbums.get(position).getId());
				Log.i("jan9", "album title: "+ mAlbums.get(position).getName());
				intent.putExtra(Album.ALBUM_ID_KEY, mAlbums.get(position).getId());
				intent.putExtra(Album.ALBUM_NAME_KEY, mAlbums.get(position).getName());
				intent.putExtra(ImageGridActivity.CLEAR_TOP_ON_HOME_SELECTED, true);
				getActivity().startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
			
		}
	}

	class ItemAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView title;
			public TextView photoCount;
			public TextView updatedTime;
			public ImageView image;
		}

		@Override
		public int getCount() {
			if (mAlbums != null) {
				return mAlbums.size();
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
			if (convertView == null && getActivity() != null) {

				view = getActivity().getLayoutInflater().inflate(R.layout.album_item, null);

				holder = new ViewHolder();
				holder.title = (TextView) view.findViewById(R.id.titleText);
				configText(holder.title);
				holder.photoCount = (TextView) view.findViewById(R.id.photoCount);
				holder.updatedTime = (TextView) view.findViewById(R.id.updatedTime);
				holder.image = (ImageView) view.findViewById(R.id.image);
				view.setTag(holder);

			} else
				holder = (ViewHolder)view.getTag();

			if (mAlbums != null) {
				holder.title.setText(mAlbums.get(position).getName());

				String countText = "";
				if (mAlbums.get(position).getCount() != null) {

					int count = Integer.parseInt(mAlbums.get(position).getCount());
					
					if (count > 1 || count < 1) {
						countText = " photos";
					} else {
						countText = " photos";
					}
					
					holder.photoCount.setText(mAlbums.get(position).getCount() + countText);
				}

				String updatedTime = (String) FacebookUtils.convertFacebookCreatedTimeToRelativeTime(mAlbums.get(position).getUpdatedTime());
				holder.updatedTime.setText("Last updated " + updatedTime);

				getImageLoader().displayImage(mAlbums.get(position).getCoverPhoto(), holder.image, getImageDisplayOptions());
			}

			return view;
		}
	}
}
