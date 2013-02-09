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

package com.bluebitapps.fbclientbase.newsfeed;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.actions.Comment;
import com.bluebitapps.fbclientbase.actions.CommentService;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.page.PageActivity;
import com.bluebitapps.fbclientbase.photos.TouchImageViewActivity;
import com.bluebitapps.fbclientbase.profile.ProfileActivity;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;
import com.bluebitapps.utils.EmoticonUtil;
import com.bluebitapps.utils.FacebookUtils;
import com.bluebitapps.utils.InputUtil;
import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
import com.bluebitapps.utils.WrappingSlidingDrawer;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

/**
 * Gets object id via intent. Fetches item data from database and displays item
 * data in header Fetches comments for item and displays them in list below
 * header. Comment list can be pulled down to get newer comments.
 * 
 * user's own comments can be entered into box and added to list. item can be
 * liked from like button. likeCount: when clicked shows dialog list with people
 * who liked it. CommentCount.
 * 
 * Each comment in the list can be liked.
 * 
 * @author Gunnar Karlsson
 * 
 */

public class NewsFeedItemActivity extends BaseThemedActivity {

	private static final int TYPE_ITEM_BASE = 0;
	private static final int TYPE_ITEM_PHOTO = 1;
	private static final int TYPE_ITEM_STATUS = 2;

	public static final String LIKE_CHANGE = "like change";
	public static final String LIKE_CHANGE_OBJECT_ID = "like change object id";

	public static final int RESULT_CODE_LIKES_CHANGE = 8;
	public static final int RESULT_CODE_POSTED_COMMENT = 16;

	public static final String USER_LIKES_BOOLEAN = "user likes boolean";

	public static final String USER_POSTED_COMMENT = "user posted comment";

	private ListView mListView;
	private List<Comment> mComments;
	private ItemAdapter mAdapter;
	private NewsFeedItem mItem;
	private ViewSwitcher mPrevCommentsView;
	private int mOffset;
	private int mOffsetIncrement = 16;
	private int mCommentCount;
	private Button mPostCommentButton;
	private EditText mPostCommentEditText;
	private CommentPostResultReceiver mReceiver;
	private boolean isAddingOwnPost = false;
	private RelativeLayout mPostFooter;

	private TextView mHeaderFromName;
	private TextView mHeaderCreatedTime;
	private TextView mHeaderMessage;
	private TextView mHeaderName;
	private TextView mHeaderDescription;
	private String mTitle;
	private String mSubTitle;

	// private HorizontalListView mEmoticonListView;
	private EmoticonAdapter mEmoticonAdapter;

	private GridView mEmoticonGrid;
	private boolean menuIsOpen = false;
	private boolean softkeyboardIsShown = false;
	private WrappingSlidingDrawer mSlidingDrawer;

	private class CommentPostResultReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(CommentService.COMMENT_POST_SUCCESS)) {
				Logger.i(Logger.getClassAndMethod() + CommentService.COMMENT_POST_SUCCESS);

				Intent i = new Intent(NewsFeedItemActivity.USER_POSTED_COMMENT);
				i.putExtra(Constants.OBJECT_ID_KEY, mItem.getId());
				((Activity) context).setResult(NewsFeedItemActivity.RESULT_CODE_POSTED_COMMENT, i);

				Crouton.makeText(NewsFeedItemActivity.this, "Your comment has been posted", Style.CONFIRM).show();
				isAddingOwnPost = true;
				mOffset = 0;
				mCommentCount++;
				getComments();
			}

			if (intent.getAction().equals(CommentService.COMMENT_POST_FAIL)) {
				Logger.i("intent received - fail posting action");
				Crouton.makeText(NewsFeedItemActivity.this, "Comment could not be posted", Style.ALERT).show();
			}
		}
	}

	@Override
	public void onResume() {

		super.onResume();

		if (getActionBar() != null) {
			getActionBar().setDisplayShowTitleEnabled(true);
			getActionBar().setTitle(mTitle);
			getActionBar().setSubtitle(mSubTitle);
		}

		if (mReceiver == null) {
			mReceiver = new CommentPostResultReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(CommentService.COMMENT_POST_SUCCESS);
			intentFilter.addAction(CommentService.COMMENT_POST_FAIL);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			registerReceiver(mReceiver, intentFilter);
		}

		if (mHeaderFromName != null && StringUtil.notEmpty(mItem.getFromName())) {
			mHeaderFromName.setTextSize(getTextSizeForFromName());
		}

		if (mHeaderCreatedTime != null && StringUtil.notEmpty(mItem.getCreatedTime())) {
			mHeaderCreatedTime.setTextSize(getTextSizeForTimeStamp());
		}

		// Message
		if (mHeaderMessage != null && StringUtil.notEmpty(mItem.getMessage())) {
			mHeaderMessage.setTextSize(getTextSize());

			mHeaderMessage.setTextColor(getTextColor());
			mHeaderMessage.setTypeface(getTypeFace());
		}

		// Story
		if (mHeaderMessage != null && StringUtil.notEmpty(mItem.getStory())) {
			mHeaderMessage.setTextSize(getTextSize());
			mHeaderMessage.setTextColor(getTextColor());
			mHeaderMessage.setTypeface(getTypeFace());
		}

		// Name
		if (mHeaderName != null && StringUtil.notEmpty(mItem.getName())) {
			mHeaderName.setTextSize(getTextSize());
			mHeaderName.setTextColor(getTextColor());
			mHeaderName.setTypeface(getTypeFace());
		}

		// Description
		if (mHeaderDescription != null && StringUtil.notEmpty(mItem.getDescription())) {
			mHeaderDescription.setTextSize(getTextSize());
			mHeaderDescription.setTextColor(getTextColor());
			mHeaderDescription.setTypeface(getTypeFace());
		}

		getComments();

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			// An activity can be resumed after pausing, so the activity may not
			// register the listener in the second call to onResume.
			mReceiver = null;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.newsfeeditem_activity);

		mOffset = 0;
		mComments = new ArrayList<Comment>();

		// Get NewsFeedItem.
		Bundle bundle = getIntent().getExtras();
		mItem = (NewsFeedItem) bundle.getParcelable("newsfeeditem");
		Log.i("nftest2", mItem.toString());
		mCommentCount = Integer.parseInt(mItem.getCommentsCount());

		String title = mItem.getName();

		if (!StringUtil.notEmpty(title)) {
			title = mItem.getMessage();
		}

		if (!StringUtil.notEmpty(title)) {
			title = mItem.getStory();
		}

		mTitle = title;
		mSubTitle = mItem.getFromName();

		// Set theme.
		View view = findViewById(R.id.container);
		setThemeAndConfigureActionBar(view);

		mPostFooter = (RelativeLayout) findViewById(R.id.commentFooter);

		mSlidingDrawer = (WrappingSlidingDrawer)findViewById(R.id.slidingDrawer);
		
		mEmoticonGrid = (GridView) findViewById(R.id.gridView);
		//mEmoticonGrid.setVisibility(View.GONE);

		mEmoticonAdapter = new EmoticonAdapter(this);
		mEmoticonGrid.setAdapter(mEmoticonAdapter);
		initializeEmoticonList();

		RelativeLayout gridViewContainer = (RelativeLayout)findViewById(R.id.gridViewContainer);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(NewsFeedItemActivity.this);

		String themeSelection = prefs.getString(Constants.THEME_PREFERENCES_KEY, Constants.THEME_DEFAULT);
		gridViewContainer.setBackgroundDrawable(ThemeFactory.getActionBarColorDrawable(themeSelection, this));
		
		// Configure ListView.
		mListView = (ListView) findViewById(R.id.list);
		View headerView = getView(getItemViewType(), mListView);
		mListView.addHeaderView(headerView);

		if (Integer.parseInt(mItem.getCommentsCount()) > 0) {
			mPrevCommentsView = (ViewSwitcher) LayoutInflater.from(getBaseContext()).inflate(R.layout.header_view_previous_comments, mListView, false);
			mPrevCommentsView.setTag("MoreCommentsButton");

			mPrevCommentsView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mPrevCommentsView.setClickable(false);
					mPrevCommentsView.showNext();
					getComments();
				}
			});

			mListView.addHeaderView(mPrevCommentsView);

		}

		mAdapter = new ItemAdapter();

		try {
			mListView.setAdapter(mAdapter);
		} catch (Exception e) {
			return;
		}

		ImageButton emoticonButton = (ImageButton) findViewById(R.id.emoticonButton);
		emoticonButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (menuIsOpen) {
					//mEmoticonGrid.setVisibility(View.GONE);
					//mEmoticonGrid.setVisibility(View.VISIBLE);
					mSlidingDrawer.animateClose();
				} else {
					//mEmoticonGrid.setVisibility(View.VISIBLE);
					mSlidingDrawer.animateOpen();
				}
				

				menuIsOpen = !menuIsOpen;

				// TODO: this works but only once
				/*
				 * mEmoticonGrid.setVisibility(View.VISIBLE); float gridHeight =
				 * mEmoticonGrid.getHeight(); float postFooterHeight =
				 * mPostFooter.getHeight(); Display display =
				 * getWindowManager().getDefaultDisplay(); Point size = new
				 * Point(); display.getSize(size); int screenHeight = size.y;
				 * ObjectAnimator.ofFloat(mEmoticonGrid, "translationY",
				 * screenHeight+gridHeight,
				 * gridHeight-postFooterHeight).start();
				 */

				// ObjectAnimator.ofFloat(mEmoticonGrid, "yFraction", 1.0f,
				// 0.25f).start();
			}
		});

		mPostCommentButton = (Button) findViewById(R.id.postButton);
		mPostCommentEditText = (EditText) findViewById(R.id.editText);

		mPostCommentEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mPostCommentEditText, InputMethodManager.SHOW_FORCED);
				}
			}
		});

		mPostCommentEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				EmoticonUtil.addSmiledText(NewsFeedItemActivity.this, editable);
			}
		});

		// check if softkeyboard is launched.
		final View activityRootView = findViewById(R.id.container);
		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				Rect r = new Rect();
				// r will be populated with the coordinates of your view that
				// area still visible.
				activityRootView.getWindowVisibleDisplayFrame(r);

				int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
				if (heightDiff > 100) { // if more than 100 pixels, its probably
										// a keyboard...
					softkeyboardIsShown = true;
				} else {
					softkeyboardIsShown = false;
				}
			}
		});

		mPostCommentButton.setClickable(true);
		mPostCommentButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (softkeyboardIsShown) {
					InputUtil.hideKeyboard(NewsFeedItemActivity.this);
				}

				Logger.i("post button clicked");

				String userComment = mPostCommentEditText.getText().toString();

				if (!StringUtil.notEmpty(userComment)) {
					Crouton.makeText(NewsFeedItemActivity.this, "Please compose a message before posting", Style.ALERT).show();
				} else {

					Crouton.makeText(NewsFeedItemActivity.this, "Posting your comment...", Style.ALERT).show();

					Intent intent = new Intent(NewsFeedItemActivity.this, CommentService.class);

					intent.putExtra(Constants.MESSAGE_KEY, userComment);
					intent.putExtra(Constants.ACTION_TYPE, Constants.ACTION_TYPE_COMMENT);
					intent.putExtra(Constants.COMMENT_OBJECT_ID_KEY, mItem.getId());

					Logger.i("starting service");

					startService(intent);
				}
			}
		});

	}

	private View getView(int type, ListView listView) {

		final View view;

		switch (type) {
		case TYPE_ITEM_PHOTO:
			view = LayoutInflater.from(getBaseContext()).inflate(R.layout.single_newsfeed_item_photo, listView, false);

			ImageView imageView = (ImageView) view.findViewById(R.id.newsFeedItemImage);
			if (imageView != null) {

				String token = FBClientApplication.getApplication().getFBConnection().getFacebook().getAccessToken();
				boolean isTablet = getResources().getBoolean(R.bool.isTablet);
				String imageUrl;
				if (isTablet) {
					imageUrl = "https://graph.facebook.com/" + mItem.getObjectId() + "/picture?width=1000&height=1000&access_token=" + token;
				} else {
					imageUrl = "https://graph.facebook.com/" + mItem.getObjectId() + "/picture?width=400&height=400&access_token=" + token;
				}

				getImageLoader().displayImage(imageUrl, imageView, getImageDisplayOptions());

				final String stringUrl = imageUrl = "https://graph.facebook.com/" + mItem.getObjectId() + "/picture?width=1200&height=1200&access_token=" + token;

				imageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(NewsFeedItemActivity.this, TouchImageViewActivity.class);
						intent.putExtra(Constants.OBJECT_URL_KEY, stringUrl);
						startActivity(intent);
					}
				});
			}

			break;

		case TYPE_ITEM_STATUS:
			view = LayoutInflater.from(getBaseContext()).inflate(R.layout.single_newsfeed_item_status, listView, false);
			break;

		default:
			view = LayoutInflater.from(getBaseContext()).inflate(R.layout.single_newsfeed_item, listView, false);
			ImageView iv = (ImageView) view.findViewById(R.id.newsFeedItemPicture);
			if (iv != null && StringUtil.notEmpty(mItem.getPicture())) {
				getImageLoader().displayImage(mItem.getPicture(), iv, getImageDisplayOptions());
			}
			break;
		}

		// From
		if (StringUtil.notEmpty(mItem.getFromName())) {
			mHeaderFromName = (TextView) view.findViewById(R.id.fromName);
			mHeaderFromName.setText(Html.fromHtml(FacebookUtils.getFromStringForNewsFeedItem(mItem)));
		}

		// from pic

		ImageView fromImage = (ImageView) view.findViewById(R.id.fromPicture);

		if (StringUtil.notEmpty(mItem.getProfilePicture())) {
			getImageLoader().displayImage(mItem.getProfilePicture(), fromImage, getImageDisplayOptions());

		}

		// time
		if (StringUtil.notEmpty(mItem.getCreatedTime())) {
			mHeaderCreatedTime = (TextView) view.findViewById(R.id.createdTime);
			mHeaderCreatedTime.setText(FacebookUtils.getCreatedTimeInNewsFeed(mItem));
		}

		// Message
		mHeaderMessage = (TextView) view.findViewById(R.id.newsFeedItemMessage);
		if (StringUtil.notEmpty(mItem.getMessage())) {
			mHeaderMessage.setText(mItem.getMessage());
			Linkify.addLinks(mHeaderMessage, Linkify.ALL);
		} else if (StringUtil.notEmpty(mItem.getStory())) {
			mHeaderMessage.setText(mItem.getStory());
			Linkify.addLinks(mHeaderMessage, Linkify.ALL);
		} else {
			mHeaderMessage.setVisibility(View.GONE);
		}

		if (StringUtil.notEmpty(mItem.getStoryTags()) && mHeaderMessage != null) {
			mHeaderMessage.setMovementMethod(LinkMovementMethod.getInstance());
			ArrayList<StoryTag> tags = NewsFeedItem.getStoryTagsFromJSON(mItem.getStoryTags());
			Spannable spans = (Spannable) mHeaderMessage.getText();
			if (tags.size() > 0) {
				for (final StoryTag tag : tags) {
					ClickableSpan clickSpan = new ClickableSpan() {
						@Override
						public void onClick(View widget) {
							handleLinkClicks(tag);
						}
					};
					int end = tag.getOffset() + tag.getLength();
					try {
						spans.setSpan(clickSpan, tag.getOffset(), end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					} catch (IndexOutOfBoundsException e) {
					}
				}
			}
		}

		// name

		if (StringUtil.notEmpty(mItem.getName())) {
			mHeaderName = (TextView) view.findViewById(R.id.newsFeedItemName);
			mHeaderName.setText(mItem.getName());
			// Linkify.addLinks(mHeaderName, Linkify.ALL);
		}

		// description
		if (StringUtil.notEmpty(mItem.getDescription())) {
			mHeaderDescription = (TextView) view.findViewById(R.id.newsFeedItemDescription);
			mHeaderDescription.setText(mItem.getDescription());
			// Linkify.addLinks(mHeaderDescription, Linkify.ALL);
		}

		final ViewGroup linkBox = (ViewGroup) view.findViewById(R.id.newsFeedItemPictureAndName);
		if (linkBox != null) {
			final String url = mItem.getLink();

			linkBox.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Logger.i(Logger.getClassAndMethod() + url);

					if (url == null)
						return;

					// don't link back to facebook
					if ("photo".equalsIgnoreCase(mItem.getType()))
						return;

					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.addCategory(Intent.CATEGORY_BROWSABLE);
					intent.setData(Uri.parse(url));
					startActivity(intent);
				}
			});
		}

		// image

		// like count
		ImageView likeIcon = (ImageView) view.findViewById(R.id.likeIcon);
		final TextView likeCount = (TextView) view.findViewById(R.id.likeCount);
		if (StringUtil.notEmpty(mItem.getLikesCount()) && Integer.parseInt(mItem.getLikesCount()) > 0) {
			likeIcon.setVisibility(View.VISIBLE);
			likeCount.setVisibility(View.VISIBLE);
			likeCount.setText(mItem.getLikesCount());
		} else {
			likeIcon.setVisibility(View.GONE);
			likeCount.setVisibility(View.GONE);
		}

		// like button
		final Button likeButton = (Button) view.findViewById(R.id.likeButton);

		FacebookUtils.setLikeButtonState(likeButton, mItem);

		// like button click
		likeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				FacebookUtils.handleLikeButtonClick(NewsFeedItemActivity.this, mItem, likeButton, likeCount, view, FacebookUtils.LIKE_CONTEXT_SINGLE_NEWSFEED_ITEM);
			}
		});

		return view;
	}

	private void getComments() {
		if (mCommentCount > 0 && mOffset < mCommentCount) {
			Logger.i("NewsFeedItemFragment#getComments for id: " + mItem.getId());

			// FB doesn't support JOIN so we compose a FB 'multiquery' to
			// retrieve comment details and commenting user's id from different
			// tables.

			final String query1 = "SELECT id, post_id, fromid, text, time, likes, user_likes FROM comment WHERE post_id = " + "'" + mItem.getId() + "'" + " ORDER BY time DESC LIMIT 16 OFFSET "
					+ Integer.toString(mOffset);
			final String query2 = "SELECT uid, name FROM user WHERE uid IN (SELECT fromid FROM #query1)";

			final JSONObject jsonQueries = new JSONObject() {
				{
					try {
						put("query1", query1);
						put("query2", query2);
					} catch (Exception e) {
						Logger.i(e.toString());
					}
				}
			};

			Bundle params = new Bundle();
			params.putString("method", "fql.multiquery");
			Logger.i(jsonQueries.toString());
			params.putString("queries", jsonQueries.toString());
			((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(null, params, new CommentsListener());
		}
	}

	private class CommentsListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {
			try {
				// Split response into two JSON arrays: comments and names
				JSONArray a = new JSONArray(response);
				JSONObject commentsObj = a.getJSONObject(0);
				JSONObject namesObj = a.getJSONObject(1);
				JSONArray commentsJsonArray = commentsObj.getJSONArray("fql_result_set");
				JSONArray namesJsonArray = namesObj.getJSONArray("fql_result_set");

				// Create names map
				HashMap<String, String> namesMap = new HashMap<String, String>();
				for (int i = 0; i < namesJsonArray.length(); i++) {

					String uid = "";
					String username = "";
					if (namesJsonArray.getJSONObject(i).has("uid")) {
						uid = namesJsonArray.getJSONObject(i).getString("uid");
					}
					if (namesJsonArray.getJSONObject(i).has("name")) {
						username = namesJsonArray.getJSONObject(i).getString("name");
					}
					namesMap.put(uid, username);
				}

				// Create comments ArrayList. Fill it with comment objects. Set
				// their fromName from the namesMap.
				final List<Comment> comments = new ArrayList<Comment>();

				if (commentsJsonArray.length() > 0) {
					for (int i = 0; i < commentsJsonArray.length(); i++) {
						JSONObject obj = commentsJsonArray.getJSONObject(i);
						Comment comment = Comment.fromJson(obj);
						comment.setFromName(namesMap.get(comment.getFromId()));
						comments.add(comment);
					}
				}

				try {
					NewsFeedItemActivity.this.runOnUiThread(new Runnable() {

						@Override
						public void run() {

							// Show latest at bottom of list
							Collections.reverse(comments);

							// If we're adding user's posted post via
							// getComments, we need to clear comment arraylist
							// to avoid duplication
							if (isAddingOwnPost) {
								isAddingOwnPost = false;
								mComments.clear();
							}

							// Add older comments at top of list

							mComments.addAll(0, comments);
							mOffset += mOffsetIncrement;
							Logger.i("mComments.size(): " + mComments.size());

							// Decide if we should show View-Previous-Comments
							// Button
							if (mPrevCommentsView != null) {
								if (mComments.size() >= mCommentCount) {
									mPrevCommentsView.setVisibility(View.GONE);
									mListView.removeHeaderView(mPrevCommentsView);
								} else {
									mPrevCommentsView.showNext();
									mPrevCommentsView.setClickable(true);
								}
							}

							mAdapter.notifyDataSetChanged();

						}
					});
				} catch (NullPointerException e) {
					Logger.i(e.toString());
				}

			} catch (JSONException e) {
				Logger.i(NewsFeedItemActivity.class.getSimpleName() + "#CommentsListener" + e.toString());
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(NewsFeedItemActivity.class.getSimpleName() + "#CommentsListener" + e.toString());
			OutputUtil.showCrouton(getParent(), "Comments could not be retrieved");
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(NewsFeedItemActivity.class.getSimpleName() + "#CommentsListener" + e.toString());
			OutputUtil.showCrouton(getParent(), "Comments could not be retrieved");
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(NewsFeedItemActivity.class.getSimpleName() + "#CommentsListener" + e.toString());
			OutputUtil.showCrouton(getParent(), "Comments could not be retrieved");
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(NewsFeedItemActivity.class.getSimpleName() + "#CommentsListener" + e.toString());
			OutputUtil.showCrouton(getParent(), "Comments could not be retrieved");
		}
	}

	class ItemAdapter extends BaseAdapter {

		private class ViewHolder {
			public ImageView fromPicture;
			public TextView fromName;
			public TextView createdTime;
			public TextView message;
			public TextView likeCount;
			public Button likeButton;
		}

		@Override
		public int getCount() {
			if (mComments != null) {
				return mComments.size();
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

			final Comment comment = mComments.get(position);

			View view = convertView;
			final ViewHolder holder;
			if (convertView == null) {
				view = getLayoutInflater().inflate(R.layout.comment, null);

				holder = new ViewHolder();
				holder.fromName = (TextView) view.findViewById(R.id.fromName);
				holder.fromName.setTextSize(getTextSizeForFromName());
				holder.fromPicture = (ImageView) view.findViewById(R.id.fromPicture);
				holder.createdTime = (TextView) view.findViewById(R.id.createdTime);
				holder.createdTime.setTextSize(getTextSizeForTimeStamp());
				holder.likeCount = (TextView) view.findViewById(R.id.likeCount);
				holder.message = (TextView) view.findViewById(R.id.message);
				holder.message.setTextSize(getTextSize());
				holder.message.setTextColor(getTextColor());
				holder.message.setTypeface(getTypeFace());
				holder.likeButton = (Button) view.findViewById(R.id.likeButton);
				view.setTag(holder);
			} else
				holder = (ViewHolder) view.getTag();

			holder.fromName.setText(comment.getFromName());
			
			String message = comment.getMessage();
			
			Editable  editable = new SpannableStringBuilder(message);
			EmoticonUtil.addSmiledText(NewsFeedItemActivity.this, editable);
			
			holder.message.setText(editable);
			holder.createdTime.setText(FacebookUtils.convertUnixTimeStampToRelativeTime(comment.getCreatedTime()));
			holder.likeCount.setText(comment.getLikesCount());
			getImageLoader().displayImage(comment.getFromPicture(), holder.fromPicture, getImageDisplayOptions());

			FacebookUtils.setLikeButtonState(holder.likeButton, comment);

			holder.likeButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					FacebookUtils.handleLikeButtonClick(NewsFeedItemActivity.this, comment, holder.likeButton, holder.likeCount, v, FacebookUtils.LIKE_CONTEXT_COMMENT);
				}
			});

			holder.fromPicture.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Intent intent = new Intent(NewsFeedItemActivity.this, ProfileActivity.class);
					intent.putExtra(Constants.OBJECT_ID_KEY, comment.getFromId());
					intent.putExtra(Constants.OBJECT_TITLE_KEY, comment.getFromName());
					startActivity(intent);
					overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				}
			});

			return view;
		}
	}

	public int getItemViewType() {

		String type = mItem.getType();

		if (type.equalsIgnoreCase(NewsFeedItem.NEWSFEED_ITEM_TYPE_PHOTO)) {
			return TYPE_ITEM_PHOTO;
		} else if (type.equalsIgnoreCase(NewsFeedItem.NEWSFEED_ITEM_TYPE_STATUS)) {
			return TYPE_ITEM_STATUS;
		} else {
			return TYPE_ITEM_BASE;
		}
	}

	private void handleLinkClicks(StoryTag tag) {
		Logger.i("tag name: " + tag.getName());
		Logger.i("tag id: " + tag.getUserId());
		Logger.i("tag type: " + tag.getType());

		if (tag.getType().toString().trim().equalsIgnoreCase("user")) {

			Intent intent = new Intent(NewsFeedItemActivity.this, ProfileActivity.class);
			intent.putExtra(Constants.OBJECT_ID_KEY, tag.getUserId());
			intent.putExtra(Constants.OBJECT_TITLE_KEY, tag.getName());
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

		} else if (tag.getType().toString().trim().equalsIgnoreCase("page")) {
			Intent intent = new Intent(NewsFeedItemActivity.this, PageActivity.class);
			intent.putExtra(Constants.OBJECT_ID_KEY, tag.getUserId());
			intent.putExtra(Constants.OBJECT_TITLE_KEY, tag.getName());
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}
	}

	// below this is emoticon stuff

	private void initializeEmoticonList() {
		mEmoticonAdapter.add(new EmoticonItem(":)", R.drawable.emoticon_smile));
		mEmoticonAdapter.add(new EmoticonItem(":(", R.drawable.emoticon_frown));
		mEmoticonAdapter.add(new EmoticonItem(":P", R.drawable.emoticon_tongue));
		mEmoticonAdapter.add(new EmoticonItem(":D", R.drawable.emoticon_grin));
		mEmoticonAdapter.add(new EmoticonItem(":O", R.drawable.emoticon_gasp));
		mEmoticonAdapter.add(new EmoticonItem(";)", R.drawable.emoticon_wink));
		mEmoticonAdapter.add(new EmoticonItem("B)", R.drawable.emoticon_glasses));
		mEmoticonAdapter.add(new EmoticonItem("B|", R.drawable.emoticon_sunglasses));
		mEmoticonAdapter.add(new EmoticonItem(">:(", R.drawable.emoticon_grumpy));
		mEmoticonAdapter.add(new EmoticonItem(":/", R.drawable.emoticon_unsure));
		mEmoticonAdapter.add(new EmoticonItem(":'(", R.drawable.emoticon_cry));
		mEmoticonAdapter.add(new EmoticonItem("3:)", R.drawable.emoticon_devil));
		mEmoticonAdapter.add(new EmoticonItem("O:)", R.drawable.emoticon_angel));
		mEmoticonAdapter.add(new EmoticonItem(":*", R.drawable.emoticon_kiss));
		mEmoticonAdapter.add(new EmoticonItem("<3", R.drawable.emoticon_heart));
		mEmoticonAdapter.add(new EmoticonItem("^_^", R.drawable.emoticon_kiki));
		mEmoticonAdapter.add(new EmoticonItem("-_-", R.drawable.emoticon_squint));
		mEmoticonAdapter.add(new EmoticonItem("o.O", R.drawable.emoticon_confused));
		mEmoticonAdapter.add(new EmoticonItem(">:o", R.drawable.emoticon_upset));
		mEmoticonAdapter.add(new EmoticonItem(":v", R.drawable.emoticon_pacman));
		//mEmoticonAdapter.add(new EmoticonItem(":3", R.drawable.emoticon_curlylips));
		//mEmoticonAdapter.add(new EmoticonItem(":|]", R.drawable.emoticon_robot));
		//mEmoticonAdapter.add(new EmoticonItem("(^^^)", R.drawable.emoticon_shark));
		//mEmoticonAdapter.add(new EmoticonItem("<(\")", R.drawable.emoticon_penguin));
		mEmoticonAdapter.add(new EmoticonItem("(Y)", R.drawable.emoticon_thumb));

		for (int i = 0; i < mEmoticonAdapter.getCount(); i++) {
			mEmoticonAdapter.addItem(mEmoticonAdapter.getItem(i));
		}

		mEmoticonAdapter.notifyDataSetChanged();
	}

	private class EmoticonItem {
		private String name;
		private Drawable drawable;
		private int type;

		public EmoticonItem(String name, int iconRes) {
			this.name = name;
			this.drawable = (Drawable) getResources().getDrawable(iconRes);
		}

		public Drawable getIcon() {
			return drawable;
		}

		public String getName() {
			return name;
		}

	}

	public class EmoticonAdapter extends ArrayAdapter<EmoticonItem> {

		private ArrayList<EmoticonItem> emoticons = new ArrayList<EmoticonItem>();

		private class ViewHolder {
			public ImageView icon;
		}

		public EmoticonAdapter(Context context) {
			super(context, 0);
		}

		public void addItem(EmoticonItem item) {
			emoticons.add(item);
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			final EmoticonItem item = emoticons.get(position);

			View view = convertView;
			ViewHolder holder = null;

			if (convertView == null) {
				holder = new ViewHolder();
				view = LayoutInflater.from(getContext()).inflate(R.layout.emoticon_icon, null);
				holder.icon = (ImageView) view.findViewById(R.id.emoticonIcon);

				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.icon.setImageDrawable(item.getIcon());
			holder.icon.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Editable text = mPostCommentEditText.getText();
					String str = " " + item.getName() + " ";
					text.append(str);
					mPostCommentEditText.setText(text);

					//mEmoticonGrid.setVisibility(View.GONE);
					menuIsOpen = !menuIsOpen;
					mSlidingDrawer.animateClose();

				}
			});

			return view;
		}
	}

}