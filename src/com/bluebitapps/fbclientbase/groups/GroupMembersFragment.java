/* Copyright 2012 Gunnar Karlsson.
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

package com.bluebitapps.fbclientbase.groups;

import java.io.FileNotFoundException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseFragment;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.profile.ProfileActivity;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

//TODO: save state on orientation change

public class GroupMembersFragment extends BaseNavigationFragment {

	private static final String GROUP_ID_KEY = "group id key";

	private ArrayList<GroupMember> mMembers;
	private LoadingView mLoadingView;
	private ListView mListView;
	private ItemAdapter mAdapter;

	public static final GroupMembersFragment newInstance(String groupId, String title, boolean refreshOnlyMenuFlag) {
		GroupMembersFragment f = new GroupMembersFragment();
		Bundle bundle = new Bundle();
		bundle.putString(GroupMembersFragment.GROUP_ID_KEY, groupId);
		bundle.putString(Constants.OBJECT_TITLE_KEY, title);
		bundle.putBoolean(FLAG_HAS_ONLY_REFRESH_MENU_ITEM_IN_ACTIONBAR,refreshOnlyMenuFlag);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();
		String title = "";
		if (bundle != null) {
			setObjectId(bundle.getString(GroupMembersFragment.GROUP_ID_KEY));
			title = bundle.getString(Constants.OBJECT_TITLE_KEY);
		}

		if (StringUtil.notEmpty(title)) {
			setTitle(title);
		}

		setHasOptionsMenu(true);

		mMembers = new ArrayList<GroupMember>();

		getGroupMembers();

		prepareRefreshMenuItemAnimation();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup vg = null;

		if (getActivity() != null) {

			vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.image_list);
			mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);
			mListView = (ListView) vg.findViewById(R.id.image_list_view);
			mAdapter = new ItemAdapter();
			mListView.setAdapter(mAdapter);

		}
		return vg;
	}

	private void getGroupMembers() {

		startRefreshMenuItemAnimation();

		if (getActivity() == null) {
			return;
		}
		AsyncFacebookRunner runner = ((FBClientApplication) getActivity().getApplication()).getFBConnection().getAsyncFacebookRunner();
		runner.request(getObjectId() + "/members", new MemberListRequestListener());
	}

	@Override
	protected void onRefresh() {
		getGroupMembers();
	};
	
	private class MemberListRequestListener implements RequestListener {

		ArrayList<GroupMember> asyncMembers;

		@Override
		public void onComplete(String response, Object state) {
			try {

				asyncMembers = new ArrayList<GroupMember>();

				final JSONObject dataJsonObject = new JSONObject(response);
				final JSONArray jsonArray = dataJsonObject.getJSONArray("data");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject obj = jsonArray.getJSONObject(i);
					GroupMember groupMember = GroupMember.fromJSON(obj);
					asyncMembers.add(groupMember);
				}

				try {

					if (getActivity() != null) {

						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mMembers = asyncMembers;
								setItems();
								stopRefreshMenuItemAnimation();
							}
						});
					}

				} catch (Exception e) {
					Logger.i(Logger.getClassAndMethod() + e.toString());
					if(getActivity()!=null){						
						OutputUtil.showCrouton(getActivity(), getActivity().getResources().getString(R.string.data_not_available));
					}
					stopRefreshMenuItemAnimation();
				}

			} catch (JSONException e) {
				Logger.i(Logger.getClassAndMethod() + e.toString());
				if(getActivity()!=null){						
					OutputUtil.showCrouton(getActivity(), getActivity().getResources().getString(R.string.data_not_available));
				}
				stopRefreshMenuItemAnimation();
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			if(getActivity()!=null){						
				OutputUtil.showCrouton(getActivity(), getActivity().getResources().getString(R.string.data_not_available));
			}
			stopRefreshMenuItemAnimation();
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			if(getActivity()!=null){						
				OutputUtil.showCrouton(getActivity(), getActivity().getResources().getString(R.string.data_not_available));
			}
			stopRefreshMenuItemAnimation();
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			if(getActivity()!=null){						
				OutputUtil.showCrouton(getActivity(), getActivity().getResources().getString(R.string.data_not_available));
			}
			stopRefreshMenuItemAnimation();
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			if(getActivity()!=null){						
				OutputUtil.showCrouton(getActivity(), getActivity().getResources().getString(R.string.data_not_available));
			}
			stopRefreshMenuItemAnimation();
		}
	}

	private void setItems() {

		mLoadingView.setVisibility(View.GONE);

		if (mMembers == null || mMembers.size() < 1) {
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
		} else {
			mAdapter.notifyDataSetChanged();
		}
	}

	class ItemAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView name;
			public ImageView picture;
		}

		@Override
		public int getCount() {
			if (mMembers != null) {
				return mMembers.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return mMembers.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			View view = convertView;
			final ViewHolder holder;

			if (mMembers != null) {

				if (convertView == null && getActivity() != null) {
					view = getActivity().getLayoutInflater().inflate(R.layout.friend_item, null);

					holder = new ViewHolder();
					holder.picture = (ImageView) view.findViewById(R.id.image);
					holder.name = (TextView) view.findViewById(R.id.friendName);
					configText(holder.name);

					view.setTag(holder);
				} else
					holder = (ViewHolder) view.getTag();

				final GroupMember groupMember = mMembers.get(position);

				getImageLoader().displayImage(groupMember.getPicture(getApplication()), holder.picture, getImageDisplayOptions());

				holder.name.setText(groupMember.getName());

				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						Log.i("jan16", Logger.getClassAndMethod() + "onClick");

						Intent intent = new Intent(getActivity(), ProfileActivity.class);
						Log.i("jan16", groupMember.getName());
						Log.i("jan16", groupMember.getId());
						intent.putExtra(Constants.OBJECT_ID_KEY, groupMember.getId());
						intent.putExtra(Constants.OBJECT_TITLE_KEY, groupMember.getName());
						intent.putExtra(Constants.USER_TYPE_KEY, Constants.USER_TYPE_FRIEND);
						intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);

						if (getActivity() != null) {
							getActivity().startActivity(intent);
							getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
						}

					}
				});
			}

			return view;
		}
	}
}