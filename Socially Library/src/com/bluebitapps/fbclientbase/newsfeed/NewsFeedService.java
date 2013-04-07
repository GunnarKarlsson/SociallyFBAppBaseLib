/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.newsfeed;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.utils.FacebookUtils;
import com.bluebitapps.utils.StringUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

/**
 * NewsFeedService requests the user/friend's Facebook news feed items and saves
 * them in a database, then notifies the fragment that new data is available.
 * 
 * Beware the idiosyncrasies of IntentService: - Constructor should not take a
 * String parameter, but should pass one to super. - Must override
 * onStartCommand(), or else onHandleIntent() will not be called.
 * 
 * @author Gunnar Karlsson
 * 
 */

public class NewsFeedService extends IntentService {

	private String mCreatedTime;

	private List<NewsFeedItem> mNewsFeedItems;
	private Map<String, NewsFeedItem> mNewsFeedCache;
	private String mUserId;
	private String mState;

	public static final String REFRESH_WALL_DATA_OLDER = "refresh wall data older";

	public static final String REFRESH_NEWSFEED_DATA_OLDER = "refresh newsfeed data older";

	public static final String REFRESH_NEWSFEED_DATA = "refresh newsfeed data";

	public static final String REFRESH_WALL_DATA = "refresh wall data";

	public static final String REFRESH_NEWSFEED_DATA_FAIL = "refresh data fail";

	/**
	 * The constructor should not take a String parameter, or it will throw
	 * java.lang.InstantiationException. It must pass a String name to super.
	 */

	public NewsFeedService() {
		super("NewsFeedService");

		mNewsFeedItems = new ArrayList<NewsFeedItem>();
		mNewsFeedCache = new HashMap<String, NewsFeedItem>();
	}

	/**
	 * Need to override onStartCommand() or onHandleIntent() will not be called.
	 * Is called for every call initiated by the alarm manager in
	 * broadcastreceiver.
	 */

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Log.i("april5","onHandleIntent");
		
		if (intent == null) {
			return;
		}

		if (intent.getExtras() == null) {
			return;
		}

		Bundle bundle = intent.getExtras();

		if (bundle != null) {
			mCreatedTime = bundle.getString(Constants.CREATED_TIME_KEY);
			mUserId = bundle.getString(Constants.USER_ID_KEY);
			mState = bundle.getString(Constants.STATE_KEY);
			Log.i("april5", mCreatedTime);
		}

		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();

		if (isValidSession) {
			getNewsFeed();
		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private void getNewsFeed() {

		Log.i("jan17nf", Logger.getClassAndMethod());

		Bundle params = new Bundle();

		if (mCreatedTime != null) {

			try {
				SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss+SSSS");
				Date date = format.parse(mCreatedTime);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				calendar.add(Calendar.SECOND, -1);// Subtract one second
				String newTime = format.format(calendar.getTime());
				Date newDate = format.parse(newTime);
				String timeParameter = format.format(newDate);

				params.putString("limit", "25");
				params.putString("until", timeParameter);

			} catch (ParseException e) {
				Logger.i(Logger.getClassAndMethod() + e.toString());
			}

		}

		if (mState.equalsIgnoreCase(Constants.STATE_PROFILE)) {
			Logger.i(Logger.getClassAndMethod() + " Feed");
			((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(mUserId + "/feed", params, new NewsFeedListener());
		} else {
			Logger.i(Logger.getClassAndMethod() + "Home");
			((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(mUserId + "/home", params, new NewsFeedListener());
		}

	}

	private class NewsFeedListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {

			Log.i("jan21", Logger.getClassAndMethod() + response);
			
			Log.i("april5",response);

			try {

				mNewsFeedCache.clear();
				mNewsFeedItems.clear();

				Logger.i(Logger.getClassAndMethod());

				final JSONObject dataJsonObject = new JSONObject(response);
				final JSONArray jsonArray = dataJsonObject.getJSONArray("data");

				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {

						JSONObject obj = jsonArray.getJSONObject(i);
						NewsFeedItem newsFeedItem = NewsFeedItem.fromJson(obj, mUserId);

						// Remove "incomprehensible" posts: posts with reference
						// to objects that are not showed (the API doesn't
						// provide an object reference to them in the post).

						if (StringUtil.notEmpty(newsFeedItem.getStory())) {

							String languageCode = getResources().getConfiguration().locale.getLanguage();

							if (languageCode.contains("es")) {
								if (StringUtil.stringContainsItemFromList(newsFeedItem.getStory(), Constants.forbiddenStrings_ES)) {
									continue;
								}
							} else if (languageCode.contains("de")) {

								if (StringUtil.stringContainsItemFromList(newsFeedItem.getStory(), Constants.forbiddenStrings_DE)) {
									continue;
								}
							} else if (languageCode.contains("it")) {

								if (StringUtil.stringContainsItemFromList(newsFeedItem.getStory(), Constants.forbiddenStrings_IT)) {
									continue;
								}

							} else if (languageCode.contains("pt")) {

								if (StringUtil.stringContainsItemFromList(newsFeedItem.getStory(), Constants.forbiddenStrings_PT)) {
									continue;
								}
							} else if (languageCode.contains("fr")) {

								if (StringUtil.stringContainsItemFromList(newsFeedItem.getStory(), Constants.forbiddenStrings_FR)) {
									continue;
								}
							} else {
								if (StringUtil.stringContainsItemFromList(newsFeedItem.getStory(), Constants.forbiddenStrings)) {
									continue;
								}
							}
						}

						if (!StringUtil.notEmpty(newsFeedItem.getStory()) && !StringUtil.notEmpty(newsFeedItem.getMessage()) && !StringUtil.notEmpty(newsFeedItem.getDescription())
								&& !StringUtil.notEmpty(newsFeedItem.getPicture())) {
							continue;
						}

						if (newsFeedItem != null) {

							if (newsFeedItem.getApplicationName() == null) {
								mNewsFeedItems.add(newsFeedItem);
								mNewsFeedCache.put(newsFeedItem.getId(), newsFeedItem);
							} else {
								if (!newsFeedItem.getApplicationName().equalsIgnoreCase(NewsFeedItem.APPLICATION_NAME_LIKES)) {
									if (!newsFeedItem.getApplicationName().equalsIgnoreCase(NewsFeedItem.APPLICATION_NAME_INSTAGRAM)) {
										mNewsFeedItems.add(newsFeedItem);
										mNewsFeedCache.put(newsFeedItem.getId(), newsFeedItem);
									}
								}
							}

						}
					}
				}

				if (mNewsFeedItems.size() < 1) {
					Log.i("jan21", NewsFeedListener.class.getSimpleName() + "mNewsFeedItems.size < 1");
					sendBroadcast(new Intent(NewsFeedService.REFRESH_NEWSFEED_DATA_FAIL));
				} else {

					String query = "SELECT likes, post_id FROM stream WHERE post_id IN (";

					try {
						for (int i = 0; i < mNewsFeedItems.size(); i++) {
							if (mNewsFeedItems.get(i).getId() != null) {
								query += "'" + mNewsFeedItems.get(i).getId() + "'";
								if (i < (mNewsFeedItems.size() - 1)) {
									query += ",";
								}
							}
						}
					} catch (IndexOutOfBoundsException e) {
						return;
					}

					query += ")";

					Bundle params = new Bundle();
					params.putString("method", "fql.query");
					params.putString("query", query);
					((FBClientApplication) getApplication()).getFBConnection().getAsyncFacebookRunner().request(null, params, new LikesRequestListener());
				}
			} catch (JSONException e) {
				Log.i("jan21", NewsFeedListener.class.getSimpleName() + e.toString());
				sendBroadcast(new Intent(NewsFeedService.REFRESH_NEWSFEED_DATA_FAIL));
			}
		}// end onComplete

		@Override
		public void onIOException(IOException e, Object state) {
			Log.i("jan21", NewsFeedService.class.getSimpleName() + "#NewsFeedListener" + e.toString());
			sendBroadcast(new Intent(NewsFeedService.REFRESH_NEWSFEED_DATA_FAIL));

		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Log.i("jan21", NewsFeedService.class.getSimpleName() + "#NewsFeedListener" + e.toString());
			sendBroadcast(new Intent(NewsFeedService.REFRESH_NEWSFEED_DATA_FAIL));
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Log.i("jan21", NewsFeedService.class.getSimpleName() + "#NewsFeedListener" + e.toString());
			sendBroadcast(new Intent(NewsFeedService.REFRESH_NEWSFEED_DATA_FAIL));
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Log.i("jan21", NewsFeedService.class.getSimpleName() + "#NewsFeedListener" + e.toString());
			sendBroadcast(new Intent(NewsFeedService.REFRESH_NEWSFEED_DATA_FAIL));
		}

	}

	private class LikesRequestListener implements RequestListener {

		class Holder {
			private String userLikes;
			private String postId;

			public String getUserLikes() {
				return userLikes;
			}

			public void setUserLikes(String likes) {
				this.userLikes = likes;
			}

			public String getPostId() {
				return postId;
			}

			public void setPostId(String postId) {
				this.postId = postId;
			}
		}

		@Override
		public void onComplete(String response, Object state) {

			try {

				Logger.i(LikesRequestListener.class.getSimpleName() + "#onComplete()");
				final JSONArray jsonArray = new JSONArray(response);

				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						Holder holder = new Holder();
						JSONObject obj = jsonArray.getJSONObject(i);

						if (obj.has("post_id")) {
							holder.setPostId(obj.getString("post_id"));
						}

						if (obj.has("likes")) {
							JSONObject likes = obj.getJSONObject("likes");
							if (likes.has("user_likes")) {
								holder.setUserLikes(likes.getString("user_likes"));
							}
						}

						NewsFeedItem item = mNewsFeedCache.get(holder.getPostId());
						if (item != null && holder != null && StringUtil.notEmpty(holder.getUserLikes())) {
							item.setUserLikes(holder.getUserLikes());
						}

					}

				}

				saveToDb();

			} catch (JSONException e) {
				Log.i("jan21", NewsFeedListener.class.getSimpleName() + e.toString());

			}

		}// end onComplete

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(NewsFeedListener.class.getSimpleName() + e.toString());

		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(NewsFeedListener.class.getSimpleName() + e.toString());

		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(NewsFeedListener.class.getSimpleName() + e.toString());

		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(NewsFeedListener.class.getSimpleName() + e.toString());
		}
	}

	private void saveToDb() {

		Intent intent = null;
		// Case: we have fetched the latest data
		if (mCreatedTime == null) {

			NewsFeedData data;

			if (mState.equalsIgnoreCase(Constants.STATE_PROFILE)) {
				Logger.i(Logger.getClassAndMethod() + " saveToDb saving to wall db");
				data = ((FBClientApplication) getApplication()).getNewsFeedData(NewsFeedData.REQUEST_WALL_FROM_DB);
			} else {
				Logger.i(Logger.getClassAndMethod() + "saving to newsfeed db");
				data = ((FBClientApplication) getApplication()).getNewsFeedData(NewsFeedData.REQUEST_NEWSFEED_FROM_DB);
			}

			data.deleteRowsForUser(mUserId);

			try {
				for (NewsFeedItem newsFeedItem : mNewsFeedItems) {
					data.insertOrIgnore(newsFeedItem.toContentValues());
				}
			} catch (ConcurrentModificationException e) {
				sendBroadcast(new Intent(NewsFeedService.REFRESH_NEWSFEED_DATA_FAIL));
				return;
			}

			if (mState.equalsIgnoreCase(Constants.STATE_PROFILE)) {
				intent = new Intent(NewsFeedService.REFRESH_WALL_DATA);
				Logger.i(Logger.getClassAndMethod() + "sending broadcast - wall data is available");
			} else {
				Logger.i(Logger.getClassAndMethod() + "sending broadcast - newsfeed data is available");
				intent = new Intent(NewsFeedService.REFRESH_NEWSFEED_DATA);
			}

			// Case: we have fetched older data
		} else {

			mCreatedTime = null;

			NewsFeedData data;

			if (mState.equalsIgnoreCase(Constants.STATE_PROFILE)) {
				data = ((FBClientApplication) getApplication()).getNewsFeedData(NewsFeedData.REQUEST_WALL_OLDER_FROM_DB);
			} else {
				data = ((FBClientApplication) getApplication()).getNewsFeedData(NewsFeedData.REQUEST_NEWSFEED_OLDER_FROM_DB);
			}

			data.deleteRowsForUser(mUserId);

			try {
				for (NewsFeedItem newsFeedItem : mNewsFeedItems) {
					data.insertOrIgnore(newsFeedItem.toContentValues());
				}
			} catch (ConcurrentModificationException e) {
				sendBroadcast(new Intent(NewsFeedService.REFRESH_NEWSFEED_DATA_FAIL));
				return;
			}

			if (mState.equalsIgnoreCase(Constants.STATE_PROFILE)) {
				Logger.i("sending broadcast - older wall data is available");
				intent = new Intent(NewsFeedService.REFRESH_WALL_DATA_OLDER);
			} else {
				Logger.i("sending broadcast - older newsfeed data is available");
				intent = new Intent(NewsFeedService.REFRESH_NEWSFEED_DATA_OLDER);
			}
		}
		sendBroadcast(intent);
	}
}