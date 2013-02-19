/*******************************************************************************
 * Copyright 2012 Gunnar Karlsson.
 *******************************************************************************/

package com.bluebitapps.fbclientbase.subscriptions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bluebitapps.utils.InputUtil;
import com.bluebitapps.utils.OutputUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

public class SubscriptionsFragment extends BaseNavigationFragment {

	private static final String INSTANCE_STATE_KEY = "instance state key";

	private ArrayList<Subscription> mSubscriptions;
	private ListView mListView;
	private LoadingView mLoadingView;
	private ItemAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prepareRefreshMenuItemAnimation();

		mSubscriptions = new ArrayList<Subscription>();

		if (savedInstanceState != null) {
			mSubscriptions = savedInstanceState.getParcelableArrayList(INSTANCE_STATE_KEY);
		} else {
			getSubscriptions();
		}

		setHasOptionsMenu(true);

		setTitle("Subscriptions");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup vg = null;

		if (getActivity() != null) {

			vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.item_list);

			mListView = (ListView) vg.findViewById(R.id.list);
			mAdapter = new ItemAdapter();
			mListView.setAdapter(mAdapter);
			mListView.setVisibility(View.GONE);
			mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);

		}

		return vg;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		if (mSubscriptions != null && mSubscriptions.size() > 0) {
			outState.putParcelableArrayList(INSTANCE_STATE_KEY, mSubscriptions);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRefresh() {
		getSubscriptions();
	}

	private void getSubscriptions() {
		startRefreshMenuItemAnimation();
		final String query1 = "select subscribed_id from subscription where subscriber_id = me() ";
		final String query2 = "select name, uid from user where uid IN (select subscribed_id FROM #query1)";

		final JSONObject jsonQueries = new JSONObject() {
			{
				try {
					put("query1", query1);
					put("query2", query2);
				} catch (Exception e) {
					Logger.i(SubscriptionsFragment.class.getSimpleName() + ".jsonQueries." + e.toString());
				}
			}
		};

		Bundle params = new Bundle();
		params.putString("method", "fql.multiquery");
		Logger.i(Logger.getClassAndMethod() + jsonQueries.toString());
		params.putString("queries", jsonQueries.toString());
		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(null, params, new SubscriptionRequestListener());

	}

	private class SubscriptionRequestListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {

			final List<Subscription> subscriptions = new ArrayList<Subscription>();

			try {
				JSONArray a = new JSONArray(response);
				JSONObject subscriptionsObj = a.getJSONObject(0);
				JSONObject userNamesObj = a.getJSONObject(1);
				JSONArray subscriptionsJsonArray = subscriptionsObj.getJSONArray("fql_result_set");
				JSONArray userNamesJsonArray = userNamesObj.getJSONArray("fql_result_set");

				HashMap<String, String> userNamesMap = new HashMap<String, String>();

				if (userNamesJsonArray.length() > 0) {

					// Place user names in map
					for (int i = 0; i < userNamesJsonArray.length(); i++) {
						String uid = "";
						String name = "";
						if (userNamesJsonArray.getJSONObject(i).has("uid")) {
							uid = userNamesJsonArray.getJSONObject(i).getString("uid");
						}
						if (userNamesJsonArray.getJSONObject(i).has("name")) {
							name = userNamesJsonArray.getJSONObject(i).getString("name");
						}

						userNamesMap.put(uid, name);
					}
				}

				// Create friend request list an add names from map to list
				if (subscriptionsJsonArray.length() > 0) {
					for (int i = 0; i < subscriptionsJsonArray.length(); i++) {
						JSONObject obj = subscriptionsJsonArray.getJSONObject(i);
						Subscription subscription = Subscription.fromJSON(obj);

						if (userNamesMap.containsKey(subscription.getSubscribedId())) {
							String name = userNamesMap.get(subscription.getSubscribedId());
							subscription.setSubscribedName(name);
						}

						subscriptions.add(subscription);
					}
				}

			} catch (JSONException e) {
				Logger.i(Logger.getClassAndMethod() + e.toString());
				OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.data_could_not_be_retrieved));
				stopRefreshMenuItemAnimation();
			}

			if (getActivity() != null) {

				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {

						if (mSubscriptions != null) {

							mSubscriptions.clear();
							mSubscriptions.addAll(subscriptions);

							if (mSubscriptions.size() < 1) {
								OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.no_subscriptions_available));
							}

							String subscriptionString = getResources().getString(R.string.subscription_lowercase);
							String subscriptionsString = getResources().getString(R.string.subscriptions_lowercase);
							
							String subscriptionWord = mSubscriptions.size() == 1 ? subscriptionString : subscriptionsString;
							String str = mSubscriptions.size() + " " + subscriptionWord;
							getActivity().getActionBar().setSubtitle(str);
						}

						if (mListView != null) {
							mListView.setVisibility(View.VISIBLE);
						}

						if (mLoadingView != null) {
							mLoadingView.setVisibility(View.GONE);
						}

						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}

						if (getActivity() != null) {
							getActivity().invalidateOptionsMenu();
						}

						stopRefreshMenuItemAnimation();
					}
				});
			}

		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(SubscriptionsFragment.class.getSimpleName() + "." + SubscriptionRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.data_could_not_be_retrieved));
			stopRefreshMenuItemAnimation();

		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(SubscriptionsFragment.class.getSimpleName() + "." + SubscriptionRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.data_could_not_be_retrieved));
			stopRefreshMenuItemAnimation();
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(SubscriptionsFragment.class.getSimpleName() + "." + SubscriptionRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.data_could_not_be_retrieved));
			stopRefreshMenuItemAnimation();
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(SubscriptionsFragment.class.getSimpleName() + "." + SubscriptionRequestListener.class.getSimpleName() + "." + e.toString());
			OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.data_could_not_be_retrieved));
			stopRefreshMenuItemAnimation();
		}
	}

	class ItemAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView fromName;
			public ImageView fromPicture;
		}

		@Override
		public int getCount() {
			if (mSubscriptions != null) {
				return mSubscriptions.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			View view = convertView;
			final ViewHolder holder;

			if (getActivity() != null) {

				final Subscription subscription = mSubscriptions.get(position);
				if (convertView == null) {
					view = getActivity().getLayoutInflater().inflate(R.layout.friend_item, null);

					holder = new ViewHolder();
					holder.fromName = (TextView) view.findViewById(R.id.friendName);
					configText(holder.fromName);
					holder.fromPicture = (ImageView) view.findViewById(R.id.image);

					view.setTag(holder);
				} else
					holder = (ViewHolder) view.getTag();

				holder.fromName.setText(subscription.getSubscribedName());

				getImageLoader().displayImage(subscription.getProfilePicture(), holder.fromPicture, getImageDisplayOptions());

				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getActivity(), SubscriptionActivity.class);
						intent.putExtra(Constants.OBJECT_ID_KEY, subscription.getSubscribedId());
						intent.putExtra(Constants.USER_TYPE_KEY, Constants.USER_TYPE_FRIEND);
						intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
						getActivity().startActivity(intent);
					}
				});
			}

			return view;
		}
	}
}