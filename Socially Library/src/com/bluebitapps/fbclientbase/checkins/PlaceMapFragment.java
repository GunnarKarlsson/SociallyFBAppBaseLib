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

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseFragment;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.page.PageData;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

public class PlaceMapFragment extends BaseNavigationFragment {

	private WebView mWebView;
	private String mLatitude;
	private String mLongitude;

	public static final PlaceMapFragment newInstance(String objectId, String title) {
		PlaceMapFragment f = new PlaceMapFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.OBJECT_ID_KEY, objectId);
		bundle.putString(Constants.OBJECT_TITLE_KEY, title);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		if(getArguments()!=null){			
			setObjectId(getArguments().getString(Constants.OBJECT_ID_KEY));
			String title = getArguments().getString(Constants.OBJECT_TITLE_KEY);
			if(StringUtil.notEmpty(title)){
				setTitle(title);
			}
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.place_map);

		mWebView = (WebView) vg.findViewById(R.id.webView);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setSupportZoom(true);

		return vg;
	}

	@Override
	public void onResume() {
		super.onResume();
		getCoordinates();
	}
	
	private void getCoordinates(){
		FBClientApplication app = (FBClientApplication) getActivity().getApplication();
		PageData data = app.getPageData();
		Cursor c = data.getPageById(getObjectId());
		Logger.i(PlaceMapFragment.class.getSimpleName() + "#getProfile().getObjectId: " + getObjectId());

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					mLatitude = c.getString(c.getColumnIndex(PageData.C_LATITUDE));
					mLongitude = c.getString(c.getColumnIndex(PageData.C_LONGITUDE));

				} while (c.moveToNext());
			}
		}

		if (c != null) {
			c.close();
		}

		getMap();
	}
	
	private void getMap(){
		
		String url;
		
		if(mLatitude != null && mLongitude != null){			
			//example: url = "http://www.google.com/maps?q=37.423156,-122.084917";
			url = "http://www.google.com/maps?q=" + mLatitude + ","+mLongitude;
			OutputUtil.showCrouton(getActivity(), "Retrieving map...");
		}else{
			url = "http://www.google.com/maps";
			OutputUtil.showCrouton(getActivity(), "Location coordinates not available");
		}
		
		mWebView.loadUrl(url);
	}

}