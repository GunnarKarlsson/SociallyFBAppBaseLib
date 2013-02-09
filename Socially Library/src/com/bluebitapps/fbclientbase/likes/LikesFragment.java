/* 
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.likes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bluebitapps.utils.InputUtil;
import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.page.PageActivity;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

public class LikesFragment extends BaseNavigationFragment {

	public static final String STATE_CURRENT_USER_LIKED_PAGES = "state current user liked pages";
	public static final String STATE_SEARCH = "state search";

	private List<LikedObject> mLikes;
	private ListView mListView;
	private ItemAdapter mAdapter;
	private LoadingView mLoadingView;
	private DataUpdateReceiver mDataUpdateReceiver;
	private String mSearchString;
	private boolean isFirstDataRequest;

	private class DataUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			Logger.i(Logger.getClassAndMethod());

			if (LikesService.REFRESH_LIKES_DATA_SUCCESS.equals(intent.getAction())) {
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					int likesCount = bundle.getInt(LikesService.LIKES_COUNT_KEY);
					Logger.i(Logger.getClassAndMethod() + "#likes: " + likesCount);
				}

				stopRefreshMenuItemAnimation();

				getLikes();
			}

			if (LikesService.REFRESH_LIKES_DATA_FAIL.equals(intent.getAction())) {
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();

				OutputUtil.showCrouton(getActivity(), "Data could not be retrieved");
			}
		}
	}

	public static final LikesFragment newInstance(String objectId, String state, String searchString) {

		LikesFragment f = new LikesFragment();
		Bundle bundle = new Bundle();
		Logger.i(Logger.getClassAndMethod() + " objectId:" + objectId);
		Logger.i(Logger.getClassAndMethod() + searchString);
		bundle.putString(Constants.OBJECT_ID_KEY, objectId);
		bundle.putString(Constants.STATE_KEY, state);
		bundle.putString(Constants.SEARCH_STRING, searchString);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setHasOptionsMenu(true);
		isFirstDataRequest = true;
		prepareRefreshMenuItemAnimation();

		if (getArguments() != null) {
			setObjectId(getArguments().getString(Constants.OBJECT_ID_KEY));
			Logger.i(Logger.getClassAndMethod() + " getObjectId(): " + getArguments().getString(Constants.OBJECT_ID_KEY));
			Logger.i(Logger.getClassAndMethod() + " getObjectId(): " + getObjectId());
			setState(getArguments().getString(Constants.STATE_KEY));
			mSearchString = getArguments().getString(Constants.SEARCH_STRING);
			Logger.i(Logger.getClassAndMethod() + " mSearchString: " + mSearchString);
		}

		setTitle("Likes");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup vg = null;

		if (getActivity() != null) {

			vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.image_list);

			mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);
			mLikes = new ArrayList<LikedObject>();
			mListView = (ListView) vg.findViewById(R.id.image_list_view);
			// TextView padding = new TextView(getActivity());
			// padding.setHeight(getResources().getDimensionPixelOffset(R.dimen.item_list_padding));
			// mListView.addHeaderView(padding);
			// mListView.addFooterView(padding);
			mAdapter = new ItemAdapter();
			mListView.setAdapter(mAdapter);
		}

		return vg;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Logger.i("LikesFragment#onActivityCreated$OnItemClickListener");
				if (getActivity() != null) {
					Intent intent = new Intent(getActivity(), PageActivity.class);
					intent.putExtra(Constants.OBJECT_ID_KEY, mLikes.get(position).getObjectId());
					intent.putExtra(Constants.OBJECT_TITLE_KEY, mLikes.get(position).getObjectName());
					intent.putExtra(Constants.USER_TYPE_KEY, Constants.USER_TYPE_CURRENT_USER);
					intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
					getActivity().startActivity(intent);
					getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				}
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();

		if (mDataUpdateReceiver == null) {
			mDataUpdateReceiver = new DataUpdateReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(LikesService.REFRESH_LIKES_DATA_SUCCESS);
			intentFilter.addAction(LikesService.REFRESH_LIKES_DATA_FAIL);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			if (getActivity() != null) {
				getActivity().registerReceiver(mDataUpdateReceiver, intentFilter);
			}
		}

		startRefreshMenuItemAnimation();
		getLikes();

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mDataUpdateReceiver != null && getActivity() != null) {
			getActivity().unregisterReceiver(mDataUpdateReceiver);
			// An activity can be resumed after pausing, so the activity may not
			// register the listener in the second call to onResume.
			mDataUpdateReceiver = null;
		}
	}

	@Override
	public void onRefresh() {
		Logger.i(Logger.getClassAndMethod());
		getLikesFromFB();
	}

	private void getLikes() {

		mLikes.clear();

		if (getState().equals(STATE_SEARCH)) {
			if (getActivity() != null) {
				ActionBar actionBar = getActivity().getActionBar();
				if (actionBar != null) {
					actionBar.setTitle("Search");
					actionBar.setSubtitle(null);
				}
			}
			// Crouton.makeText(getActivity(), "Searching for Pages...",
			// Style.INFO);
			doSearch();
		} else {

			getLikesFromDb();

			if (isFirstDataRequest) {

				if (mLikes.size() > 0) {
					mLoadingView.setVisibility(View.GONE);
				}
				if (getActivity() != null) {
					getActivity().invalidateOptionsMenu();
				}
				getLikesFromFB();
			} else {
				if (mLikes.size() < 1) {
					OutputUtil.showCrouton(getActivity(), "No likes items available");
				}
				mLoadingView.setVisibility(View.GONE);
				if (getActivity() != null) {
					getActivity().invalidateOptionsMenu();
				}
			}
		}
	}

	private void getLikesFromFB() {
		// OutputUtil.showCrouton(getActivity(), "Loading Liked Pages");
		Intent intent = new Intent(getActivity(), LikesService.class);
		Logger.i(Logger.getClassAndMethod() + " getObjectId " + getObjectId());
		intent.putExtra(Constants.OBJECT_ID_KEY, getObjectId());
		if (getActivity() != null) {
			getActivity().startService(new Intent(intent));
		}
	}

	private void doSearch() {
		Bundle params = new Bundle();
		String typeString = "page";
		params.putString("type", typeString);
		params.putString("q", mSearchString);
		String query = "search";
		FBClientApplication.getApplication().getFBConnection().getAsyncFacebookRunner().request(query, params, new SearchListener());
	}

	private class SearchListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {

			ArrayList<LikedObject> likes = new ArrayList<LikedObject>();
			try {

				final JSONObject dataJsonObject = new JSONObject(response);
				final JSONArray jsonArray = dataJsonObject.getJSONArray("data");

				Logger.i(Logger.getClassAndMethod() + "likes count: " + jsonArray.length());

				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject obj = jsonArray.getJSONObject(i);

						LikedObject likedObject = LikedObject.fromJSON(obj, null);
						if (StringUtil.notEmpty(likedObject.getObjectName())) {
							likes.add(likedObject);
						}
					}
				}

				final ArrayList<LikedObject> tempLikes = new ArrayList<LikedObject>(likes);

				if (tempLikes.size() < 1) {
					OutputUtil.showCrouton(getActivity(), "No search results found");
				} else {

					if (getActivity() != null) {

						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								mLikes = tempLikes;
								mAdapter.notifyDataSetChanged();
								mLoadingView.setVisibility(View.GONE);

								if (!getState().equals(STATE_SEARCH)) {
									String pageWord = mLikes.size() == 1 ? "page" : "pages";
									String str = mLikes.size() + " liked " + pageWord;
									getActivity().getActionBar().setSubtitle(str);
								}

							}
						});
					}
				}

			} catch (JSONException e) {
				Logger.i(Logger.getClassAndMethod() + e.toString());
				OutputUtil.showCrouton(getActivity(), "Search results could not be retrieved");
			}

		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			OutputUtil.showErrorGettingResultsMessage(getActivity(), mLoadingView);
		}

	}

	private void getLikesFromDb() {

		if (getActivity() == null) {
			return;
		}

		List<LikedObject> likes = new ArrayList<LikedObject>();

		Cursor c = ((FBClientApplication) getActivity().getApplication()).getLikesData().getLikesByUserId(getObjectId());

		if (c != null) {
			if (c.moveToFirst()) {
				do {
					String userId = c.getString(c.getColumnIndex(LikesData.C_USER_ID));
					String objectId = c.getString(c.getColumnIndex(LikesData.C_ID));
					String objectName = c.getString(c.getColumnIndex(LikesData.C_OBJECT_NAME));

					LikedObject like = new LikedObject();
					like.setUserId(userId);
					like.setObjectId(objectId);
					like.setObjectName(objectName);

					likes.add(like);

				} while (c.moveToNext());
			}
		}
		if (c != null) {

			c.close();
		}

		Log.i("jan12", "likes.size(): " + likes.size());

		mLikes.clear();
		mLikes = likes;
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	class ItemAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView text;
			public ImageView image;
		}

		@Override
		public int getCount() {
			if (mLikes != null) {
				return mLikes.size();
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

				if (convertView == null) {
					view = getActivity().getLayoutInflater().inflate(R.layout.image_list_item, null);

					holder = new ViewHolder();

					holder.text = (TextView) view.findViewById(R.id.titleText);
					configText(holder.text);

					holder.image = (ImageView) view.findViewById(R.id.image);

					view.setTag(holder);
				} else
					holder = (ViewHolder) view.getTag();

				holder.text.setText(mLikes.get(position).getObjectName());

				getImageLoader().displayImage(mLikes.get(position).getPicture(FBClientApplication.getApplication()), holder.image, getImageDisplayOptions());

			}
			return view;
		}
	}
}