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

package com.bluebitapps.fbclientbase.base;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager.BadTokenException;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bluebitapps.utils.ExitUtil;
import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
import com.slidingmenu.lib.app.SlidingFragmentActivity;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.MainActivity;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.albums.Album;
import com.bluebitapps.fbclientbase.albums.AlbumsData;
import com.bluebitapps.fbclientbase.albums.AlbumsService;
import com.bluebitapps.fbclientbase.checkins.PostCheckinService;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.events.BirthdaysData;
import com.bluebitapps.fbclientbase.events.EventsData;
import com.bluebitapps.fbclientbase.friends.FriendsData;
import com.bluebitapps.fbclientbase.groups.GroupsData;
import com.bluebitapps.fbclientbase.likes.LikesData;
import com.bluebitapps.fbclientbase.menu.slidingmenu.SlidingMenuFragment;
import com.bluebitapps.fbclientbase.messages.MessageThreadData;
import com.bluebitapps.fbclientbase.messages.MessagesService;
import com.bluebitapps.fbclientbase.newsfeed.NewsFeedData;
import com.bluebitapps.fbclientbase.newsfeed.NewsFeedItem;
import com.bluebitapps.fbclientbase.newsfeed.NewsFeedItemActivity;
import com.bluebitapps.fbclientbase.notifications.NotificationsData;
import com.bluebitapps.fbclientbase.notifications.NotificationsFragment;
import com.bluebitapps.fbclientbase.photos.Photo;
import com.bluebitapps.fbclientbase.search.SearchFragment;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;

@SuppressLint({ "NewApi" })
public class BaseSlidingMenuActivity extends SlidingFragmentActivity implements com.bluebitapps.fbclientbase.menu.slidingmenu.SlidingMenuFragment.OnSlidingMenuItemSelectedListener,
		OnNewsFeedTappedListener, OnSearchQueryListener {

	private static final String SECTION_STATE_KEY = "state key";
	private static final String FB_OBJECT_ID_KEY = "tabbed view object id key";
	private static final String TAB_SELECTION_KEY = "tab selection key";

	private String mThemeSelection;
	private SectionRequestReceiver mTabViewRequestReceiver;

	private SectionManager mSectionManager;

	private boolean isShowingTextSettings = false;

	private Uri mFileUri;

	private boolean hasToPopulateMessagesDb = true;
	private boolean hasToPopulateAlbumsDb = true;

	public Uri getFileUri() {
		return mFileUri;
	}

	public void setFileUri(Uri uri) {
		mFileUri = uri;
	}

	public void setIsShowingTextSettings(boolean value) {
		isShowingTextSettings = value;
	}

	private class SectionRequestReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			Logger.i(Logger.getClassAndMethod() + intent.toString());

			Bundle bundle = intent.getExtras();

			if (Constants.STATE_IMAGE_GRID.equals(intent.getAction())) {
				if (bundle.containsKey(Album.ALBUM_ID_KEY)) {
					mSectionManager.displayImageGrid(intent.getStringExtra(Album.ALBUM_ID_KEY), intent.getStringExtra(Album.ALBUM_NAME_KEY));
				}
			}

			if (Constants.REQUEST_ALBUMS.equals(intent.getAction())) {
				if (bundle.containsKey(Constants.REQUEST_ALBUMS)) {
					mSectionManager.displayAlbumsSection();
				}
			}

			if (Constants.REQUEST_FRIEND_REQUESTS.equals(intent.getAction())) {
				Logger.i(BaseSlidingMenuActivity.class.getSimpleName() + "." + SectionRequestReceiver.class.getSimpleName() + "." + Constants.REQUEST_FRIEND_REQUESTS);
				if (bundle.containsKey(Constants.REQUEST_FRIEND_REQUESTS)) {
					mSectionManager.displayFriendRequests();
				}
			}

			if (Constants.REQUEST_IMAGE_PAGER.equals(intent.getAction())) {
				Logger.i(BaseSlidingMenuActivity.class.getSimpleName() + "." + SectionRequestReceiver.class.getSimpleName() + "." + Constants.REQUEST_IMAGE_PAGER);
				if (bundle.containsKey("photos")) {
					ArrayList<Photo> photos;
					photos = bundle.getParcelableArrayList("photos");
					String flag = bundle.getString(Constants.PHOTO_ACCESS_VIA_NOTIFICATION);
					mSectionManager.displayImagePager(photos, flag);
				}
			}

			if (Constants.REQUEST_PLACE_PROFILE.equals(intent.getAction())) {
				Logger.i(BaseSlidingMenuActivity.class.getSimpleName() + "." + SectionRequestReceiver.class.getSimpleName() + "." + Constants.REQUEST_PLACE_PROFILE);
				if (bundle.containsKey(Constants.OBJECT_ID_KEY)) {
					String placeId = bundle.getString(Constants.OBJECT_ID_KEY);
					if (StringUtil.notEmpty(placeId)) {
						mSectionManager.setTabbedViewObjectId(placeId);
						mSectionManager.displayPlaceProfile(placeId);
					}
				}

			}

			if (Constants.REQUEST_PLACE_PROFILE_ACTIVITY.equals(intent.getAction())) {

			}

			if (Constants.REQUEST_PROFILE_ACTIVITY.equals(intent.getAction())) {
				Log.i("frag", "broadcast received");
				Bundle extras = intent.getExtras();
				String userId = extras.getString(Constants.OBJECT_ID_KEY);
				String title = extras.getString(Constants.OBJECT_TITLE_KEY);
				mSectionManager.displayUserProfileActivity(userId, title);
			}

			if (Constants.REQUEST_PROFILE.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + Constants.REQUEST_PROFILE);
				if (bundle.containsKey(Constants.OBJECT_ID_KEY) && bundle.containsKey(Constants.USER_TYPE_KEY)) {
					String userId = bundle.getString(Constants.OBJECT_ID_KEY);
					int userType = bundle.getInt(Constants.USER_TYPE_KEY);
					if (StringUtil.notEmpty(userId) && userType > 0) {
						mSectionManager.setTabbedViewObjectId(userId);

						if (bundle.containsKey(Constants.TAB_INDEX_SELECTION_KEY)) {
							int index = bundle.getInt(Constants.TAB_INDEX_SELECTION_KEY);
							mSectionManager.setSelectedTabIndex(index);
						}

						mSectionManager.displayUserProfile(userId);
					}
				}
			}

			if (Constants.REQUEST_EVENT_SINGLE.equals(intent.getAction())) {
				Logger.i(BaseSlidingMenuActivity.class.getSimpleName() + "." + SectionRequestReceiver.class.getSimpleName() + "." + Constants.REQUEST_EVENT_SINGLE);
				if (bundle.containsKey(Constants.OBJECT_ID_KEY)) {

					String objectId = bundle.getString(Constants.OBJECT_ID_KEY);

					String title = "";
					if (bundle.containsKey(Constants.OBJECT_TITLE_KEY)) {
						title = bundle.getString(Constants.OBJECT_TITLE_KEY);
					}

					if (StringUtil.notEmpty(objectId)) {
						mSectionManager.setTabbedViewObjectId(objectId);
						mSectionManager.displayEventDetails(title, objectId);
					}
				}

			}

			if (Constants.REQUEST_NOTIFICATIONS.equals(intent.getAction())) {
				Logger.i(BaseSlidingMenuActivity.class.getSimpleName() + "." + SectionRequestReceiver.class.getSimpleName() + "." + Constants.REQUEST_NOTIFICATIONS);
				mSectionManager.displayNotificationSection(null);
			}
			if (Constants.TABBED_VIEW_REQUEST_GROUP_SINGLE.equals(intent.getAction())) {
				Logger.i(BaseSlidingMenuActivity.class.getSimpleName() + "." + SectionRequestReceiver.class.getSimpleName() + "." + Constants.TABBED_VIEW_REQUEST_GROUP_SINGLE);
				if (bundle.containsKey(Constants.OBJECT_ID_KEY)) {

					String objectId = bundle.getString(Constants.OBJECT_ID_KEY);
					String title = "";
					if (bundle.containsKey(Constants.OBJECT_TITLE_KEY)) {
						title = bundle.getString(Constants.OBJECT_TITLE_KEY);
					}
					Logger.i("--id: " + objectId);
					Logger.i("--title: " + title);
					if (StringUtil.notEmpty(objectId)) {
						mSectionManager.displayGroupDetails(objectId, title);
					}
				}
			}

			if (Constants.REQUEST_PAGES.equals(intent.getAction())) {

				Logger.i(BaseSlidingMenuActivity.class.getSimpleName() + "." + SectionRequestReceiver.class.getSimpleName() + "." + Constants.REQUEST_PAGES);

				if (bundle.containsKey(Constants.OBJECT_ID_KEY)) {
					String objectId = bundle.getString(Constants.OBJECT_ID_KEY);
					mSectionManager.displayPages(objectId);
				}
			}
		}
	}

	protected void notificationReceived(Intent intent) {

		if (Constants.REQUEST_NOTIFICATIONS.equals(intent.getAction())) {
			final String flag = NotificationsFragment.FLAG_STARTED_FROM_NOTIFICATION_TAP;
			mSectionManager.displayNotificationSection(flag);
		}

		Logger.i(Logger.getClassAndMethod() + intent.getAction());
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("t1", "onCreate");
		handleSearchQuery();
		setTheme();
		configureSlidingMenu();

		ActionBar actionBar = configureActionBar();
		mSectionManager = new SectionManager(BaseSlidingMenuActivity.this, actionBar);
	}

	private void handleSearchQuery() {
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// String query = intent.getStringExtra(SearchManager.QUERY);
			// Logger.i("BaseSlidingMenuActivity#onCreate: Received search query ");
			// TODO: handle search query.
		}
	}

	private void setTheme() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mThemeSelection = prefs.getString(Constants.THEME_PREFERENCES_KEY, Constants.THEME_DEFAULT);
	}

	private void configureSlidingMenu() {
		// Set the "Above View"
		setContentView(R.layout.base_sliding_menu_activity);
		// Set the "Behind View".
		setBehindContentView(R.layout.sliding_menu_frame);
		FragmentTransaction t = this.getFragmentManager().beginTransaction();
		t.replace(R.id.sliding_menu_frame, new SlidingMenuFragment());
		t.commit();

		// Customize the SlidingMenu.
		this.setSlidingActionBarEnabled(true);
		getSlidingMenu().setShadowWidthRes(R.dimen.shadow_width);
		getSlidingMenu().setShadowDrawable(R.drawable.sliding_menu_shadow);
		getSlidingMenu().setBehindOffsetRes(R.dimen.actionbar_home_width);
		getSlidingMenu().setBehindScrollScale(0.25f);

		// Set behind-offset for tablets.
		int orientation = getResources().getConfiguration().orientation;
		boolean isTablet = getResources().getBoolean(R.bool.isTablet);
		if (isTablet) {
			Logger.i(Logger.getClassAndMethod() + "isTablet == true");

			if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
				getSlidingMenu().setBehindOffset(Constants.SLIDING_MENU_BEHIND_OFFSET_LARGE_SCREEN_LANDSCAPE);
			} else {
				getSlidingMenu().setBehindOffset(Constants.SLIDING_MENU_BEHIND_OFFSET_LARGE_SCREEN_PORTRAIT);
			}

		} else {

			Logger.i(Logger.getClassAndMethod() + "isTablet == false");

			if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
				getSlidingMenu().setBehindOffset(Constants.SLIDING_MENU_BEHIND_OFFSET_SMARTPHONE_SCREEN_LANDSCAPE);
			}
		}
	}

	private ActionBar configureActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setBackgroundDrawable(ThemeFactory.getActionBarColorDrawable(mThemeSelection, this));
		// set ActionBar Tab background color

		if (Constants.THEME_IS_COLOR_PICKER_COLOR.equals(mThemeSelection)) {

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			int color = prefs.getInt(Constants.COLOR_PICKER_CHOICE_SLIDING_MENU, 0x000000);
			actionBar.setStackedBackgroundDrawable(new ColorDrawable(color));
		} else {
			actionBar.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(ThemeFactory.getSlidingMenuColor(this, mThemeSelection))));
		}

		return actionBar;

	}

	@Override
	public void onResume() {
		super.onResume();
		if (mTabViewRequestReceiver == null) {
			mTabViewRequestReceiver = new SectionRequestReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Constants.REQUEST_PROFILE);
			intentFilter.addAction(Constants.REQUEST_EVENT_SINGLE);
			intentFilter.addAction(Constants.TABBED_VIEW_REQUEST_GROUP_SINGLE);
			intentFilter.addAction(Constants.REQUEST_PAGES);
			intentFilter.addAction(Constants.REQUEST_PLACE_PROFILE);
			intentFilter.addAction(Constants.REQUEST_NOTIFICATIONS);
			intentFilter.addAction(Constants.REQUEST_IMAGE_PAGER);
			intentFilter.addAction(Constants.REQUEST_ALBUMS);
			intentFilter.addAction(Constants.REQUEST_FRIEND_REQUESTS);
			intentFilter.addAction(Constants.STATE_IMAGE_GRID);
			intentFilter.addAction(Constants.REQUEST_PROFILE_ACTIVITY);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			registerReceiver(mTabViewRequestReceiver, intentFilter);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mTabViewRequestReceiver != null) {
			unregisterReceiver(mTabViewRequestReceiver);
			mTabViewRequestReceiver = null;
		}
	}

	protected void showFirstFragment() {

		if (hasToPopulateMessagesDb) {
			hasToPopulateMessagesDb = false;
			populateMessageDatabase();
		}

		if (hasToPopulateAlbumsDb) {
			hasToPopulateAlbumsDb = false;
			Intent intent = new Intent(this, AlbumsService.class);
			intent.putExtra(Constants.OBJECT_ID_KEY, FBClientApplication.getApplication().getFBConnection().getUserId());
			startService(new Intent(intent));
		}

		try {
			if (SectionManager.STATE_NO_SELECTION.equals(mSectionManager.getTabState())) {
				mSectionManager.displayNewsFeed();
			}
		} catch (java.lang.IllegalStateException e) {
			// TODO decide how to handle
		}
	}

	private void populateMessageDatabase() {
		startService(new Intent(BaseSlidingMenuActivity.this, MessagesService.class));
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString(SECTION_STATE_KEY, mSectionManager.getTabState());
		Log.i("jan3", Logger.getClassAndMethod() + mSectionManager.getTabState());
		savedInstanceState.putString(FB_OBJECT_ID_KEY, mSectionManager.getTabbedViewObjectId());
		if (getActionBar() != null) {
			if (getActionBar().getSelectedTab() != null && getActionBar().getSelectedTab().getPosition() > -1) {
				savedInstanceState.putInt(TAB_SELECTION_KEY, getActionBar().getSelectedTab().getPosition());
			}
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mSectionManager.setTabState(savedInstanceState.getString(SECTION_STATE_KEY));
		mSectionManager.setTabbedViewObjectId(savedInstanceState.getString(FB_OBJECT_ID_KEY));
		mSectionManager.setSelectedTabIndex(savedInstanceState.getInt(TAB_SELECTION_KEY));
		Log.i("jan3", Logger.getClassAndMethod() + mSectionManager.getTabState());
		mSectionManager.recreateState();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Logger.i(BaseSlidingMenuActivity.class.getSimpleName() + "#onNewIntent");
		// Logger.i("action: " + intent.getAction().toString());

		if (Constants.ACTION_PHOTO_UPLOAD_RESULT.equals(intent.getAction())) {
			mSectionManager.displayAlbumsSection();
		}

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// String query = intent.getStringExtra(SearchManager.QUERY);
			Logger.i("BaseSlidingMenuActivity#onNewIntent: Received search query ");
			// TODO: handle search query;
		}

		if (PostCheckinService.ACTION_POSTED_CHECKIN.equals(intent.getAction())) {
			mSectionManager.displayCheckinSection(0);
		}

		if (Constants.REQUEST_NOTIFICATIONS.equals(intent.getAction())) {
			Logger.i(Logger.getClassAndMethod() + Constants.REQUEST_NOTIFICATIONS);
			mSectionManager.displayNotificationSection(NotificationsFragment.FLAG_STARTED_FROM_NOTIFICATION_TAP);
		}

	}

	@Override
	public void onSlidingMenuItemSelected(String selection) {
		Logger.i(Logger.getClassAndMethod(2) + "selection ==" + selection);

		if (Constants.MENU_ITEM_LOGOUT.equalsIgnoreCase(selection)) {

			if (BaseSlidingMenuActivity.this.isFinishing()) {
				return;
			}

			new AlertDialog.Builder(BaseSlidingMenuActivity.this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(getResources().getString(R.string.logout))
					.setMessage(getResources().getString(R.string.do_you_want_to_logout)).setPositiveButton(getResources().getString(R.string.logout), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							executeLogout();
						}

					}).setNegativeButton("Cancel", null).show();

		} else if (Constants.MENU_ITEM_REMOVE_ADS.equalsIgnoreCase(selection)) {

			if (FBClientApplication.getApplication().hasKindleFeatureSet()) {
				if (getResources().getBoolean(R.bool.isPinkVersion)) {
					//is Kindle and Pink
				} else {
					//is not Kindle and Blue
					Uri uriToAmazonAppStore = Uri.parse("amzn://apps.android?p=com.bluebitapps.sociallypremium");
					Intent intentToLaunchAmazonAppStore = new Intent(Intent.ACTION_VIEW, uriToAmazonAppStore);
					try {
						startActivity(intentToLaunchAmazonAppStore);
					} catch (ActivityNotFoundException e) {
						// Amazon AppStore not found, link to Amazon web store.
						Uri uriToAmazonWebStore = Uri.parse("http://www.amazon.com/gp/mas/dl/android?p=com.bluebitapps.sociallypremium");
						Intent intentToLaunchAmazonWebStore = new Intent(Intent.ACTION_VIEW, uriToAmazonWebStore);
						try {
							startActivity(intentToLaunchAmazonWebStore);
						} catch (ActivityNotFoundException ex) {
							Toast.makeText(BaseSlidingMenuActivity.this, R.string.remove_ads_menu_item_error_message, Toast.LENGTH_SHORT).show();
						}
					}
				}
			} else {
				
				if (getResources().getBoolean(R.bool.isPinkVersion)) {
					// is non-Kindle and pink version
					Uri uriToGooglePlayApp = Uri.parse("market://details?id=com.bluebitapps.sociallypinkpremium");
					Intent intentToLaunchGooglePlayApp = new Intent(Intent.ACTION_VIEW, uriToGooglePlayApp);
					try {
						startActivity(intentToLaunchGooglePlayApp);
					} catch (ActivityNotFoundException e) {
						// Google Play store not found, link to web store
						Uri uriToGooglePlayWebStore = Uri.parse("http://google.play.com/store/apps/details?id=com.bluebitapps.sociallypinkpremium");
						Intent intentTolaunchGooglePlayWebStore = new Intent(Intent.ACTION_VIEW, uriToGooglePlayWebStore);
						try {
							startActivity(intentTolaunchGooglePlayWebStore);
						} catch (ActivityNotFoundException ex) {
							Toast.makeText(BaseSlidingMenuActivity.this, R.string.remove_ads_menu_item_error_message, Toast.LENGTH_SHORT).show();
						}
					}

				} else {
					// is non-Kindle and blue version
					Uri uriToGooglePlayApp = Uri.parse("market://details?id=com.bluebitapps.sociallypremium");
					Intent intentToLaunchGooglePlayApp = new Intent(Intent.ACTION_VIEW, uriToGooglePlayApp);
					try {
						startActivity(intentToLaunchGooglePlayApp);
					} catch (ActivityNotFoundException e) {
						// Google Play store not found, link to web store
						Uri uriToGooglePlayWebStore = Uri.parse("http://google.play.com/store/apps/details?id=com.bluebitapps.sociallypremium");
						Intent intentTolaunchGooglePlayWebStore = new Intent(Intent.ACTION_VIEW, uriToGooglePlayWebStore);
						try {
							startActivity(intentTolaunchGooglePlayWebStore);
						} catch (ActivityNotFoundException ex) {
							Toast.makeText(BaseSlidingMenuActivity.this, R.string.remove_ads_menu_item_error_message, Toast.LENGTH_SHORT).show();
						}
					}
				}
			}

		} else {

			toggle();// moves menu back to closed state
			Crouton.cancelAllCroutons();
			mSectionManager.displaySelection(selection);
		}
	}

	private void executeLogout() {
		deleteAllDataBaseTableRows();
		FBClientApplication.getApplication().getFBConnection().setForceAuthFlag(true);

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					FBClientApplication.getApplication().getFBConnection().getFacebook().logout(getApplicationContext());
				} catch (BadTokenException e) {
					Logger.i(Logger.getClassAndMethod() + e);
				} catch (MalformedURLException e) {
					Logger.i(Logger.getClassAndMethod() + e);
				} catch (IOException e) {
					Logger.i(Logger.getClassAndMethod() + e);
				}
			}
		});
		thread.start();

		finish();

	}

	private void deleteAllDataBaseTableRows() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					FBClientApplication app = FBClientApplication.getApplication();

					app.getNewsFeedData(NewsFeedData.DB_NAME_NEWSFEED).deleteAllRows();
					app.getNewsFeedData(NewsFeedData.DB_NAME_NEWSFEED_OLDER).deleteAllRows();
					app.getNewsFeedData(NewsFeedData.DB_NAME_WALL).deleteAllRows();
					app.getNewsFeedData(NewsFeedData.DB_NAME_WALL_OLDER).deleteAllRows();
					app.getFriendsData().deleteAllRows();
					app.getAlbumsData().deleteAllRows();
					app.getNotificationsData().deleteAllRows();
					app.getEventsData().deleteAllRows();
					app.getBirthdaysData().deleteAllRows();
					app.getGroupsData().deleteAllRows();
					app.getMessagesData().deleteAllRows();
					app.getLikesData().deleteAllRows();
					app.getCheckinsData().deleteAllRows();
					app.getPageData().deleteAllRows();
				} catch (Exception e) {
					Logger.i(Logger.getClassAndMethod() + e.toString());
				}
			}
		});
		thread.start();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			if (isShowingTextSettings) {
				isShowingTextSettings = false;
				getActionBar().setDisplayHomeAsUpEnabled(false);
				getFragmentManager().popBackStackImmediate();
				return true;

			} else {

				toggle();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onNewsFeedTapped(NewsFeedItem item) {
		Intent intent = new Intent(this, NewsFeedItemActivity.class);
		intent.putExtra("newsfeeditem", item);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	@Override
	public void onSearchQuery(String query) {
		// Hide soft keyboard
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

		toggle();
		SearchFragment fragment = new SearchFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constants.SEARCH_QUERY_KEY, query);
		fragment.setArguments(bundle);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(android.R.id.content, fragment).commit();
	}

	public static class CustomTabListener<T extends Fragment> implements TabListener {
		Fragment fragment;

		public CustomTabListener(Fragment fragment) {
			this.fragment = fragment;
		}

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			ft.replace(android.R.id.content, fragment);
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// ft.remove(fragment);
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i("feb4", Logger.getClassAndMethod());

		switch (requestCode) {
		case Constants.COLOR_PICKER_LAUNCH_REQUEST_CODE:
			Log.i("feb4", Logger.getClassAndMethod());

			Intent intent = getIntent();// new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			finish();
			startActivity(intent);
			break;
		}
	}

	/**
	 * close app if there are is no fragment on backstack and back is pressed.
	 */

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (getFragmentManager().getBackStackEntryCount() == 1) {

				if (ExitUtil.isLastActivity(BaseSlidingMenuActivity.this)) {
					showDialog();
				}

				return true;
			} else {
				getFragmentManager().popBackStack();
			}
		}

		return false;
	}

	private void showDialog() {

		new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(getResources().getString(R.string.quit_app))
				.setMessage(getResources().getString(R.string.do_you_want_to_exit_socially)).setPositiveButton(getResources().getString(R.string.exit), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						BaseSlidingMenuActivity.this.finish();
					}

				}).setNegativeButton(getResources().getString(R.string.stay), null).show();
	}

}