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

package com.bluebitapps.fbclientbase.photos;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bluebitapps.utils.FacebookUtils;
import com.bluebitapps.utils.InputUtil;
import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.actions.CommentService;
import com.bluebitapps.fbclientbase.actions.Likeable;
import com.bluebitapps.fbclientbase.albums.Album;
import com.bluebitapps.fbclientbase.base.BaseFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

public class ImagePagerFragment extends BaseFragment {

	private ImagePagerAdapter mAdapter;
	private List<Photo> mPhotos;
	private HashMap<String, Photo> mPhotosMap;
	private ViewPager mPager;
	private int mPosition;
	private static final String SAVED_STATE = "saved state";
	private static final String PAGER_INDEX = "pager index";
	private boolean hasRetrievedComments = false;
	private boolean needToRetrievePhotoDetailsFromFb = false;
	private int mPositionInPager;
	// private int pagerIndex = 0;

	private CommentPostResultReceiver mCommentPostResultReceiver;

	private String mIds;
	private String mTitle;
	private int mPhotoCount;

	private class CommentPostResultReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (CommentService.COMMENT_POST_SUCCESS.equals(intent.getAction())) {
				Logger.i(ImagePagerFragment.class.getSimpleName() + "." + CommentPostResultReceiver.class.getSimpleName() + "." + CommentService.COMMENT_POST_SUCCESS);
				OutputUtil.showCrouton(getActivity(), "Your comment has been posted");
				getComments();
			}

			if (CommentService.COMMENT_POST_FAIL.equals(intent.getAction())) {
				Logger.i(ImagePagerFragment.class.getSimpleName() + CommentPostResultReceiver.class.getSimpleName() + "." + CommentService.COMMENT_POST_FAIL);
				OutputUtil.showCrouton(getActivity(), "Comment could not be posted");
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("jan22", Logger.getClassAndMethod());

		setHasOptionsMenu(true);
		setHasNoLoadingImage(true);
		
		if (savedInstanceState != null) {
			hasRetrievedComments = savedInstanceState.getBoolean(SAVED_STATE);
		}

		setRetainInstance(true);
		Bundle bundle = getArguments();

		mPhotos = new ArrayList<Photo>();
		mPhotos = bundle.getParcelableArrayList("photos");
		mPosition = bundle.getInt("position");
		mTitle = bundle.getString(Album.ALBUM_NAME_KEY);
		Log.i("jan9", "mTitle: " + mTitle);
		mPhotoCount = bundle.getInt(Album.ALBUM_PHOTO_COUNT);

		if (Constants.TRUE.equalsIgnoreCase(bundle.getString(Constants.PHOTO_ACCESS_VIA_NOTIFICATION))) {
			Logger.i(Logger.getClassAndMethod() + "flag true");
			needToRetrievePhotoDetailsFromFb = true;
		} else {
			needToRetrievePhotoDetailsFromFb = false;
			Logger.i(Logger.getClassAndMethod() + "flag not true");
		}

		mPhotosMap = new HashMap<String, Photo>();
		for (int i = 0; i < mPhotos.size(); i++) {
			mPhotosMap.put(mPhotos.get(i).getId(), mPhotos.get(i));
		}

		Logger.i(ImagePagerFragment.class.getSimpleName() + "mPhotos: " + mPhotos.toString());

		// Gather the photo ids into a string for use in SQL query.
		if (mPhotos.size() > 0) {
			mIds = "(";

			for (int i = 0; i < mPhotos.size(); i++) {
				mIds += mPhotos.get(i).getId() + ",";
			}
			mIds = mIds.substring(0, mIds.length() - 2);
			mIds += ")";
		}
		Logger.i("mIds: " + mIds);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.ac_image_pager);

		mPager = (ViewPager) vg.findViewById(R.id.pager);
		mPager.setPageMargin(8);
		mAdapter = new ImagePagerAdapter();

		return vg;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (getActivity() != null) {
			if (StringUtil.notEmpty(mTitle)) {
				Log.i("jan9", "onResume " + mTitle);
				getActivity().getActionBar().setDisplayShowTitleEnabled(true);
				getActivity().getActionBar().setTitle(mTitle);

				String photoWord = mPhotoCount == 1 ? "photo" : "photos";
				String str = mPhotoCount + " " + photoWord + ". Swipe to see next.";
				getActivity().getActionBar().setSubtitle(str);
			}
		}

		if (mCommentPostResultReceiver == null) {
			mCommentPostResultReceiver = new CommentPostResultReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(CommentService.COMMENT_POST_SUCCESS);
			intentFilter.addAction(CommentService.COMMENT_POST_FAIL);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			if (getActivity() != null) {
				getActivity().registerReceiver(mCommentPostResultReceiver, intentFilter);
			}
		}

		if (needToRetrievePhotoDetailsFromFb) {
			getPhotoDetailsFromFB();
		}

		if (!hasRetrievedComments) {
			getComments();
		} else {
			mPager.setAdapter(mAdapter);
			mPager.setCurrentItem(mPosition);
			mAdapter.notifyDataSetChanged();
			hasRetrievedComments = true;

		}
	}

	private void getPhotoDetailsFromFB() {
		if (getActivity() == null) {
			return;
		}

		String objectId = mPhotos.get(0).getId();

		((FBClientApplication) getActivity().getApplication()).getFBConnection().getAsyncFacebookRunner().request(objectId, new PhotosRequestListener());
	}

	private class PhotosRequestListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {

			Logger.i(Logger.getClassAndMethod() + response.toString());

			try {

				final JSONObject dataJsonObject = new JSONObject(response);
				final Photo photo = Photo.fromJson(dataJsonObject);
				Logger.i(Logger.getClassAndMethod() + photo.toString());

				if (getActivity() != null) {

					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mPhotos.clear();
							mPhotos.add(photo);

							Logger.i(Logger.getClassAndMethod() + mPhotos.get(0).getSource());
							Logger.i(Logger.getClassAndMethod() + "# photos: " + mPhotos.size());
							mPager.setAdapter(mAdapter);
							mPager.setCurrentItem(0);
							mAdapter.notifyDataSetChanged();
						}
					});
				}

			} catch (JSONException e) {
				Logger.i(PhotosFragment.class.getSimpleName() + "." + PhotosRequestListener.class.getSimpleName() + ": " + e.toString());
				OutputUtil.showCrouton(getActivity(), "Photos could not be retrieved");
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(PhotosFragment.class.getSimpleName() + "." + PhotosRequestListener.class.getSimpleName() + ": " + e.toString());
			OutputUtil.showCrouton(getActivity(), "Photos could not be retrieved");
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(PhotosFragment.class.getSimpleName() + "." + PhotosRequestListener.class.getSimpleName() + ": " + e.toString());
			OutputUtil.showCrouton(getActivity(), "Photos could not be retrieved");
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(PhotosFragment.class.getSimpleName() + "." + PhotosRequestListener.class.getSimpleName() + ": " + e.toString());
			OutputUtil.showCrouton(getActivity(), "Photos could not be retrieved");
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(PhotosFragment.class.getSimpleName() + "." + PhotosRequestListener.class.getSimpleName() + ": " + e.toString());
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mCommentPostResultReceiver != null && getActivity() != null) {
			getActivity().unregisterReceiver(mCommentPostResultReceiver);
			// An activity can be resumed after pausing, so the activity may not
			// register the listener in the second call to onResume.
			mCommentPostResultReceiver = null;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putBoolean(SAVED_STATE, hasRetrievedComments);
		outState.putInt(PAGER_INDEX, mPager.getCurrentItem());
	}

	private void getComments() {
		final String query1 = "SELECT id, object_id, fromid, text, time, user_likes, likes FROM comment WHERE object_id IN " + mIds;
		Logger.i("query1: " + query1);
		final String query2 = "SELECT uid, name FROM user WHERE uid IN (SELECT fromid FROM #query1)";
		final String query3 = "SELECT object_id, like_info FROM photo WHERE object_id IN " + mIds;

		final JSONObject jsonQueries = new JSONObject() {
			{
				try {
					put("query1", query1);
					put("query2", query2);
					put("query3", query3);
				} catch (JSONException e) {
					Logger.i(ImagePagerFragment.class.getSimpleName() + "#getComments().jsonQueries: " + e.toString());
				}
			}
		};

		Bundle params = new Bundle();
		params.putString("method", "fql.multiquery");
		params.putString("queries", jsonQueries.toString());
		((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(null, params, new CommentsListener());
	}

	private class CommentsListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {

			try {
				JSONArray a = new JSONArray(response);
				JSONObject commentsObj = a.getJSONObject(0);
				JSONObject userNamesObj = a.getJSONObject(2);
				JSONObject likeInfoObj = a.getJSONObject(1);
				Logger.i(ImagePagerFragment.class.getSimpleName() + "." + CommentsListener.class.getSimpleName() + "commentsObj: " + commentsObj.toString());
				Logger.i(ImagePagerFragment.class.getSimpleName() + "." + CommentsListener.class.getSimpleName() + "userNamesObj: " + userNamesObj.toString());
				Logger.i(ImagePagerFragment.class.getSimpleName() + "." + CommentsListener.class.getSimpleName() + "likeInfoObj: " + likeInfoObj.toString());
				JSONArray commentsJsonArray = commentsObj.getJSONArray("fql_result_set");
				JSONArray userNamesJsonArray = userNamesObj.getJSONArray("fql_result_set");
				JSONArray likesInfoJsonArray = likeInfoObj.getJSONArray("fql_result_set");

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

				HashMap<String, String> likesCountMap = new HashMap<String, String>();
				HashMap<String, String> userLikesMap = new HashMap<String, String>();

				if (likesInfoJsonArray.length() > 0) {
					// Places likesCount in map
					for (int k = 0; k < likesInfoJsonArray.length(); k++) {
						String objectId = "";
						String likesCount = "0";
						String userLikes = "false";

						if (likesInfoJsonArray.getJSONObject(k).has("object_id")) {
							objectId = likesInfoJsonArray.getJSONObject(k).getString("object_id");
						}

						if (likesInfoJsonArray.getJSONObject(k).has("like_info")) {
							JSONObject likeInfo = likesInfoJsonArray.getJSONObject(k).getJSONObject("like_info");
							likesCount = likeInfo.getString("like_count");
							userLikes = likeInfo.getString("user_likes");
						}

						likesCountMap.put(objectId, likesCount);
						userLikesMap.put(objectId, userLikes);

						Photo photo = mPhotosMap.get(objectId);
						if (photo != null) {
							photo.setUserLikes(userLikes);
							photo.setLikesCount(likesCount);
						}
					}
				}

				if (commentsJsonArray.length() > 0) {
					for (int i = 0; i < commentsJsonArray.length(); i++) {
						JSONObject obj = commentsJsonArray.getJSONObject(i);
						// Create comment object
						PhotoComment comment = PhotoComment.fromJSON(obj);
						// Set comment's fromName from userNamesMap
						comment.setFromName(userNamesMap.get(comment.getFromId()));
						// Add comment to comments ArrayList for that photo in
						// commentsMap.
						Logger.i("comment.getObjectId(): " + comment.getObjectId());

						String key = comment.getObjectId();
						Photo photo = mPhotosMap.get(key);
						// add comments to photo
						photo.addComment(comment);
						// set likes count on photo
					}
				}

				if (getActivity() != null) {

					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {

							if (mPager != null) {
								mPager.setAdapter(mAdapter);
								mPager.setCurrentItem(mPosition);
							}

							if (mAdapter != null) {
								mAdapter.notifyDataSetChanged();
							}

							hasRetrievedComments = true;

						}
					});
				}

			} catch (JSONException e) {
				Logger.i(Logger.getClassAndMethod() + e.toString());
				OutputUtil.showCrouton(getActivity(), "Images could not be retrieved");
			}

		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			OutputUtil.showCrouton(getActivity(), "Images could not be retrieved");

		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			OutputUtil.showCrouton(getActivity(), "Images could not be retrieved");

		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			OutputUtil.showCrouton(getActivity(), "Images could not be retrieved");

		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			OutputUtil.showCrouton(getActivity(), "Images could not be retrieved");

		}

	}

	private class ImagePagerAdapter extends PagerAdapter {

		private LayoutInflater inflater;

		ImagePagerAdapter() {
			if (getActivity() == null) {
				return;
			}
			inflater = getActivity().getLayoutInflater();
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		@Override
		public void finishUpdate(View container) {
		}

		@Override
		public int getCount() {
			if (mPhotos != null) {
				return mPhotos.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object instantiateItem(View view, int position) {
			Logger.i(Logger.getClassAndMethod() + "position: " + position);
			final View page = inflater.inflate(R.layout.photos_pager_item, null);
			final ImageView imageView = (ImageView) page.findViewById(R.id.image);
			final LoadingView loadingView = (LoadingView) page.findViewById(R.id.loading);
			final ListView listView = (ListView) page.findViewById(R.id.list);
			final TextView commentCount = (TextView) page.findViewById(R.id.commentCount);
			final TextView likeCount = (TextView) page.findViewById(R.id.likeCount);
			final Button likeButton = (Button) page.findViewById(R.id.likeButton);

			final Likeable photo = mPhotos.get(position);
			final View parentView = view;

			FacebookUtils.setLikeButtonState(likeButton, mPhotos.get(position));

			likeButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					FacebookUtils.handleLikeButtonClick(getActivity(), photo, likeButton, likeCount, parentView, null);
				}
			});

			likeCount.setText(mPhotos.get(position).getLikesCount());
			commentCount.setText(mPhotos.get(position).getCommentsCount());

			ArrayList<PhotoComment> comments = mPhotos.get(position).getComments();

			Logger.i(ImagePagerFragment.class.getSimpleName() + "comments.size(): " + comments.size());

			CommentsListAdapter adapter = new CommentsListAdapter(comments);

			listView.setAdapter(adapter);

			Button postButton = (Button) page.findViewById(R.id.postButton);
			final EditText messageEditText = (EditText) page.findViewById(R.id.editText);
			postButton.setClickable(true);
			postButton.setOnClickListener(new OnClickListener() {

				final String photoId = photo.getId();

				@Override
				public void onClick(View v) {

					InputUtil.hideKeyboard(getActivity());

					String userComment = messageEditText.getText().toString();

					if (!StringUtil.notEmpty(userComment)) {
						OutputUtil.showCrouton(getActivity(), "Please compose a message before posting");
					} else {

						OutputUtil.showCrouton(getActivity(), "Posting your comment...");

						Intent intent = new Intent(getActivity(), CommentService.class);

						intent.putExtra(Constants.MESSAGE_KEY, userComment);
						intent.putExtra(Constants.ACTION_TYPE, Constants.ACTION_TYPE_COMMENT);
						intent.putExtra(Constants.COMMENT_OBJECT_ID_KEY, photoId);
						if (getActivity() != null) {
							getActivity().startService(intent);
						}
					}
				}
			});

			// Logger.i(Logger.getClassAndMethod() + "item.getPicture: " +
			// mPhotos.get(position).getPicture());

			Log.i("jan22", "mPhotos.get(position).getPicture(): " + mPhotos.get(position).getPicture());

			getImageLoader().displayImage(mPhotos.get(position).getPicture(), imageView, getImageDisplayOptions(), new ImageLoadingListener() {
				@Override
				public void onLoadingStarted() {
					loadingView.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadingFailed(FailReason failReason) {

					OutputUtil.showCrouton(getActivity(), "Error loading image");
					loadingView.setVisibility(View.GONE);
					imageView.setImageResource(android.R.drawable.ic_delete);
				}

				@Override
				public void onLoadingComplete(Bitmap loadedImage) {
					if (loadingView != null) {
						loadingView.setVisibility(View.GONE);
					}
					if (getActivity() != null) {
						Animation anim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
						if (anim != null) {
							imageView.setAnimation(anim);
						}
						anim.start();
					}
				}

				@Override
				public void onLoadingCancelled() {
					// Do nothing
				}
			});

			((ViewPager) view).addView(page, 0);
			return page;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View container) {
		}
	}

	class CommentsListAdapter extends BaseAdapter {

		private ArrayList<PhotoComment> photoComments;

		private class ViewHolder {
			public ImageView fromPicture;
			public TextView fromName;
			public TextView createdTime;
			public TextView message;
			public TextView likeCount;
			public Button likeButton;
		}

		public CommentsListAdapter(ArrayList<PhotoComment> list) {
			photoComments = list;
		}

		@Override
		public int getCount() {
			if (photoComments != null) {
				return photoComments.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		// TODO: what should getItem

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			View view = convertView;
			final ViewHolder holder;

			if (getActivity() != null) {

				final PhotoComment comment = photoComments.get(position);
				if (convertView == null) {
					view = getActivity().getLayoutInflater().inflate(R.layout.comment, null);

					holder = new ViewHolder();
					holder.fromName = (TextView) view.findViewById(R.id.fromName);
					holder.fromPicture = (ImageView) view.findViewById(R.id.fromPicture);
					holder.createdTime = (TextView) view.findViewById(R.id.createdTime);
					holder.message = (TextView) view.findViewById(R.id.message);
					holder.likeCount = (TextView) view.findViewById(R.id.likeCount);
					holder.likeButton = (Button) view.findViewById(R.id.likeButton);

					view.setTag(holder);
				} else
					holder = (ViewHolder) view.getTag();

				if (StringUtil.notEmpty(comment.getFromName())) {
					holder.fromName.setText(comment.getFromName());
				}
				if (StringUtil.notEmpty(comment.getText())) {
					holder.message.setText(comment.getText());
				}
				if (StringUtil.notEmpty(comment.getTime())) {
					holder.createdTime.setText(comment.getTime());
				}

				getImageLoader().displayImage(photoComments.get(position).getFromPicture(), holder.fromPicture, getImageDisplayOptions());

				FacebookUtils.setLikeButtonState(holder.likeButton, comment);

				holder.likeButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						FacebookUtils.handleLikeButtonClick(getActivity(), comment, holder.likeButton, holder.likeCount, v, FacebookUtils.LIKE_CONTEXT_COMMENT);
					}
				});
			}
			return view;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.imagepagermenu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		if (item.getItemId() == R.id.saveMenuItem) {

			new AlertDialog.Builder(getActivity()).setIcon(android.R.drawable.ic_menu_save).setTitle("Save Image to SD Card").setPositiveButton("Save", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					int position = mPager.getCurrentItem();
					String urlString = mPhotos.get(position).getPicture();
					OutputUtil.saveImageFromUrlToSDCard(getActivity(), "Socially", null, urlString);
				}

			}).setNegativeButton("Cancel", null).show();
			return true;
		} else {
			return false;
		}

	}
}