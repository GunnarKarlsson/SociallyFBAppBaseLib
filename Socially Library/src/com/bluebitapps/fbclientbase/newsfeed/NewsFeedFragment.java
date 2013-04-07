/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.newsfeed;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteMisuseException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.page.PageActivity;
import com.bluebitapps.fbclientbase.profile.ProfileActivity;
import com.bluebitapps.fbclientbase.statusupdate.PostStatusUpdateActivity;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;
import com.bluebitapps.utils.FacebookUtils;
import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * Must to be initialized with an intent with parcelable with key
 * Constants.USER_TYPE_KEY and value NewsFeedFragment.USER_TYPE.CURRENT_USER or
 * NewsFeedFragment.USER_TYPE.FRIEND.
 * 
 * @author Gunnar Karlsson
 * 
 */

public class NewsFeedFragment extends BaseNavigationFragment {

	protected static final int LIKE_CHANGE_RESULT = 16;

	private String mState;
	private String mUserId;
	private DataUpdateReceiver mDataUpdateReceiver;
	private PullToRefreshListView mListView;
	private List<NewsFeedItem> mNewsFeedItems;
	private ItemAdapter mAdapter;
	private LoadingView mLoadingView;
	private boolean isFirstDataRequest;
	private boolean willRefreshOnStart = true;

	public NewsFeedFragment() {
	}

	private class DataUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			Logger.i(NewsFeedFragment.class.getSimpleName() + "." + DataUpdateReceiver.class.getSimpleName());

			if (NewsFeedService.REFRESH_NEWSFEED_DATA_FAIL.equals(intent.getAction())) {
				Logger.i(NewsFeedFragment.class.getSimpleName() + "." + DataUpdateReceiver.class.getSimpleName() + "." + NewsFeedService.REFRESH_NEWSFEED_DATA_FAIL);
				OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.data_could_not_be_retrieved));

				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();
				mListView.onRefreshComplete();
				mAdapter.notifyDataSetChanged();
			}

			if (NewsFeedService.REFRESH_NEWSFEED_DATA.equals(intent.getAction())) {
				Logger.i(NewsFeedFragment.class.getSimpleName() + "." + DataUpdateReceiver.class.getSimpleName() + "." + NewsFeedService.REFRESH_NEWSFEED_DATA);

				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();

				getNewsFeed();
			}

			if (NewsFeedService.REFRESH_NEWSFEED_DATA_OLDER.equals(intent.getAction())) {
				Logger.i(NewsFeedFragment.class.getSimpleName() + "." + DataUpdateReceiver.class.getSimpleName() + "." + NewsFeedService.REFRESH_NEWSFEED_DATA_OLDER);

				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();

				getOlderNewsFeed();
			}
			if (NewsFeedService.REFRESH_WALL_DATA.equals(intent.getAction())) {
				Logger.i(NewsFeedFragment.class.getSimpleName() + "." + DataUpdateReceiver.class.getSimpleName() + "." + NewsFeedService.REFRESH_WALL_DATA);

				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();

				getNewsFeed();
			}
			if (NewsFeedService.REFRESH_WALL_DATA_OLDER.equals(intent.getAction())) {
				Logger.i(NewsFeedFragment.class.getSimpleName() + "." + DataUpdateReceiver.class.getSimpleName() + "." + NewsFeedService.REFRESH_WALL_DATA_OLDER);

				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();

				getOlderNewsFeed();
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public static final NewsFeedFragment newInstance(String userId, String state, String title, boolean refreshOnlyMenuFlag) {
		Logger.i(NewsFeedFragment.class.getSimpleName() + "#newInstance");
		Log.i("jan21", Logger.getClassAndMethod() + "userId " + userId);
		Logger.i("state: " + state);
		NewsFeedFragment f = new NewsFeedFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.STATE_KEY, state);
		bundle.putString(Constants.USER_ID_KEY, userId);
		bundle.putString(Constants.OBJECT_TITLE_KEY, title);
		bundle.putBoolean(FLAG_HAS_ONLY_REFRESH_MENU_ITEM_IN_ACTIONBAR, refreshOnlyMenuFlag);
		f.setArguments(bundle);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("jan17nf", Logger.getClassAndMethod());

		setRetainInstance(true);
		setHasOptionsMenu(true);
		isFirstDataRequest = true;
		prepareRefreshMenuItemAnimation();

		String title = "";
		if (getArguments() != null) {
			mUserId = getArguments().getString(Constants.USER_ID_KEY);
			mState = getArguments().getString(Constants.STATE_KEY);
			title = getArguments().getString(Constants.OBJECT_TITLE_KEY);
		}
		if (StringUtil.notEmpty(title)) {
			setTitle(title);
		} else {
			setTitle(getResources().getString(R.string.newsfeed_menu_item));
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i("jan17nf", Logger.getClassAndMethod());
		ViewGroup vg = null;

		if (getActivity() != null) {
			Log.i("jan17nf", Logger.getClassAndMethod() + "getActivity() != null");

			vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.item_list_pull_to_refresh);

			mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);

			mNewsFeedItems = new ArrayList<NewsFeedItem>();
			mAdapter = new ItemAdapter(this.getActivity());
			mListView = (PullToRefreshListView) vg.findViewById(R.id.pull_refresh_list);
			ListView lv = mListView.getRefreshableView();

			OnRefreshListener2<ListView> customListener = new OnRefreshListener2<ListView>() {

				@Override
				public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
					startNewsFeedService();
				}

				@Override
				public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

					startNewsfeedServiceForOlderPosts();
				}
			};

			mListView.setOnRefreshListener(customListener);
			lv.setAdapter(mAdapter);

		}
		return vg;
	}

	/*
	 * @Override public void onActivityCreated(Bundle savedInstanceState) {
	 * super.onActivityCreated(savedInstanceState); Log.i("jan17nf",
	 * Logger.getClassAndMethod()); getNewsFeed(); }
	 */

	@Override
	public void onResume() {
		super.onResume();
		Log.i("jan17nf", Logger.getClassAndMethod());

		if (getActivity() != null) {

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			if (prefs != null) {
				String willRefreshString = prefs.getString(getResources().getString(R.string.PREFS_NEWSFEED_REFRESH_KEY), "true");
				if ("false".equalsIgnoreCase(willRefreshString)) {
					willRefreshOnStart = false;
				} else {
					willRefreshOnStart = true;
				}
			}
		}

		if (mDataUpdateReceiver == null) {
			mDataUpdateReceiver = new DataUpdateReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(NewsFeedService.REFRESH_NEWSFEED_DATA);
			intentFilter.addAction(NewsFeedService.REFRESH_NEWSFEED_DATA_OLDER);
			intentFilter.addAction(NewsFeedService.REFRESH_WALL_DATA);
			intentFilter.addAction(NewsFeedService.REFRESH_WALL_DATA_OLDER);
			intentFilter.addAction(NewsFeedService.REFRESH_NEWSFEED_DATA_FAIL);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");

			if (getActivity() != null) {
				getActivity().registerReceiver(mDataUpdateReceiver, intentFilter);
			}
			getNewsFeed();
		}

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

	private void startNewsFeedService() {
		Logger.i(NewsFeedFragment.class.getSimpleName() + "#startNewsFeedService()");
		Logger.i("mUserId: " + mUserId);
		Logger.i("mState:" + mState);
		Intent intent = new Intent(getActivity(), NewsFeedService.class);
		intent.putExtra(Constants.USER_ID_KEY, mUserId);
		intent.putExtra(Constants.STATE_KEY, mState);
		if (getActivity() != null) {
			getActivity().startService(intent);
		}
	}

	private void startNewsfeedServiceForOlderPosts() {
		Log.i("nftest", Logger.getClassAndMethod());
		Intent intent = new Intent(getActivity(), NewsFeedService.class);
		intent.putExtra(Constants.USER_ID_KEY, mUserId);

		String createdTime = "";

		if (mNewsFeedItems.size() >= 1) {
			createdTime = mNewsFeedItems.get(mNewsFeedItems.size() - 1).getCreatedTime();
		}

		if (StringUtil.notEmpty(createdTime)) {

			Log.i("nftest", "createdTime: " + createdTime);
			intent.putExtra(Constants.CREATED_TIME_KEY, createdTime);
			intent.putExtra(Constants.STATE_KEY, mState);
			if (getActivity() != null) {
				prepareRefreshMenuItemAnimation();
				startRefreshMenuItemAnimation();
				getActivity().startService(intent);
			}
		}
	}

	private void getNewsFeed() {
		Log.i("jan17nf", Logger.getClassAndMethod());

		getNewsFeedFromDatabase();

		mListView.onRefreshComplete();

		mAdapter.notifyDataSetChanged();

		getActivity().invalidateOptionsMenu();

		if (isFirstDataRequest) {
			Log.i("jan17nf", Logger.getClassAndMethod() + "isFirstDataRequest == true");
			if (mNewsFeedItems.size() > 0) {
				mLoadingView.setVisibility(View.GONE);
			}
			if (getActivity() != null) {
				getActivity().invalidateOptionsMenu();
			}

			if (mNewsFeedItems.size() < 1) {
				startRefreshMenuItemAnimation();
				startNewsFeedService();
			} else {

				if (willRefreshOnStart) {
					Log.i("feb7", "startNewsFeedService");
					startRefreshMenuItemAnimation();
					startNewsFeedService();
				} else {
					Log.i("feb7", "stop animation");
					stopRefreshMenuItemAnimation();
				}
			}

		} else {
			Log.i("jan21", Logger.getClassAndMethod() + "isFirstDataRequest == false");
			if (mNewsFeedItems.size() < 1) {
				OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.no_newsfeed_posts_available));
			}
			mLoadingView.setVisibility(View.GONE);
			if (getActivity() != null) {
				getActivity().invalidateOptionsMenu();
			}
		}

	}

	private void getNewsFeedFromDatabase() {
		Cursor c;

		if (mState == Constants.STATE_PROFILE) {
			c = ((FBClientApplication) getActivity().getApplication()).getNewsFeedData(NewsFeedData.REQUEST_WALL_FROM_DB).getPostsByUserId(mUserId);
		} else {
			Logger.i("NewsFeedFragment#getNewsFeed: mUserId: " + mUserId);
			c = ((FBClientApplication) getActivity().getApplication()).getNewsFeedData(NewsFeedData.REQUEST_NEWSFEED_FROM_DB).getPostsByUserId(mUserId);
		}

		if (c != null) {
			try {
				if (c.moveToFirst()) {

					// Clear news feed.
					mNewsFeedItems.clear();

					do {
						NewsFeedItem item = new NewsFeedItem();
						item.set(c);
						mNewsFeedItems.add(item);

					} while (c.moveToNext());
				}

			} catch (SQLiteMisuseException e) {
				Log.i("jan21", NewsFeedFragment.class.getSimpleName() + "#getNewsFeed" + ": " + e.toString());
			} catch (SQLiteException e) {
				Log.i("jan21", NewsFeedFragment.class.getSimpleName() + "#getNewsFeed" + ": " + e.toString());
			}
		} else {
			return;
		}

		if (c != null) {

			c.close();
		}

	}

	private void getOlderNewsFeed() {

		getOlderNewsFeedFromDatabase();
		mListView.onRefreshComplete();
		mAdapter.notifyDataSetChanged();
	}

	private void getOlderNewsFeedFromDatabase() {
		Log.i("jan17nf", Logger.getClassAndMethod());
		Cursor c;
		if (mState == Constants.STATE_PROFILE) {
			c = ((FBClientApplication) getActivity().getApplication()).getNewsFeedData(NewsFeedData.REQUEST_WALL_OLDER_FROM_DB).getPostsByUserId(mUserId);
		} else {
			c = ((FBClientApplication) getActivity().getApplication()).getNewsFeedData(NewsFeedData.REQUEST_NEWSFEED_OLDER_FROM_DB).getPostsByUserId(mUserId);
		}

		if (c != null) {
			try {

				if (c.moveToFirst()) {

					do {
						NewsFeedItem item = new NewsFeedItem();
						item.set(c);
						mNewsFeedItems.add(item);

					} while (c.moveToNext());
				}
			} catch (SQLiteMisuseException e) {
				Logger.i(NewsFeedFragment.class.getSimpleName() + "#getNewsFeed" + ": " + e.toString());
				OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.older_posts_could_not_be_retrieved));
			} catch (SQLiteException e) {
				Logger.i(NewsFeedFragment.class.getSimpleName() + "#getNewsFeed" + ": " + e.toString());
				OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.older_posts_could_not_be_retrieved));
			}
		} else {
			return;
		}
		if (c != null) {

			c.close();
		}
	}

	private class ItemAdapter extends BaseAdapter {

		private static final int TYPE_ITEM_BASE = 0;
		private static final int TYPE_ITEM_PHOTO = 1;
		private static final int TYPE_ITEM_STATUS = 2;
		private static final int TYPE_MAX_COUNT = TYPE_ITEM_STATUS + 1;

		private Context context;

		private class ViewHolder {
			public TextView fromName;
			public TextView createdTime;
			public TextView message;
			public TextView name;
			public TextView description;
			public ImageView picture;
			public ImageView fromPicture;
			// public ProgressBar progressBar;
			public ViewGroup storyBox;
			// public ViewGroup actions;
			public Button likeButton;
			public Button commentsButton;
			public Button shareButton;
			public ImageView likesIcon;
			public TextView likesCount;
			public ImageView commentsIcon;
			public TextView commentsCount;
		}

		public ItemAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getItemViewType(int position) {

			String type = mNewsFeedItems.get(position).getType();

			if (type.equalsIgnoreCase(NewsFeedItem.NEWSFEED_ITEM_TYPE_PHOTO)) {
				return TYPE_ITEM_PHOTO;
			} else if (type.equalsIgnoreCase(NewsFeedItem.NEWSFEED_ITEM_TYPE_STATUS)) {
				return TYPE_ITEM_STATUS;
			} else {
				return TYPE_ITEM_BASE;
			}
		}

		@Override
		public int getViewTypeCount() {
			return TYPE_MAX_COUNT;
		}

		@Override
		public int getCount() {
			if (mNewsFeedItems != null) {
				return mNewsFeedItems.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return mNewsFeedItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			int type = getItemViewType(position);

			NewsFeedItem item = mNewsFeedItems.get(position);
			View view = convertView;

			switch (type) {

			case TYPE_ITEM_PHOTO:
				return getPhotoView(position, convertView, parent, item, view);

			case TYPE_ITEM_STATUS:
				return getStatusView(position, convertView, parent, item, view);

			default:
				return getLinkView(position, convertView, parent, item, view);
			}
		}

		private void setFromHeaderReferences(View view, ViewHolder holder) {
			holder.fromName = (TextView) view.findViewById(R.id.fromName);
			configFromText(holder.fromName);

			holder.createdTime = (TextView) view.findViewById(R.id.createdTime);
			configFromText(holder.createdTime);

			holder.fromPicture = (ImageView) view.findViewById(R.id.fromPicture);
		}

		private void setFromHeaderValues(NewsFeedItem item, ViewHolder holder) {
			holder.fromName.setText(Html.fromHtml(FacebookUtils.getFromStringForNewsFeedItem(item)));
			holder.createdTime.setText(Html.fromHtml(FacebookUtils.getCreatedTimeInNewsFeed(item, getActivity())));
			getImageLoader().displayImage(item.getProfilePicture(), holder.fromPicture, getImageDisplayOptions());
		}

		private void setActionFooterReferences(View view, ViewHolder holder) {
			holder.likeButton = (Button) view.findViewById(R.id.likeButton);
			holder.commentsButton = (Button) view.findViewById(R.id.commentButton);
			holder.shareButton = (Button) view.findViewById(R.id.shareButton);
			holder.likesCount = (TextView) view.findViewById(R.id.likeCount);
			holder.commentsCount = (TextView) view.findViewById(R.id.commentCount);
		}

		private void setActionFooterValues(final NewsFeedItem item, final ViewHolder holder, final int position) {

			Log.i("likes", "item.getLikesCount(): " + item.getLikesCount());

			if (item.getLikesCount() != null && Integer.parseInt(item.getLikesCount()) > 0) {
				holder.likesIcon.setVisibility(View.VISIBLE);
				holder.likesCount.setVisibility(View.VISIBLE);
				holder.likesCount.setText(item.getLikesCount());
			} else {
				holder.likesIcon.setVisibility(View.GONE);
				holder.likesCount.setVisibility(View.GONE);
			}

			if (item.getCommentsCount() != null && Integer.parseInt(item.getCommentsCount()) > 0) {
				holder.commentsIcon.setVisibility(View.VISIBLE);
				holder.commentsCount.setVisibility(View.VISIBLE);
				holder.commentsCount.setText(item.getCommentsCount());
			} else {
				holder.commentsIcon.setVisibility(View.GONE);
				holder.commentsCount.setVisibility(View.GONE);
			}

			if (StringUtil.notEmpty(item.getUserLikes()) && item.getUserLikes().equalsIgnoreCase(Constants.TRUE)) {
				holder.likeButton.setText(R.string.unlike_on_button);
			} else {
				holder.likeButton.setText(R.string.like_on_button);
			}

			holder.likeButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Logger.i(Logger.getClassAndMethod());

					FacebookUtils.handleLikeButtonClick(getActivity(), item, holder.likeButton, holder.likesCount, null, FacebookUtils.LIKE_CONTEXT_NEWSFEED_LIST);

				}
			});

			holder.commentsButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					displayNewsFeedItemDetails(position);
				}
			});

			holder.shareButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String link = mNewsFeedItems.get(position).getLink();
					Log.i("ae3", "link: " + link);
					if (getActivity() != null) {
						Intent intent = new Intent(getActivity(),PostStatusUpdateActivity.class);
						intent.putExtra(Constants.IS_SHARE_KEY, true);
						intent.putExtra(Constants.URL_KEY, link);
						intent.putExtra(Constants.OBJECT_TITLE_KEY, mNewsFeedItems.get(position).getFromName());
						intent.putExtra(Constants.OBJECT_ID_KEY, mNewsFeedItems.get(position).getFromId());
						intent.putExtra(Constants.OBJECT_SUBTITLE_KEY, mNewsFeedItems.get(position).getType());
						String token = getApplication().getFBConnection().getFacebook().getAccessToken();
						String imageUrl;

						if(mNewsFeedItems.get(position).getType().equalsIgnoreCase("status")){
							imageUrl = "https://graph.facebook.com/" + item.getFromId() + "/picture?width=100&height=100&access_token=" + token;						
						}else if(mNewsFeedItems.get(position).getType().equalsIgnoreCase("photo")){
							imageUrl = "https://graph.facebook.com/" + item.getObjectId() + "/picture?width=100&height=100&access_token=" + token;
						}else{
							imageUrl = mNewsFeedItems.get(position).getPicture();
						}
						
						intent.putExtra(Constants.OBJECT_IMAGE_URL_KEY, imageUrl);
						
						getActivity().startActivity(intent);
					}
				}
			});
		}

		private void displayNewsFeedItemDetails(int position) {

			Intent intent = new Intent(getActivity(), NewsFeedItemActivity.class);
			intent.putExtra("newsfeeditem", mNewsFeedItems.get(position));
			startActivityForResult(intent, NewsFeedFragment.LIKE_CHANGE_RESULT);
			getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}

		private View getLinkView(final int position, View convertView, ViewGroup parent, NewsFeedItem inItem, View view) {
			final ViewHolder holder;
			// Set references

			final NewsFeedItem item = inItem;

			if (convertView == null) {

				view = ((Activity) context).getLayoutInflater().inflate(R.layout.newsfeed_item, null);

				holder = new ViewHolder();

				setFromHeaderReferences(view, holder);

				holder.message = (TextView) view.findViewById(R.id.newsFeedItemMessage);
				configText(holder.message);

				holder.description = (TextView) view.findViewById(R.id.newsFeedItemDescription);
				configText(holder.description);
				holder.description.setClickable(true);

				holder.name = (TextView) view.findViewById(R.id.newsFeedItemName);
				configText(holder.name);

				holder.storyBox = (ViewGroup) view.findViewById(R.id.newsFeedItemPictureAndName);
				holder.picture = (ImageView) view.findViewById(R.id.newsFeedItemPicture);

				setActionFooterReferences(view, holder);

				holder.commentsIcon = (ImageView) view.findViewById(R.id.commentIconImage);

				holder.likesIcon = (ImageView) view.findViewById(R.id.likeIconImage);

				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			// Set values
			setFromHeaderValues(item, holder);

			if (StringUtil.notEmpty(item.getMessage())) {
				holder.message.setText(item.getMessage());
			} else {
				if (StringUtil.notEmpty(item.getStory())) {
					holder.message.setText(item.getStory());
				} else {
					holder.message.setText("");
					holder.message.setVisibility(View.GONE);
				}
			}

			holder.storyBox.setClickable(true);

			// Set story tags.
			if (StringUtil.notEmpty(item.getStoryTags())) {

				holder.message.setMovementMethod(LinkMovementMethod.getInstance());

				ArrayList<StoryTag> tags = NewsFeedItem.getStoryTagsFromJSON(item.getStoryTags());

				Spannable spans = (Spannable) holder.message.getText();

				for (final StoryTag tag : tags) {
					ClickableSpan clickSpan = new ClickableSpan() {

						@Override
						public void onClick(View widget) {

							handleLinkClicks(tag);

						}
					};
					int end = tag.getOffset() + tag.getLength();
					Logger.i("offset" + tag.getOffset());
					Logger.i("end: " + end);
					try {
						spans.setSpan(clickSpan, tag.getOffset(), end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					} catch (IndexOutOfBoundsException e) {
						// TODO Handle
					}
				}
			}

			String areNowFriendsString = getResources().getString(R.string.are_now_friends_forbidden_string);
			if (holder.message != null && holder.message.getText() != null && holder.message.getText().toString().contains(areNowFriendsString)) {
				holder.storyBox.setVisibility(View.GONE);
				holder.description.setVisibility(View.GONE);
			}

			String isGoingToAnEventString = getResources().getString(R.string.is_going_to_an_event_forbidden_string);
			if (holder.message != null && holder.message.getText() != null && holder.message.getText().toString().contains(isGoingToAnEventString)) {
				holder.storyBox.setVisibility(View.GONE);
			}

			/* Too long */

			if (StringUtil.notEmpty(item.getDescription())) {
				String desc = "";
				if (item.getDescription().length() > 1000) {
					desc = item.getDescription().substring(0, 1000);
					desc = desc + "...";
					holder.description.setText(desc);
				} else {
					holder.description.setText(item.getDescription());
				}
			}

			holder.name.setText(item.getName());

			getImageLoader().displayImage(item.getPicture(), holder.picture, getImageDisplayOptions());
			setActionFooterValues(item, holder, position);

			Linkify.addLinks(holder.name, Linkify.ALL);
			Linkify.addLinks(holder.message, Linkify.ALL);
			// Linkify.addLinks(holder.description, Linkify.ALL);

			final String url = item.getLink();

			holder.storyBox.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Logger.i(Logger.getClassAndMethod() + url);

					if (url == null)
						return;
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.addCategory(Intent.CATEGORY_BROWSABLE);
					intent.setData(Uri.parse(url));
					startActivity(intent);
				}
			});

			return view;

		}

		private View getStatusView(final int position, View convertView, ViewGroup parent, NewsFeedItem item, View view) {
			final ViewHolder holder;
			// Set references
			if (convertView == null) {

				view = ((Activity) context).getLayoutInflater().inflate(R.layout.newsfeeditem_status, null);
				holder = new ViewHolder();

				setFromHeaderReferences(view, holder);

				holder.message = (TextView) view.findViewById(R.id.newsFeedItemMessage);
				configText(holder.message);

				holder.message.setClickable(true);

				holder.commentsIcon = (ImageView) view.findViewById(R.id.commentIconImage);
				holder.likesIcon = (ImageView) view.findViewById(R.id.likeIconImage);

				setActionFooterReferences(view, holder);

				view.setTag(holder);
			} else
				holder = (ViewHolder) view.getTag();

			// Set values
			setFromHeaderValues(item, holder);

			// Logger.i("getStoryTags: " + item.getStoryTags());

			if (StringUtil.notEmpty(item.getStoryTags())) {
				ArrayList<StoryTag> tags = NewsFeedItem.getStoryTagsFromJSON(item.getStoryTags());
				Logger.i("tags: " + tags.toString());
			}

			if (StringUtil.notEmpty(item.getMessage())) {
				holder.message.setText(item.getMessage());
			}

			if (StringUtil.notEmpty(item.getStory())) {
				holder.message.setText(item.getStory());
			}

			setActionFooterValues(item, holder, position);

			Linkify.addLinks(holder.message, Linkify.ALL);

			if (StringUtil.notEmpty(item.getStoryTags())) {

				holder.message.setMovementMethod(LinkMovementMethod.getInstance());

				ArrayList<StoryTag> tags = NewsFeedItem.getStoryTagsFromJSON(item.getStoryTags());

				Spannable spans = (Spannable) holder.message.getText();
				for (final StoryTag tag : tags) {
					ClickableSpan clickSpan = new ClickableSpan() {
						@Override
						public void onClick(View widget) {

							handleLinkClicks(tag);

						}
					};
					int end = tag.getOffset() + tag.getLength();
					Logger.i("offset" + tag.getOffset());
					Logger.i("end: " + end);
					try {
						spans.setSpan(clickSpan, tag.getOffset(), end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					} catch (IndexOutOfBoundsException e) {

					}
				}
			}

			return view;
		}

		private View getPhotoView(final int position, View convertView, ViewGroup parent, NewsFeedItem item, View view) {
			final ViewHolder holder;

			if (convertView == null) {
				view = ((Activity) context).getLayoutInflater().inflate(R.layout.newsfeeditem_photo, null);
				holder = new ViewHolder();
				setFromHeaderReferences(view, holder);
				holder.message = (TextView) view.findViewById(R.id.newsFeedItemMessage);
				configText(holder.message);
				holder.storyBox = (ViewGroup) view.findViewById(R.id.newsFeedItemPictureAndName);
				holder.picture = (ImageView) view.findViewById(R.id.newsFeedItemPicture);
				holder.commentsIcon = (ImageView) view.findViewById(R.id.commentIconImage);
				holder.likesIcon = (ImageView) view.findViewById(R.id.likeIconImage);
				setActionFooterReferences(view, holder);
				view.setTag(holder);
			} else
				holder = (ViewHolder) view.getTag();

			setFromHeaderValues(item, holder);
			setActionFooterValues(item, holder, position);

			holder.storyBox.setClickable(true);
			holder.storyBox.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					displayNewsFeedItemDetails(position);
				}
			});

			// Logger.i("getStoryTags: " + item.getStoryTags());
			if (StringUtil.notEmpty(item.getStoryTags())) {
				ArrayList<StoryTag> tags = NewsFeedItem.getStoryTagsFromJSON(item.getStoryTags());
				Logger.i("tags: " + tags.toString());
			}

			if (StringUtil.notEmpty(item.getMessage())) {
				holder.message.setText(item.getMessage());
			}

			if (StringUtil.notEmpty(item.getStory())) {
				holder.message.setText(item.getStory());
			}
			String token = getApplication().getFBConnection().getFacebook().getAccessToken();

			boolean isTablet = getResources().getBoolean(R.bool.isTablet);

			String imageUrl;
			if (isTablet) {
				imageUrl = "https://graph.facebook.com/" + item.getObjectId() + "/picture?width=1000&height=1000&access_token=" + token;
			} else {
				imageUrl = "https://graph.facebook.com/" + item.getObjectId() + "/picture?width=400&height=400&access_token=" + token;
			}

			Log.i("url", imageUrl);
			getImageLoader().displayImage(imageUrl, holder.picture, getImageDisplayOptions());

			Linkify.addLinks(holder.message, Linkify.ALL);

			if (StringUtil.notEmpty(item.getStoryTags())) {

				holder.message.setMovementMethod(LinkMovementMethod.getInstance());

				ArrayList<StoryTag> tags = NewsFeedItem.getStoryTagsFromJSON(item.getStoryTags());

				Spannable spans = (Spannable) holder.message.getText();

				if (tags.size() > 0) {
					for (final StoryTag tag : tags) {
						ClickableSpan clickSpan = new ClickableSpan() {
							@Override
							public void onClick(View widget) {

								handleLinkClicks(tag);

							}
						};
						int end = tag.getOffset() + tag.getLength();
						Logger.i("offset" + tag.getOffset());
						Logger.i("end: " + end);
						try {
							spans.setSpan(clickSpan, tag.getOffset(), end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						} catch (IndexOutOfBoundsException e) {

						}
					}
				}
			}

			return view;
		}

	}

	private void handleLinkClicks(StoryTag tag) {
		Logger.i("tag name: " + tag.getName());
		Logger.i("tag id: " + tag.getUserId());
		Logger.i("tag type: " + tag.getType());

		if (tag.getType().toString().trim().equalsIgnoreCase("user")) {
			if (getActivity() != null) {
				/*
				 * Intent intent = new
				 * Intent(Constants.REQUEST_PROFILE_ACTIVITY);
				 * intent.putExtra(Constants.OBJECT_ID_KEY, tag.getUserId());
				 * intent.putExtra(Constants.OBJECT_TITLE_KEY, tag.getName());
				 * intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
				 * intent.putExtra(Constants.USER_TYPE_KEY,
				 * Constants.USER_TYPE_FRIEND);
				 * 
				 * getActivity().sendBroadcast(intent);
				 */

				Intent intent = new Intent(getActivity(), ProfileActivity.class);
				intent.putExtra(Constants.OBJECT_ID_KEY, tag.getUserId());
				intent.putExtra(Constants.OBJECT_TITLE_KEY, tag.getName());
				// intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
				getActivity().startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		} else if (tag.getType().toString().trim().equalsIgnoreCase("page")) {
			if (getActivity() != null) {

				Intent intent = new Intent(getActivity(), PageActivity.class);
				intent.putExtra(Constants.OBJECT_ID_KEY, tag.getUserId());
				intent.putExtra(Constants.OBJECT_TITLE_KEY, tag.getName());
				getActivity().startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		}
	}

	@Override
	protected void onRefresh() {
		Log.i("jan17nf", Logger.getClassAndMethod());
		startNewsFeedService();

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("jan17nf", Logger.getClassAndMethod());
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == NewsFeedItemActivity.RESULT_CODE_LIKES_CHANGE) {
			int likeChange = data.getIntExtra(NewsFeedItemActivity.LIKE_CHANGE, 0);
			String likeChangeObjectId = data.getStringExtra(NewsFeedItemActivity.LIKE_CHANGE_OBJECT_ID);
			String userLikes = data.getStringExtra(NewsFeedItemActivity.USER_LIKES_BOOLEAN);
			if (likeChange != 0) {
				for (int i = 0; i < mNewsFeedItems.size(); i++) {
					if (mNewsFeedItems.get(i).getId().equalsIgnoreCase(likeChangeObjectId)) {
						mNewsFeedItems.get(i).setLikesCount(Integer.toString(likeChange));
						mNewsFeedItems.get(i).setUserLikes(userLikes);
					}
				}
			}

			mAdapter.notifyDataSetChanged();
		}

		if (resultCode == NewsFeedItemActivity.RESULT_CODE_POSTED_COMMENT) {
			String postId = data.getStringExtra(Constants.OBJECT_ID_KEY);

			for (int i = 0; i < mNewsFeedItems.size(); i++) {
				if (mNewsFeedItems.get(i).getId().equalsIgnoreCase(postId)) {
					int commentsCount = Integer.parseInt(mNewsFeedItems.get(i).getCommentsCount());
					commentsCount++;
					mNewsFeedItems.get(i).setCommentsCount(Integer.toString(commentsCount));
				}
			}

			mAdapter.notifyDataSetChanged();

		}

	}

}