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

package com.bluebitapps.fbclientbase.events;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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

public class BirthdaysFragment extends BaseNavigationFragment {

	private List<Birthday> mBirthdays;
	private ListView mListView;
	private ItemAdapter mAdapter;
	private BirthdayDataUpdateReceiver mDataUpdateReceiver;
	private LoadingView mLoadingView;
	private boolean isFirstDataRequest;

	private class BirthdayDataUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			Logger.i(Logger.getClassAndMethod());

			if (BirthdaysService.REFRESH_BIRTHDAYS_DATA_SUCCESS.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + BirthdaysService.REFRESH_BIRTHDAYS_DATA_SUCCESS);
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();

				getBirthdays();
			}
			if (BirthdaysService.REFRESH_BIRTHDAYS_DATA_FAIL.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + BirthdaysService.REFRESH_BIRTHDAYS_DATA_FAIL);
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();

				OutputUtil.showCrouton(getActivity(), "Birthdays could not be refreshed");
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mDataUpdateReceiver == null) {
			mDataUpdateReceiver = new BirthdayDataUpdateReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(BirthdaysService.REFRESH_BIRTHDAYS_DATA_SUCCESS);
			intentFilter.addAction(BirthdaysService.REFRESH_BIRTHDAYS_DATA_FAIL);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			if (getActivity() != null) {
				getActivity().registerReceiver(mDataUpdateReceiver, intentFilter);
			}
		}

		startRefreshMenuItemAnimation();

		getBirthdays();
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
	public void onCreate(Bundle savedInstanceState) {
		Logger.i(Logger.getClassAndMethod());
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setHasOptionsMenu(true);
		isFirstDataRequest = true;
		prepareRefreshMenuItemAnimation();

		mBirthdays = new ArrayList<Birthday>();

		setTitle("Birthdays");
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup vg = null;
		if (getActivity() != null) {

			vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.image_list);
			mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);
			mListView = (ListView) vg.findViewById(R.id.image_list_view);
			// TextView padding = new TextView(getActivity());
			// padding.setHeight(getResources().getDimensionPixelOffset(R.dimen.item_list_padding));
			// mListView.addHeaderView(padding);
			// mListView.addFooterView(padding);

			mAdapter = new ItemAdapter();

			mListView.setAdapter(mAdapter);

			mBirthdays = new ArrayList<Birthday>();
		}

		return vg;
	}

	@Override
	public void onRefresh() {

		Logger.i(Logger.getClassAndMethod());

		getBirthdaysFromFB();
	}

	private void getBirthdays() {

		getBirthdaysFromDatabase();

		if (getActivity() != null) {
			getActivity().invalidateOptionsMenu();
		}

		if (isFirstDataRequest) {

			if (mBirthdays.size() > 0) {
				mLoadingView.setVisibility(View.GONE);
			}

			if (getActivity() != null) {
				getActivity().invalidateOptionsMenu();
			}
			getBirthdaysFromFB();
		} else {
			if (mBirthdays.size() < 1) {
				OutputUtil.showCrouton(getActivity(), "No birthdays data available");
			}
			mLoadingView.setVisibility(View.GONE);

			if (getActivity() != null) {
				getActivity().invalidateOptionsMenu();
			}
		}

	}

	private void getBirthdaysFromDatabase() {

		if (getActivity() == null) {
			return;
		}
		mBirthdays.clear();

		Cursor c = ((FBClientApplication) getActivity().getApplication()).getBirthdaysData().getBirthdays();

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					Birthday bd = new Birthday();
					bd.set(c);

					mBirthdays.add(bd);

				} while (c.moveToNext());
			}
		}

		if (c != null) {
			c.close();
		}

		mAdapter.notifyDataSetChanged();
	}

	private void getBirthdaysFromFB() {
		if (getActivity() == null) {
			return;
		}
		getActivity().startService(new Intent(getActivity(), BirthdaysService.class));
	}

	class ItemAdapter extends BaseAdapter {

		private class ViewHolder {
			public ImageView picture;
			public TextView name;
			public TextView birthday;
			public ViewGroup container;
		}

		@Override
		public int getCount() {
			if (mBirthdays != null) {
				return mBirthdays.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return mBirthdays.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			View view = convertView;
			final ViewHolder holder;

			if (mBirthdays != null) {

				final Birthday birthday = mBirthdays.get(position);

				if (convertView == null && getActivity() != null) {
					view = getActivity().getLayoutInflater().inflate(R.layout.birthday_item, null);

					holder = new ViewHolder();
					holder.container = (ViewGroup) view.findViewById(R.id.rootView);
					holder.picture = (ImageView) view.findViewById(R.id.userPicture);

					holder.name = (TextView) view.findViewById(R.id.userName);
					configText(holder.name);

					holder.birthday = (TextView) view.findViewById(R.id.birthdayDate);
					configBodyText(holder.birthday);

					view.setTag(holder);
				} else
					holder = (ViewHolder) view.getTag();

				holder.container.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						Intent intent = new Intent(Constants.REQUEST_PROFILE_ACTIVITY);
						intent.putExtra(Constants.OBJECT_ID_KEY, mBirthdays.get(position).getUid());
						intent.putExtra(Constants.OBJECT_TITLE_KEY, mBirthdays.get(position).getName());
						intent.putExtra(Constants.USER_TYPE_KEY, Constants.USER_TYPE_FRIEND);

						if (getActivity() != null) {
							getActivity().sendBroadcast(intent);
						}
					}
				});

				holder.name.setText(mBirthdays.get(position).getName());

				if (StringUtil.notEmpty(birthday.getBirthdayDate())) {
					holder.birthday.setText(mBirthdays.get(position).getBirthdayDate());
				} else {
					holder.birthday.setText("Not available.");
				}
				// hack
				if (!holder.birthday.getText().toString().contains("/")) {
					holder.birthday.setText("Not available.");
				}

				String token = getApplication().getFBConnection().getFacebook().getAccessToken();

				String query = "https://graph.facebook.com/" + birthday.getUid() + "/picture?access_token=" + token;
				getImageLoader().displayImage(query, holder.picture, getImageDisplayOptions());
			}

			return view;
		}
	}

}