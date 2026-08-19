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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.MainActivity;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

public class BaseThemedActivity extends Activity {

	public static final String CLEAR_TOP_ON_HOME_SELECTED = "clear top on home selected";

	private ImageLoader mImageLoader;
	private DisplayImageOptions mOptions;
	private String mThemeSelection;
	private int mTextColor;
	private int mTextSize;
	private Typeface mTypeface;
	private boolean clearTop = false;
	private boolean hasNoLoadingImage = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getIntent() != null) {
			Bundle bundle = getIntent().getExtras();
			if (bundle != null) {
				clearTop = bundle.getBoolean(CLEAR_TOP_ON_HOME_SELECTED);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		FBClientApplication app = FBClientApplication.getApplication();
		setTextSize(ThemeFactory.getFontSize(app));
		int color = ThemeFactory.getFontColor(app);
		setTextColor(getResources().getColor(color));
		setTypeFace(ThemeFactory.getFontType(app));
	}

	protected void setThemeAndConfigureActionBar(View rootView) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mThemeSelection = prefs.getString(Constants.THEME_PREFERENCES_KEY, Constants.THEME_DEFAULT);
		ThemeFactory.setActivityTheme(this, rootView, mThemeSelection);

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {

			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setBackgroundDrawable(ThemeFactory.getActionBarColorDrawable(mThemeSelection, this));
		
			if (Constants.THEME_IS_COLOR_PICKER_COLOR.equals(mThemeSelection)) {
				
				int color= prefs.getInt(Constants.COLOR_PICKER_CHOICE_SLIDING_MENU, 0x000000);
				actionBar.setStackedBackgroundDrawable(new ColorDrawable(color));
								
			} else {
				actionBar.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(ThemeFactory.getSlidingMenuColor(this, mThemeSelection))));
			}
		}
	}
	
	protected void configureActionBar(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mThemeSelection = prefs.getString(Constants.THEME_PREFERENCES_KEY, Constants.THEME_DEFAULT);

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {

			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setBackgroundDrawable(ThemeFactory.getActionBarColorDrawable(mThemeSelection, this));
		}
	}

	protected ImageLoader getImageLoader() {
		if (mImageLoader == null) {
			mImageLoader = ImageLoader.getInstance();
			mImageLoader.init(ImageLoaderConfiguration.createDefault(this));
		}
		return mImageLoader;
	}

	protected DisplayImageOptions getImageDisplayOptions() {

		if (mOptions == null) {
			if (hasNoLoadingImage()) {
				mOptions = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc().build();
			} else {
				mOptions = new DisplayImageOptions.Builder().showStubImage(R.drawable.default_image_background).cacheInMemory().cacheOnDisc().build();
			}
		}
		return mOptions;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			if (clearTop) {
				Intent intent = new Intent(this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				return true;

			} else {
				finish();
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				return true;
			}
		}
		return false;
	}

	public int getTextSize() {
		return mTextSize;
	}

	public void setTextSize(int textSize) {
		this.mTextSize = textSize;
	}

	public int getTextSizeForBody() {
		int textSize = (int) Math.round(getTextSize() * 0.9);

		if (textSize > 20) {
			textSize = 20;
		} else if (textSize < 10) {
			textSize = 10;
		}

		return textSize;

	}

	public int getTextSizeForFromName() {
		int textSize = (int) Math.round(getTextSize() * 0.8);
		if (textSize > 20) {
			textSize = 20;
		} else if (textSize < 10) {
			textSize = 10;
		}
		return textSize;
	}

	public int getTextSizeForTimeStamp() {
		int textSize = (int) Math.round(getTextSize() * 0.8);
		return Math.min(Math.max(textSize, 8), 18);
	}

	public int getTextColor() {
		return mTextColor;
	}

	public void setTextColor(int textColor) {
		this.mTextColor = textColor;
	}

	public Typeface getTypeFace() {
		return mTypeface;
	}

	public void setTypeFace(Typeface typeFace) {
		this.mTypeface = typeFace;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (getFragmentManager().getBackStackEntryCount() < 2) {
				this.finish();
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean hasNoLoadingImage() {
		return hasNoLoadingImage;
	}

	public void setHasNoLoadingImage(boolean noLoadingImage) {
		this.hasNoLoadingImage = noLoadingImage;
	}

}