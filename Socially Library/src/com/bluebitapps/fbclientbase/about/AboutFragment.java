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

package com.bluebitapps.fbclientbase.about;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseFragment;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

public class AboutFragment extends BaseNavigationFragment {

	public static final String STATE_ABOUT = "state_about";
	public static final String STATE_LICENSE = "state_license";

	private TextView mTextView;

	public static final AboutFragment newInstance(String state) {
		AboutFragment f = new AboutFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.STATE_KEY, state);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Logger.i(Logger.getClassAndMethod());
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		if (bundle != null) {
			setState(bundle.getString(Constants.STATE_KEY));
		}
		
		setTitle("About");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logger.i(Logger.getClassAndMethod());

		ViewGroup vg = null;

		if (getActivity() != null) {

			vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.about_app);

			mTextView = (TextView) vg.findViewById(R.id.aboutTextView);
/*
			if(getState().equals(STATE_ABOUT)){
				mTextView.setText(getString(R.string.aboutText));
			}else{
				mTextView.setText(getString(R.string.licenseText));
			}
	*/		
			mTextView.setMovementMethod(new ScrollingMovementMethod());
			mTextView.setMovementMethod(LinkMovementMethod.getInstance());

			Spanned sp;

			if (getState().equals(STATE_ABOUT)) {
				sp = Html.fromHtml(getString(R.string.aboutText));
			} else {
				sp = Html.fromHtml(getString(R.string.licenseText));
			}
			mTextView.setText(sp);
			
			//Linkify.addLinks(mTextView, Linkify.ALL);
		}

		return vg;
	}

	@Override
	public void onResume() {
		super.onResume();
		configText(mTextView);
	}
}