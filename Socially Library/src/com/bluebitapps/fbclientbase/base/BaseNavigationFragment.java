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

package com.bluebitapps.fbclientbase.base;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.MainActivity;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.checkins.CheckinLocationSelectionActivity;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.photos.UploadPhotoActivity;
import com.bluebitapps.fbclientbase.settings.TextSettingsActivity;
import com.bluebitapps.fbclientbase.settings.TextSettingsFragment;
import com.bluebitapps.fbclientbase.statusupdate.PostStatusUpdateActivity;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

/**
 * 
 * This class implements actions on the ActionBar: - a refresh action item. - a
 * common actions buttons that launches a menu with post actions (post photo
 * from gallery, take photo to post, post checkin, post status update).
 * 
 * To inherit functionality, override public void onRefresh() and do data
 * request from FB therein.
 * 
 * Inheriting Fragment needs to call getActivity().invalidateOptionsMenu() for
 * refresh animation to stop.
 * 
 * @author Gunnar Karlsson
 * 
 */

public class BaseNavigationFragment extends BaseFragment {

	public static final String FLAG_HAS_ONLY_REFRESH_MENU_ITEM_IN_ACTIONBAR = "flag has only refresh menu item in actionbar";
	private static final String URI_KEY = "uriKey";

	public static final int PICK_EXISTING_PHOTO_RESULT_CODE = 2;
	public static final int TAKE_PICTURE_WITH_CAMERA_RESULT_CODE = 4;

	private boolean mRefreshing;
	private Uri mFileUri;
	private MenuItem mRefreshMenuItem;

	private int mTextSize;
	private int mTextColor;
	private Typeface mTypeFace;

	private String title;

	private boolean hasOnlyRefreshMenuItemInActionBar = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			hasOnlyRefreshMenuItemInActionBar = getArguments().getBoolean(FLAG_HAS_ONLY_REFRESH_MENU_ITEM_IN_ACTIONBAR, false);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		OutputUtil.cancelAllCroutons(getActivity());

		if (getActivity() != null) {
			getActivity().getActionBar().setDisplayShowTitleEnabled(true);

			if (StringUtil.notEmpty(getTitle())) {
				getActivity().getActionBar().setTitle(getTitle());
			} else {
				getActivity().getActionBar().setTitle("");
			}

			getActivity().getActionBar().setSubtitle(null);
		}

		FBClientApplication app = FBClientApplication.getApplication();
		setTextSize(ThemeFactory.getFontSize(app));
		int color = ThemeFactory.getFontColor(app);
		setTextColor(getActivity().getResources().getColor(color));
		setTypeFace(ThemeFactory.getFontType(app));
	}

	@Override
	public void onPause() {
		super.onPause();
		OutputUtil.cancelAllCroutons(getActivity());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Logger.i(Logger.getClassAndMethod());
		menu.clear();
		super.onCreateOptionsMenu(menu, inflater);

		if (hasOnlyRefreshMenuItemInActionBar) {
			inflater.inflate(R.menu.actionbar_with_only_refresh_menu_item, menu);
		} else {
			if (FBClientApplication.getApplication().hasKindleFeatureSet()) {
				inflater.inflate(R.menu.actionbar_for_kindle, menu);
			} else {
				inflater.inflate(R.menu.actionbar_with_multiple_menu_items, menu);
			}
		}

		if (mRefreshMenuItem == null) {
			mRefreshMenuItem = menu.findItem(R.id.refreshMenuItem);
		}

		if (isRefreshing()) {
			menu.findItem(R.id.refreshMenuItem).setActionView(R.layout.action_bar_indeterminate_progress);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Logger.i(Logger.getClassAndMethod());
		super.onOptionsItemSelected(item);

		// Can't handle this in the activity since then onOptionsItemSelected in
		// the fragment doesn't get called.
		if (item.getItemId() == android.R.id.home && getActivity() != null) {
			getActivity().finish();
		}

		if (getActivity() != null && item.getTitle().toString().equalsIgnoreCase(getActivity().getResources().getString(R.string.refresh))) {
			setIsRefreshing(true);
			item.setActionView(R.layout.action_bar_indeterminate_progress);
			onRefresh();
			return true;
		} else if (getActivity() != null && item.getTitle().toString().equalsIgnoreCase(getActivity().getResources().getString(R.string.post_status_update_or_link))) {
			getActivity().startActivity(new Intent(getActivity(), PostStatusUpdateActivity.class));
			return true;
		} else if (getActivity() != null && item.getTitle().toString().equalsIgnoreCase(getActivity().getResources().getString(R.string.checkin))) {
			getActivity().startActivity(new Intent(getActivity(), CheckinLocationSelectionActivity.class));
			return true;
		} else if (getActivity() != null && item.getTitle().toString().equalsIgnoreCase(getActivity().getResources().getString(R.string.upload_from_gallery))) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			if (getActivity() != null) {
				getActivity().startActivityForResult(Intent.createChooser(intent, getActivity().getResources().getString(R.string.pick_gallery)), PICK_EXISTING_PHOTO_RESULT_CODE);
			}
			return true;
		} else if (getActivity() != null && item.getTitle().toString().equals(getActivity().getResources().getString(R.string.snapshot_and_upload))) {
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			ContentValues values = new ContentValues();
			values.put(MediaStore.Images.Media.TITLE, "IMG_" + timeStamp + ".jpg");

			Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			mFileUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			((BaseSlidingMenuActivity) getActivity()).setFileUri(mFileUri);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);

			startActivityForResult(intent, TAKE_PICTURE_WITH_CAMERA_RESULT_CODE);
			return true;
		} else if (getActivity() != null && item.getTitle().toString().equalsIgnoreCase(getActivity().getResources().getString(R.string.text_style))) {
			if (getActivity() != null) {
				// Kindle test crash ANFE
				try {
					TextSettingsFragment fragment = new TextSettingsFragment();
					android.app.FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
					ft.addToBackStack(null);
					ft.replace(android.R.id.content, fragment).commit();
				} catch (ActivityNotFoundException e) {
					// TODO: handle
				}
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Subclasses need to override onRefresh to implement action after user
	 * pressed Refresh button.
	 */

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putParcelable(URI_KEY, mFileUri);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {

			Uri fileUri = savedInstanceState.getParcelable(URI_KEY);
			if (fileUri != null) {
				mFileUri = fileUri;
			}
		}
	}

	/**
	 * Override this method in subclasses. Within, call method to retrieve data.
	 */
	protected void onRefresh() {

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case PICK_EXISTING_PHOTO_RESULT_CODE:

			if (resultCode == Activity.RESULT_OK) {

				Uri uri = null;

				if (data != null) {
					uri = data.getData();
					startUploadActivity(uri);
				}

			}
			break;

		case TAKE_PICTURE_WITH_CAMERA_RESULT_CODE:

			Logger.i(BaseNavigationFragment.class.getSimpleName() + "#onActivityResult(): " + TAKE_PICTURE_WITH_CAMERA_RESULT_CODE);
			if (resultCode == Activity.RESULT_OK) {

				startUploadActivity(mFileUri);

			}

			break;
		}
	}

	private void startUploadActivity(Uri uri) {
		if (getActivity() != null) {
			Intent intent = new Intent(getActivity(), UploadPhotoActivity.class);
			intent.putExtra(UploadPhotoActivity.IMAGE_URI_KEY, uri);
			startActivity(intent);
		}
	}

	public boolean isRefreshing() {
		return mRefreshing;
	}

	public void setIsRefreshing(boolean value) {
		this.mRefreshing = value;
	}

	public void prepareRefreshMenuItemAnimation() {
		setIsRefreshing(true);
	}

	public void startRefreshMenuItemAnimation() {

		if (getActivity() != null) {

			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					getActivity().invalidateOptionsMenu();
				}
			});
		}
	}

	public void stopRefreshMenuItemAnimation() {
		setIsRefreshing(false);
		if (getActivity() != null) {

			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					getActivity().invalidateOptionsMenu();

				}
			});

		}
	}

	private int getTextSize() {
		return mTextSize;
	}

	private void setTextSize(int textSize) {
		this.mTextSize = textSize;
	}

	private int getTextSizeForBody() {
		int textSize = (int) Math.round(getTextSize() * 0.9);

		if (textSize > 20) {
			textSize = 20;
		} else if (textSize < 10) {
			textSize = 10;
		}

		return textSize;

	}

	private int getTextSizeForFromName() {
		int textSize = (int) Math.round(getTextSize() * 0.8);
		if (textSize > 20) {
			textSize = 20;
		} else if (textSize < 10) {
			textSize = 10;
		}
		return textSize;
	}

	private int getTextSizeForTimeStamp() {
		int textSize = (int) Math.round(getTextSize() * 0.8);
		return Math.min(Math.max(textSize, 8), 18);
	}

	private int getTextColor() {
		return mTextColor;
	}

	public void setTextColor(int textColor) {
		this.mTextColor = textColor;
	}

	private Typeface getTypeFace() {
		return mTypeFace;
	}

	public void setTypeFace(Typeface typeFace) {
		this.mTypeFace = typeFace;
	}

	public void configText(TextView view) {
		view.setTextSize(getTextSize());
		view.setTextColor(getTextColor());
		view.setTypeface(getTypeFace());
	}

	public void configFromText(TextView view) {
		view.setTextSize(getTextSizeForFromName());
	}

	public void configTimeText(TextView view) {
		view.setTextSize(getTextSizeForTimeStamp());
	}

	public void configBodyText(TextView view) {
		view.setTextSize(getTextSizeForBody());
		view.setTextColor(getTextColor());
		view.setTypeface(getTypeFace());
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
