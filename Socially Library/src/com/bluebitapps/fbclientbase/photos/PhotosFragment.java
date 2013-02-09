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
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bluebitapps.utils.OutputUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.albums.Album;
import com.bluebitapps.fbclientbase.base.BaseFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

public class PhotosFragment extends BaseFragment {

	List<Photo> mPhotos;
	Context mContext;
	GridView mGridView;
	ImageAdapter mAdapter;

	String mAlbumId;
	String mAlbumName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		Bundle bundle = getArguments();
		mAlbumId = bundle.getString(Album.ALBUM_ID_KEY);
		mAlbumName = bundle.getString(Album.ALBUM_NAME_KEY);

		Logger.i(Logger.getClassAndMethod() + "AlbumName: " + mAlbumName + ", id: " + mAlbumId);

		getPhotos();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup vg = null;
		
		if(getActivity()!=null){
			
		ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.image_grid);

		mPhotos = new ArrayList<Photo>();
		mGridView = (GridView) vg.findViewById(R.id.image_grid_view);
		mAdapter = new ImageAdapter();
		mGridView.setAdapter(mAdapter);

		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO action on click
			}
		});

		// mProgressBar = (ProgressBar) vg.findViewById(R.id.progressBar);
		}

		return vg;
	}

	private void getPhotos() {
		
		if(getActivity()==null){
			return;
		}

		((FBClientApplication) getActivity().getApplication()).getFBConnection().getAsyncFacebookRunner().request(mAlbumId + "/photos", new PhotosRequestListener());
	}

	public class ImageAdapter extends BaseAdapter {
		/*
		private class ViewHolder {
			public TextView text;
			public ImageView image;
		}
		*/

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
			return position;
		}

		// TODO: what should getItemId really return?

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final ImageView imageView;
			if (convertView == null && getActivity()!=null) {
				imageView = (ImageView) getActivity().getLayoutInflater().inflate(R.layout.image_grid_item, parent, false);
			} else {
				imageView = (ImageView) convertView;
			}

			getImageLoader().displayImage(mPhotos.get(position).getSource(), imageView, getImageDisplayOptions(), new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(Bitmap loadedImage) {
					Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_image_on_load_complete);
					imageView.setAnimation(anim);
					anim.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {

						}

						@Override
						public void onAnimationRepeat(Animation animation) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onAnimationEnd(Animation animation) {
							// TODO Auto-generated method stub
							imageView.setVisibility(View.VISIBLE);
						}
					});
					anim.start();
				}
			});

			return imageView;
		}
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
							mPhotos = asyncPhotos;
							setItems();
						}
					});
				}

			} catch (JSONException e) {
				Logger.i(PhotosFragment.class.getSimpleName() + "." + PhotosRequestListener.class.getSimpleName() + ": " + e.toString());
				OutputUtil.showCrouton(getActivity(), "Photos could not be retrieved");
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(PhotosFragment.class.getSimpleName() + "." + PhotosRequestListener.class.getSimpleName() + ": " + e.toString());
			OutputUtil.showCrouton(getActivity(), "Photos could not be retrieved");
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(PhotosFragment.class.getSimpleName() + "." + PhotosRequestListener.class.getSimpleName() + ": " + e.toString());
			OutputUtil.showCrouton(getActivity(), "Photos could not be retrieved");
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(PhotosFragment.class.getSimpleName() + "." + PhotosRequestListener.class.getSimpleName() + ": " + e.toString());
			OutputUtil.showCrouton(getActivity(), "Photos could not be retrieved");
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(PhotosFragment.class.getSimpleName() + "." + PhotosRequestListener.class.getSimpleName() + ": " + e.toString());
		}

	}

	private void setItems() {

		if (mPhotos == null || mPhotos.size() < 1) {
			//TODO fix logic for empty album message
			/*
			Toast toast = Toast.makeText(getActivity(), "Your photo album is empty", Toast.LENGTH_SHORT);

			toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);

			toast.show();
*/
		} else {

			mAdapter.notifyDataSetChanged();
		}
	}
}