/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.base;

import android.app.ActionBar.LayoutParams;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bluebitapps.utils.Admob;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.debug.Logger;

public class BaseFragment extends Fragment {

	private ImageLoader mImageLoader;
	private DisplayImageOptions mOptions;
	private FBClientApplication mApplication;
	private String mThemeSelection;
	private String mObjectId;
	private String mState;
	private Boolean hasNoLoadingImage = false;

	private AdView mAdView;

	protected void setAdView(AdView adView) {
		mAdView = adView;
	}

	public BaseFragment() {
	}

	// TODO: destroy AdView...

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Logger.i(BaseFragment.class.getSimpleName() + "#onCreate()");
		super.onCreate(savedInstanceState);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		mThemeSelection = prefs.getString(Constants.THEME_PREFERENCES_KEY, Constants.THEME_DEFAULT);

	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i("jan23", Logger.getClassAndMethod());
		if (getView() != null) {
			AdView adView = (AdView) getView().findViewById(R.id.adView);
			if (adView != null) {

				adView = new AdView(getActivity(), AdSize.SMART_BANNER, Admob.getId());
				AdRequest adRequest = new AdRequest();
				adView.loadAd(adRequest);
				if (Admob.isDebugging()) {
				}
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		AdView adView = (AdView) getView().findViewById(R.id.adView);
		if (adView != null) {

			RelativeLayout parent = (RelativeLayout) adView.getParent();
			ViewGroup.LayoutParams adViewParams = adView.getLayoutParams();
			parent.removeView(adView);
			adView.destroy();
			AdView newAdView = new AdView(getActivity(), AdSize.SMART_BANNER, Admob.getId());
			newAdView.setId(R.id.adView);
			parent.addView(newAdView, adViewParams);
			AdRequest adRequest = new AdRequest();
			newAdView.loadAd(new AdRequest());
		}
		Log.i("jan23", Logger.getClassAndMethod() + " config changed in fragment");
	}

	@Override
	public void onPause() {
		Log.i("jan23", Logger.getClassAndMethod());
		// TODO Auto-generated method stub
		super.onPause();
		// mAdView.destroy();
	}

	public ImageLoader getImageLoader() {
		if (mImageLoader == null) {
			mImageLoader = ImageLoader.getInstance();
			mImageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
		}
		return mImageLoader;
	}

	public DisplayImageOptions getImageDisplayOptions() {
		if (mOptions == null) {
			if (hasNoLoadingImage) {
				mOptions = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc().build();
			} else {
				mOptions = new DisplayImageOptions.Builder().showStubImage(R.drawable.default_image_background).cacheInMemory().cacheOnDisc().build();
			}
		}
		return mOptions;
	}

	public FBClientApplication getApplication() {
		if (mApplication == null) {
			mApplication = (FBClientApplication) FBClientApplication.getApplication();
		}
		return mApplication;
	}

	public String getThemeSelection() {
		return mThemeSelection;
	}

	public String getObjectId() {
		return mObjectId;
	}

	public void setObjectId(String objectId) {
		mObjectId = objectId;
	}

	public void setState(String state) {
		mState = state;
	}

	public String getState() {
		return mState;
	}

	public Boolean getHasNoLoadingImage() {
		return hasNoLoadingImage;
	}

	public void setHasNoLoadingImage(Boolean hasNoLoadingImage) {
		this.hasNoLoadingImage = hasNoLoadingImage;
	}
}