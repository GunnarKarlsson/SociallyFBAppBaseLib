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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.albums.Album;
import com.bluebitapps.fbclientbase.albums.AlbumsData;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.utils.AspectRatioImageView;
import com.bluebitapps.utils.ImageUtil;
import com.bluebitapps.utils.OutputUtil;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

public class UploadPhotoActivity extends BaseThemedActivity {

	public static final String IMAGE_URI_KEY = "image uri key";

	private Uri mImageUri;
	private Bitmap mBitmap;
	private EditText mCaptionEditText;
	private String mCaption = "";
	private Spinner mAlbumSpinner;
	private Spinner mPrivacySpinner;
	private ArrayList<Album> mAlbums = new ArrayList<Album>();
	private ItemAdapter mAdapter;
	private String mSelectedAlbumId = "";
	private boolean isFirstDataRequest = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_photo);

		View view = findViewById(R.id.root);
		setThemeAndConfigureActionBar(view);
		Logger.i(UploadPhotoActivity.class.getSimpleName() + "UploadPhotoActivity#onCreate()");

		mImageUri = (Uri) getIntent().getParcelableExtra(IMAGE_URI_KEY);

		mCaptionEditText = (EditText) findViewById(R.id.message);
		mAlbumSpinner = (Spinner) findViewById(R.id.albumSpinner);
		mPrivacySpinner = (Spinner) findViewById(R.id.privacySpinner);

		if (mImageUri == null)
			return;

		try {
			InputStream inputStream = FBClientApplication.getApplication().getContentResolver().openInputStream(mImageUri);
			InputStream inputStreamCopy = FBClientApplication.getApplication().getContentResolver().openInputStream(mImageUri);
			mBitmap = ImageUtil.decodeSampledBitmapFromInputStream(inputStream, inputStreamCopy, 100, 100);// BitmapFactory.decodeStream(is);
			inputStream.close();
			inputStreamCopy.close();

		} catch (FileNotFoundException e) {
			Logger.i(e.toString());
		} catch (IOException e) {
			Logger.i(e.toString());
		}

		if (mBitmap == null) {
			Crouton.makeText(this, "Error: Photo cound not be found", Style.ALERT);
		} else {

			try {
				Matrix matrix = new Matrix();

				float rotation = ImageUtil.rotationForImage(this, mImageUri);
				if (rotation != 0f) {
					matrix.preRotate(rotation);
				}

				Bitmap rotatedBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);

				AspectRatioImageView iv = (AspectRatioImageView) findViewById(R.id.image);
				iv.setImageBitmap(rotatedBitmap);
			} catch (Exception e) {
				// TODO handle
			}

		}

		InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

		if (imm != null) {
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		}

		getAlbumsFromDatabase();

	}

	private void getAlbumsFromDatabase() {

		ArrayList<Album> albums = new ArrayList<Album>();

		Cursor c = ((FBClientApplication) getApplication()).getAlbumsData().getAlbumsByUserId(FBClientApplication.getApplication().getFBConnection().getUserId());

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					String id = c.getString(c.getColumnIndex(AlbumsData.C_ID));
					String name = c.getString(c.getColumnIndex(AlbumsData.C_NAME));
					Log.i("feb6", "name: " + name);
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
		c.close();

		if (mAlbums != null) {
			mAlbums.clear();
			mAlbums = albums;
		}

		Log.i("feb7", "mAlbums.size: " + mAlbums.size());

		isFirstDataRequest = false;
		if (mAlbums.size() < 1) {

		}

		Album[] strArray = new Album[mAlbums.size()];
		mAlbums.toArray(strArray);

		ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, strArray);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAlbumSpinner.setAdapter(spinnerArrayAdapter);

		mAlbumSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { // TODO
																									// Auto-generated
																									// method
																									// stub
				Album album = (Album) parent.getItemAtPosition(position);
				mSelectedAlbumId = album.getId();
				Log.i("feb7", "album id: " + mSelectedAlbumId);
				Log.i("feb7", "album name: " + album.getName());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add("Post").setTitle("Post").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}

		if (item.getTitle().toString().equalsIgnoreCase("Post")) {

			String selection = mPrivacySpinner.getSelectedItem().toString();
			String privacySetting = "";

			Log.i("feb6", Logger.getClassAndMethod() + selection);

			if ("Everyone".equalsIgnoreCase(selection)) {
				privacySetting = "EVERYONE";
			} else if ("Friend of friends".equalsIgnoreCase(selection)) {
				privacySetting = "FRIENDS_OF_FRIENDS";
			} else if ("Myself".equalsIgnoreCase(selection)) {
				privacySetting = "SELF";
			} else {
				privacySetting = "ALL_FRIENDS";
			}

			OutputUtil.showCrouton(UploadPhotoActivity.this, "You'll get a notification when the upload is finished");
			Crouton.makeText(this, "Returning to previous screen...", Style.INFO).show();
			Intent intent = new Intent(this, UploadPhotoService.class);
			mCaption = mCaptionEditText.getText().toString();
			intent.putExtra(UploadPhotoService.CAPTION_KEY, mCaption);
			intent.putExtra(UploadPhotoService.PHOTO_URI_KEY, mImageUri);
			intent.putExtra(UploadPhotoService.ALBUM_ID_KEY, mSelectedAlbumId);
			intent.putExtra(UploadPhotoService.PRIVACY_SETTING_KEY, privacySetting);
			startService(new Intent(intent));

			Timer timer = new Timer();
			FinishActivityTask task = new FinishActivityTask();
			timer.schedule(task, 4000);

			return true;
		} else {
			return false;
		}
	}

	private void hideKeyBoard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
	}

	class FinishActivityTask extends TimerTask {

		@Override
		public void run() {
			hideKeyBoard();
			UploadPhotoActivity.this.finish();
		}

	}

	private static int exifToDegrees(int exifOrientation) {
		if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
			return 90;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
			return 180;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
			return 270;
		}
		return 0;
	}

	class ItemAdapter extends ArrayAdapter<Album> {

		private class ViewHolder {
			public TextView text;
			public ImageView image;
		}

		public ItemAdapter(Context context, int textViewResourceId) {
			super(UploadPhotoActivity.this, textViewResourceId, mAlbums);
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
		public Album getItem(int position) {
			return mAlbums.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			View view = convertView;
			final ViewHolder holder;

			if (convertView == null) {
				view = getLayoutInflater().inflate(android.R.layout.simple_spinner_dropdown_item, null);

				holder = new ViewHolder();

				holder.text = (TextView) view.findViewById(android.R.id.text1);

				view.setTag(holder);
			} else
				holder = (ViewHolder) view.getTag();

			holder.text.setText(mAlbums.get(position).getName());

			return view;
		}
	}
}
