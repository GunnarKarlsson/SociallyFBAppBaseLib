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

package com.bluebitapps.fbclientbase.page;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;

public class PageFragment extends BaseNavigationFragment {

	private PageUpdateReceiver mReceiver;
	private LoadingView mLoadingView;
	private ViewGroup mRootView;
	private Page mPage;
	private ImageView mProfilePhoto;
	private boolean isFirstDataRequest;

	private class PageUpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (PageService.REFRESH_PAGE_DATA_SUCCESS.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + PageService.REFRESH_PAGE_DATA_SUCCESS);
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();

				getPage();
			}

			if (PageService.REFRESH_PAGE_DATA_FAIL.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + PageService.REFRESH_PAGE_DATA_FAIL);
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();

				OutputUtil.showCrouton(getActivity(), "Data could not be refreshed");
			}
		}
	}

	public static final PageFragment newInstance(String objectId, String title) {
		PageFragment f = new PageFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.OBJECT_ID_KEY, objectId);
		bundle.putString(Constants.OBJECT_TITLE_KEY, title);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mReceiver == null) {
			mReceiver = new PageUpdateReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(PageService.REFRESH_PAGE_DATA_SUCCESS);
			intentFilter.addAction(PageService.REFRESH_PAGE_DATA_FAIL);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			if (getActivity() != null) {
				getActivity().registerReceiver(mReceiver, intentFilter);
			}
		}
		getPage();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		isFirstDataRequest = true;
		
		Bundle bundle = getArguments();
		setObjectId(bundle.getString(Constants.OBJECT_ID_KEY));
		String title = bundle.getString(Constants.OBJECT_TITLE_KEY);
		if(StringUtil.notEmpty(title)){
			setTitle(title);
		}

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.page_details);
		mRootView = (ViewGroup) vg.findViewById(R.id.contentRoot);
		mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);
		mProfilePhoto = (ImageView) vg.findViewById(R.id.profilePhoto);
		mRootView.setVisibility(View.GONE);
		mLoadingView.setVisibility(View.VISIBLE);

		return vg;
	}

	@Override
	public void onRefresh() {
		getPageFromFB();
	}

	private void getPage() {
		getPageFromDatabase();


		if (isFirstDataRequest) {

			if (mPage != null && StringUtil.notEmpty(mPage.getId())) {
				mLoadingView.setVisibility(View.GONE);
				setLayout();
			}

			//getActivity().invalidateOptionsMenu();
			getPageFromFB();
			
		} else {
			
			mLoadingView.setVisibility(View.GONE);

			if (mPage != null && StringUtil.notEmpty(mPage.getId())) {
				setLayout();
			}else{
				OutputUtil.showCrouton(getActivity(), "Page profile could not be retrieved");				
			}
			
		}
	}

	private void getPageFromDatabase() {
		FBClientApplication app = FBClientApplication.getApplication();
		PageData data = app.getPageData();
		Cursor c = data.getPageById(getObjectId());
		Page page = new Page();

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					page.setCoverPhoto(c.getString(c.getColumnIndex(PageData.C_COVER_PHOTO)));
					page.setDescription(c.getString(c.getColumnIndex(PageData.C_DESCRIPTION)));
					page.setFanCount(c.getString(c.getColumnIndex(PageData.C_FAN_COUNT)));
					page.setGeneralInfo(c.getString(c.getColumnIndex(PageData.C_GENERAL_INFO)));
					page.setId(c.getString(c.getColumnIndex(PageData.C_ID)));
					page.setName(c.getString(c.getColumnIndex(PageData.C_NAME)));
					page.setProfilePic(c.getString(c.getColumnIndex(PageData.C_PROFILE_PIC)));
					page.setProfilePic(c.getString(c.getColumnIndex(PageData.C_TALKING_ABOUT)));

				} while (c.moveToNext());
			}
		}

		mPage = page;
		
		
		
		if (c != null) {
			c.close();
		}

	}

	private void getPageFromFB() {
		if(getActivity()==null){
			return;
		}
		
		Intent intent = new Intent(getActivity(), PageService.class);
		intent.putExtra(Constants.OBJECT_ID_KEY, getObjectId());
		getActivity().startService(intent);

	}

	private void setLayout() {
		if (StringUtil.notEmpty(mPage.getName())) {
			TextView name = (TextView) mRootView.findViewById(R.id.nameValue);
			name.setText(mPage.getName());
			configText(name);
		} else {
			((TextView) mRootView.findViewById(R.id.nameValue)).setVisibility(View.GONE);
		}

		String token = getApplication().getFBConnection().getFacebook().getAccessToken();
		String query = "https://graph.facebook.com/" + mPage.getId() + "/picture?type=large&access_token=" + token;
		getImageLoader().displayImage(query, mProfilePhoto, getImageDisplayOptions());

		if (StringUtil.notEmpty(mPage.getDescription())) {
			TextView description = (TextView) mRootView.findViewById(R.id.descriptionValue);
			description.setText(mPage.getDescription());
			configBodyText(description);
			
		} else {
			((TextView) mRootView.findViewById(R.id.descriptionValue)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mPage.getFanCount())) {
			((TextView) mRootView.findViewById(R.id.likesCountValue)).setText(mPage.getFanCount());
		} else {
			((TableRow) mRootView.findViewById(R.id.likesCount)).setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mPage.getTalkingAbout())) {
			((TextView) mRootView.findViewById(R.id.talkingAboutValue)).setText(mPage.getTalkingAbout());
		} else {
			((TableRow) mRootView.findViewById(R.id.talkingAbout)).setVisibility(View.GONE);
		}

		mRootView.setVisibility(View.VISIBLE);
		mLoadingView.setVisibility(View.GONE);
	}
}