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

package com.bluebitapps.fbclientbase.photos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.albums.Album;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

public class ImageGridFragment extends BaseNavigationFragment {

	/*
	 * typed ArrayList instead of List for use as argument in
	 * Intent#putParcelableArrayListExtra method
	 */
	ArrayList<Photo> mPhotos;

	private String mAlbumId;
	private String mAlbumTitle;
	private LoadingView mLoadingView;

	private ImageAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		Bundle bundle = getArguments();
		mAlbumId = bundle.getString(Album.ALBUM_ID_KEY);
		mAlbumTitle = bundle.getString(Album.ALBUM_NAME_KEY);
		Log.i("jan9", "album id: " + mAlbumId);
		Log.i("jan9", "album title: " + mAlbumTitle);

		mPhotos = new ArrayList<Photo>();

		getPhotos();
	}

	@Override
	public void onRefresh() {
		getPhotos();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup vg = null;
		if (getActivity() != null) {

			vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.ac_image_grid);

			mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);

			GridView gridView = (GridView) vg.findViewById(R.id.gridview);
			mAdapter = new ImageAdapter();
			gridView.setAdapter(mAdapter);
			gridView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					startImageGalleryActivity(position);
				}
			});

		}
		return vg;
	}

	private void startImageGalleryActivity(int position) {
		Intent intent = new Intent(getActivity(), ImagePagerActivity.class);
		intent.putParcelableArrayListExtra("photos", mPhotos);
		intent.putExtra(Album.ALBUM_NAME_KEY, mAlbumTitle);
		intent.putExtra(Album.ALBUM_PHOTO_COUNT, mPhotos.size());
		intent.putExtra("position", position);
		intent.putExtra(BaseThemedActivity.CLEAR_TOP_ON_HOME_SELECTED, true);
		if (getActivity() != null) {
			getActivity().startActivity(intent);
			getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onResume()
	 */

	@Override
	public void onResume() {
		super.onResume();

		if (getActivity() != null && StringUtil.notEmpty(mAlbumTitle)) {
			getActivity().getActionBar().setTitle(mAlbumTitle);
		}

		if (mPhotos.size() > 0) {
			mLoadingView.setVisibility(View.GONE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onStop()
	 */

	public class ImageAdapter extends BaseAdapter {

		private class ViewHolder {
			ImageView image;
		}

		@Override
		public int getCount() {
			if (mPhotos != null) {
				return mPhotos.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return mPhotos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//
			View view = convertView;
			final ViewHolder holder;

			if (mPhotos != null) {

				if (convertView == null && getActivity() != null) {
					view = getActivity().getLayoutInflater().inflate(R.layout.item_grid_image, null);

					holder = new ViewHolder();
					holder.image = (ImageView) view.findViewById(R.id.image);
					view.setTag(holder);
				} else
					holder = (ViewHolder) view.getTag();

				getImageLoader().displayImage(mPhotos.get(position).getPicture(), holder.image, getImageDisplayOptions(), new SimpleImageLoadingListener() {
					@Override
					public void onLoadingComplete(Bitmap loadedImage) {
						try {
							Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_image_on_load_complete);
							holder.image.setAnimation(anim);
							anim.start();
						} catch (Exception e) {
							Logger.i(e.toString());
						}
					}
				});
			}

			return view;
		}
	}

	/* retrieving images from fb */

	private void getPhotos() {

		if (getActivity() == null) {
			return;
		}

		((FBClientApplication) getActivity().getApplication()).getFBConnection().getAsyncFacebookRunner().request(mAlbumId + "/photos", new PhotosRequestListener());
	}

	private class PhotosRequestListener implements RequestListener {

		ArrayList<Photo> asyncPhotos;

		@Override
		public void onComplete(String response, Object state) {
			try {

				asyncPhotos = new ArrayList<Photo>();

				final JSONObject dataJsonObject = new JSONObject(response);
				final JSONArray jsonArray = dataJsonObject.getJSONArray("data");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject obj = jsonArray.getJSONObject(i);
					Photo photo = Photo.fromJson(obj);
					asyncPhotos.add(photo);
				}

				if (getActivity() != null) {

					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {

							setItems();

							getActivity().invalidateOptionsMenu();

							if (mPhotos != null) {
								mPhotos = asyncPhotos;
								if (mAdapter != null) {
									mAdapter.notifyDataSetChanged();
								}
								if (mPhotos.size() > 0) {
								} else {
									OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.data_could_not_be_retrieved));
								}
							}

							String photoString = getResources().getString(R.string.photo_lowercase);
							String photosString = getResources().getString(R.string.photos_lowercase);
							String photoWord = mPhotos.size() == 1 ? photoString : photosString;
							String str = mPhotos.size() + " " + photoWord;
							getActivity().getActionBar().setSubtitle(str);

						}
					});
				}

			} catch (JSONException e) {
				Logger.i(ImageGridFragment.class.getSimpleName() + PhotosRequestListener.class.getSimpleName() + ": " + e.toString());
				OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);

			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(ImageGridFragment.class.getSimpleName() + PhotosRequestListener.class.getSimpleName() + ": " + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
		}

	}

	private void setItems() {

		if (mLoadingView != null) {
			mLoadingView.setVisibility(View.GONE);
		}

		if (mPhotos == null) {
			return;
		}

		if (mPhotos.size() < 1) {
			return;
		}

		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}
}