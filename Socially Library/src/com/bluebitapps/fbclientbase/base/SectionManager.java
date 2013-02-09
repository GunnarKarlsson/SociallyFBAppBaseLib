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

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.about.AboutFragment;
import com.bluebitapps.fbclientbase.albums.Album;
import com.bluebitapps.fbclientbase.albums.AlbumsFragment;
import com.bluebitapps.fbclientbase.base.BaseSlidingMenuActivity.CustomTabListener;
import com.bluebitapps.fbclientbase.chat.ChatRosterFragment;
import com.bluebitapps.fbclientbase.checkins.CheckinsFragment;
import com.bluebitapps.fbclientbase.checkins.NearbyPlacesFragment;
import com.bluebitapps.fbclientbase.checkins.PlaceMapFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.events.BirthdaysFragment;
import com.bluebitapps.fbclientbase.events.EventUserListFragment;
import com.bluebitapps.fbclientbase.events.EventsFragment;
import com.bluebitapps.fbclientbase.events.InvitesFragment;
import com.bluebitapps.fbclientbase.friendrequests.FriendRequestsFragment;
import com.bluebitapps.fbclientbase.friends.FriendsFragment;
import com.bluebitapps.fbclientbase.groups.GroupMembersFragment;
import com.bluebitapps.fbclientbase.groups.GroupsFragment;
import com.bluebitapps.fbclientbase.likes.LikesFragment;
import com.bluebitapps.fbclientbase.messages.MessagesFragment;
import com.bluebitapps.fbclientbase.newsfeed.NewsFeedFragment;
import com.bluebitapps.fbclientbase.notifications.NotificationsFragment;
import com.bluebitapps.fbclientbase.page.PageFragment;
import com.bluebitapps.fbclientbase.photos.ImageGridFragment;
import com.bluebitapps.fbclientbase.photos.ImagePagerActivity;
import com.bluebitapps.fbclientbase.photos.Photo;
import com.bluebitapps.fbclientbase.place.PlaceProfileFragment;
import com.bluebitapps.fbclientbase.profile.ProfileActivity;
import com.bluebitapps.fbclientbase.profile.ProfileFragment;
import com.bluebitapps.fbclientbase.settings.SettingsFragment;
import com.bluebitapps.fbclientbase.subscriptions.SubscriptionsFragment;
import com.bluebitapps.fbclientbase.theme.ColorPickerActivity;
import com.bluebitapps.fbclientbase.theme.ColorPickerFragment;
import com.bluebitapps.fbclientbase.theme.ThemeSelectionFragment;
import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;

public class SectionManager {

	private static final String STATE_NOTIFICATIONS = "state notifications";
	private static final String STATE_CHECKIN = "state checkin";
	private static final String STATE_EVENTS = "state events";
	private static final String STATE_GROUP_DETAILS = "state group details";
	private static final String STATE_PROFILE = "state profile";
	private static final String STATE_PAGES = "state pages";
	private static final String STATE_ABOUT = "state about";
	private static final String STATE_PLACE_PROFILE = "state place profile";
	private static final String STATE_THEMES = "state themes";
	private static final String STATE_NEWSFEED = "state newsfeed";
	private static final String STATE_FRIENDS = "state friends";
	private static final String STATE_PHOTOS = "state photos";
	private static final String STATE_GROUPS = "state groups";
	private static final String STATE_MESSAGES = "state messages";
	private static final String STATE_CHAT = "state chat";
	private static final String STATE_SUBSCRIPTIONS = "state subscriptions";
	private static final String STATE_FRIEND_REQUESTS = "state friend requests";
	private static final String STATE_SETTINGS = "state settings";
	private static final String STATE_LIKES = "state likes";
	private static final String STATE_COLOR_PICKER = "state color picker";
	private static final String STATE_EVENT_DETAILS = "state event details";
	private static final String STATE_IMAGE_PAGER = "state image pager";
	private static final String STATE_IMAGE_GRID = "state image grid";

	public static final String STATE_NO_SELECTION = "state selection";

	private Activity mActivity;
	private ActionBar mActionBar;
	private String mTabState;
	private String mTabbedViewObjectId;

	private GroupsFragment mGroupsFragment;
	private ThemeSelectionFragment mThemeSelectionFragment;
	private MessagesFragment mMessagesFragment;
	private FriendsFragment mFriendFragment;
	private AlbumsFragment mAlbumsFragment;
	private NewsFeedFragment mNewsFeedFragment;
	private ChatRosterFragment mChatRosterFragment;
	private SettingsFragment mSettingsFragment;
	private LikesFragment mLikesFragment;

	private SubscriptionsFragment mSubscriptionsFragment;
	private FriendRequestsFragment mFriendRequestsFragment;

	private int mSelectedTabIndex;

	public SectionManager(Activity activity, ActionBar actionBar) {
		mActivity = activity;
		mActionBar = actionBar;
		mTabState = STATE_NO_SELECTION;
		mSelectedTabIndex = 0;
	}

	private void cleanUpActionBarAndCroutons() {

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {

					mActionBar.removeAllTabs();
				}
			});
		}

		OutputUtil.cancelAllCroutons(mActivity);

	}

	public void showFirstFragment() {

		displayNewsFeed();
	}

	public void displaySelection(String selection) {

		mSelectedTabIndex = 0;

		if (Constants.MENU_ITEM_THEMES.equals(selection)) {
			displayThemeSection();
		} else if (Constants.MENU_ITEM_NEWSFEED.equals(selection)) {
			displayNewsFeed();
		} else if (Constants.MENU_ITEM_FRIENDS.equals(selection)) {
			displayFriendsSection();
		} else if (Constants.MENU_ITEM_PHOTOS.equals(selection)) {
			displayAlbumsSection();
		} else if (Constants.MENU_ITEM_NOTIFICATIONS.equals(selection)) {
			displayNotificationSection(null);
		} else if (Constants.MENU_ITEM_MESSAGES.equals(selection)) {
			displayMessagesSection();
		} else if (Constants.MENU_ITEM_CHECKINS.equals(selection)) {
			displayCheckinSection(0);
		} else if (Constants.MENU_ITEM_EVENTS.equals(selection)) {
			displayEventSection(0);
		} else if (Constants.MENU_ITEM_GROUPS.equals(selection)) {
			displayGroupsSection();
		} else if (Constants.MENU_ITEM_CHAT.equals(selection)) {
			displayChatSection();
		} else if (Constants.MENU_ITEM_PROFILE.equals(selection)) {
			displayUserProfile(FBClientApplication.getApplication().getFBConnection().getUserId());
		} else if (Constants.MENU_ITEM_LIKES.equals(selection)) {
			displayLikes();
		} else if (Constants.MENU_ITEM_SUBSCRIPTIONS.equals(selection)) {
			displaySubscriptions();
		} else if (Constants.MENU_ITEM_FRIEND_REQUESTS.equals(selection)) {
			displayFriendRequests();
		} else if (Constants.MENU_ITEM_SETTINGS.equals(selection)) {
			displaySettingsSection();
		} else if (Constants.MENU_ITEM_ABOUT.equals(selection)) {
			displayAboutSection();
		} else if (Constants.MENU_ITEM_SUB_ITEM_FRIENDS_RECENT.equals(selection)) {
			displayCheckinSection(0);
		} else if (Constants.MENU_ITEM_SUB_ITEM_NEARBY_LOCATIONS.equals(selection)) {
			displayCheckinSection(1);
		} else if (Constants.MENU_ITEM_SUB_ITEM_EVENT_LIST.equals(selection)) {
			displayEventSection(0);
		} else if (Constants.MENU_ITEM_SUB_ITEM_EVENT_INVITED.equals(selection)) {
			displayEventSection(1);
		} else if (Constants.MENU_ITEM_SUB_ITEM_BIRTHDAYS.equals(selection)) {
			displayEventSection(2);
		} else if (Constants.MENU_ITEM_SUB_ITEM_ME.equals(selection)) {
			mSelectedTabIndex = 0;
			displayUserProfile(FBClientApplication.getApplication().getFBConnection().getUserId());
		} else if (Constants.MENU_ITEM_SUB_ITEM_WALL.equals(selection)) {
			mSelectedTabIndex = 1;
			displayUserProfile(FBClientApplication.getApplication().getFBConnection().getUserId());
		} else if (Constants.MENU_ITEM_SUB_ITEM_PHOTOS.equals(selection)) {
			mSelectedTabIndex = 2;
			displayUserProfile(FBClientApplication.getApplication().getFBConnection().getUserId());
		} else if(Constants.MENU_ITEM_COLOR_PICKER.equals(selection)){
			displayColorPicker();
		}
	}

	public void recreateState() {

		Log.i("jan3", Logger.getClassAndMethod() + mTabState);

		if (STATE_THEMES.equals(mTabState)) {
			displayThemeSection();
		} else if (STATE_NEWSFEED.equals(mTabState)) {
			displayNewsFeed();
		} else if (STATE_FRIENDS.equals(mTabState)) {
			displayFriendsSection();
		} else if (STATE_PHOTOS.equals(mTabState)) {
			displayAlbumsSection();
		} else if (STATE_NOTIFICATIONS.equals(mTabState)) {
			displayNotificationSection(null);
		} else if (STATE_MESSAGES.equals(mTabState)) {
			displayMessagesSection();
		} else if (STATE_CHECKIN.equals(mTabState)) {
			displayCheckinSection(mSelectedTabIndex);
		} else if (STATE_EVENTS.equals(mTabState)) {
			displayEventSection(mSelectedTabIndex);
		} else if (STATE_EVENT_DETAILS.equals(mTabState)) {
			displayEventDetails(null, getTabbedViewObjectId());
		} else if (STATE_GROUPS.equals(mTabState)) {
			displayGroupsSection();
		} else if (STATE_GROUP_DETAILS.equals(mTabState)) {
			displayGroupDetails(getTabbedViewObjectId(), null);
		} else if (STATE_CHAT.equals(mTabState)) {
			displayChatSection();
		} else if (STATE_PROFILE.equals(mTabState)) {
			displayUserProfile(getTabbedViewObjectId());
		} else if (STATE_PLACE_PROFILE.equals(mTabState)) {
			displayPlaceProfile(getTabbedViewObjectId());
		} else if (STATE_PAGES.equals(mTabState)) {
			displayPages(getTabbedViewObjectId());
		} else if (STATE_LIKES.equals(mTabState)) {
			displayLikes();
		} else if (STATE_SUBSCRIPTIONS.equals(mTabState)) {
			displaySubscriptions();
		} else if (STATE_FRIEND_REQUESTS.equals(mTabState)) {
			displayFriendRequests();
		} else if (STATE_ABOUT.equals(mTabState)) {
			Log.i("jan3", Logger.getClassAndMethod() + "if statement selected");
			displayAboutSection();
		} else if (STATE_SETTINGS.equals(mTabState)) {
			displaySettingsSection();
		} else if (STATE_IMAGE_GRID.equals(mTabState)) {
			displayImageGrid(getTabbedViewObjectId(), null);
		} else if(STATE_COLOR_PICKER.equals(mTabState)){
			displayColorPicker();
		} else {
			Logger.i("recreate: default no tabs");

			if (mActivity != null) {
				mActivity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mActionBar.removeAllTabs();

					}
				});
			}

		}
	}

	public void displayColorPicker(){
			cleanUpActionBarAndCroutons();

			mTabState = STATE_COLOR_PICKER;
			Intent intent = new Intent(mActivity, ColorPickerActivity.class);
			mActivity.startActivityForResult(intent, Constants.COLOR_PICKER_LAUNCH_REQUEST_CODE);
	}
	
	public void displayImageGrid(String objectId, String title) {
		cleanUpActionBarAndCroutons();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

				}
			});
		}

		mTabState = STATE_IMAGE_GRID;
		mTabbedViewObjectId = objectId;

		Bundle bundle = new Bundle();
		bundle.putString(Album.ALBUM_ID_KEY, objectId);
		bundle.putString(Album.ALBUM_NAME_KEY, title);
		ImageGridFragment fragment = new ImageGridFragment();
		fragment.setArguments(bundle);
		FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.replace(android.R.id.content, fragment).commit();

	}

	public void displayImagePager(ArrayList<Photo> photos, String flag) {

		cleanUpActionBarAndCroutons();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

				}
			});
		}

		mTabState = STATE_IMAGE_PAGER;
		mTabbedViewObjectId = FBClientApplication.getApplication().getFBConnection().getUserId();

		Intent intent = new Intent(mActivity, ImagePagerActivity.class);
		intent.putParcelableArrayListExtra("photos", photos);
		if (flag != null) {
			intent.putExtra(Constants.PHOTO_ACCESS_VIA_NOTIFICATION, flag);
		}
		int pos = 0;
		intent.putExtra("position", pos);
		mActivity.startActivity(intent);
	}

	public void displayPages(final String userId) {

		mTabState = STATE_PAGES;
		mTabbedViewObjectId = userId;

		cleanUpActionBarAndCroutons();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
					PageFragment pageFragment = PageFragment.newInstance(userId, null);
					pageFragment.setRetainInstance(true);
					AlbumsFragment albumsFragment = AlbumsFragment.newInstance(userId, Constants.STATE_PROFILE, null, false);
					albumsFragment.setRetainInstance(true);
					NewsFeedFragment newsFeedFragment = NewsFeedFragment.newInstance(userId, Constants.STATE_PROFILE, null, false);
					newsFeedFragment.setRetainInstance(true);

					ActionBar.Tab tab = mActionBar.newTab().setText("Profile").setTabListener(new CustomTabListener<PageFragment>(pageFragment));
					mActionBar.addTab(tab);
					tab = mActionBar.newTab().setText("Wall").setTabListener(new CustomTabListener<NewsFeedFragment>(newsFeedFragment));
					mActionBar.addTab(tab);
					tab = mActionBar.newTab().setText("Photos").setTabListener(new CustomTabListener<AlbumsFragment>(albumsFragment));
					mActionBar.addTab(tab);

					mActionBar.setSelectedNavigationItem(mSelectedTabIndex);
				}
			});
		}

	}

	public void displayAlbumsSection() {

		cleanUpActionBarAndCroutons();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

				}
			});
		}

		mTabState = STATE_PHOTOS;
		mTabbedViewObjectId = FBClientApplication.getApplication().getFBConnection().getUserId();

		if (mAlbumsFragment == null) {
			mAlbumsFragment = AlbumsFragment.newInstance(FBClientApplication.getApplication().getFBConnection().getUserId(), Constants.STATE_USER_ALBUM, null, false);
			Bundle b = new Bundle();
			b.putString("key", "TEST_VALUE");
		}
		android.app.FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.replace(android.R.id.content, mAlbumsFragment).commit();
	}

	public void displayAboutSection() {

		Log.i("jan3", Logger.getClassAndMethod());

		cleanUpActionBarAndCroutons();

		mTabState = STATE_ABOUT;
		mTabbedViewObjectId = FBClientApplication.getApplication().getFBConnection().getUserId();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

					AboutFragment aboutFragment = AboutFragment.newInstance(AboutFragment.STATE_ABOUT);
					aboutFragment.setRetainInstance(true);
					AboutFragment licenseFragment = AboutFragment.newInstance(AboutFragment.STATE_LICENSE);
					licenseFragment.setRetainInstance(true);

					ActionBar.Tab tab = mActionBar.newTab().setText("About").setTabListener(new CustomTabListener<AboutFragment>(aboutFragment));
					mActionBar.addTab(tab);
					tab = mActionBar.newTab().setText("License").setTabListener(new CustomTabListener<AboutFragment>(licenseFragment));
					mActionBar.addTab(tab);

					mActionBar.setSelectedNavigationItem(mSelectedTabIndex);
				}
			});
		}
	}

	public void displayCheckinSection(int index) {

		mSelectedTabIndex = index;
		mTabState = STATE_CHECKIN;
		mTabbedViewObjectId = FBClientApplication.getApplication().getFBConnection().getUserId();

		cleanUpActionBarAndCroutons();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
					CheckinsFragment friendsCheckinsFragment = CheckinsFragment.newInstance(FBClientApplication.getApplication().getFBConnection().getUserId());
					friendsCheckinsFragment.setRetainInstance(true);
					NearbyPlacesFragment nearbyPlacesFragment = new NearbyPlacesFragment();
					nearbyPlacesFragment.setRetainInstance(true);

					ActionBar.Tab tab = mActionBar.newTab().setText("Friends' recent").setTabListener(new CustomTabListener<CheckinsFragment>(friendsCheckinsFragment));
					mActionBar.addTab(tab);
					tab = mActionBar.newTab().setText("Nearby").setTabListener(new CustomTabListener<NewsFeedFragment>(nearbyPlacesFragment));
					mActionBar.addTab(tab);

					mActionBar.setSelectedNavigationItem(mSelectedTabIndex);
				}
			});
		}
	}

	public void displayGroupDetails(final String groupId, final String title) {

		mTabbedViewObjectId = groupId;
		cleanUpActionBarAndCroutons();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
					mActionBar.setTitle(title);

					ProfileFragment profileFragment = ProfileFragment.newInstance(groupId, Constants.STATE_PROFILE, title, false);
					profileFragment.setRetainInstance(true);
					NewsFeedFragment wallFragment = NewsFeedFragment.newInstance(groupId, Constants.STATE_PROFILE, title, false);
					wallFragment.setRetainInstance(true);
					GroupMembersFragment membersFragment = GroupMembersFragment.newInstance(groupId, title, false);
					membersFragment.setRetainInstance(true);

					ActionBar.Tab tab = mActionBar.newTab().setText("Info").setTabListener(new CustomTabListener<ProfileFragment>(profileFragment));
					mActionBar.addTab(tab);
					tab = mActionBar.newTab().setText("Wall").setTabListener(new CustomTabListener<NewsFeedFragment>(wallFragment));
					mActionBar.addTab(tab);
					tab = mActionBar.newTab().setText("Members").setTabListener(new CustomTabListener<GroupMembersFragment>(membersFragment));
					mActionBar.addTab(tab);

					mActionBar.setSelectedNavigationItem(mSelectedTabIndex);
				}
			});
		}

	}

	public void displayUserProfileActivity(final String userId, final String title) {
		Intent intent = new Intent(mActivity, ProfileActivity.class);
		intent.putExtra(Constants.OBJECT_ID_KEY, userId);
		intent.putExtra(Constants.OBJECT_TITLE_KEY, title);
		intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
		mActivity.startActivity(intent);
		mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	public void displayUserProfile(final String userId) {

		// InputUtil.hideKeyboard(mActivity);

		cleanUpActionBarAndCroutons();

		mTabState = STATE_PROFILE;
		mTabbedViewObjectId = userId;

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
					mActionBar.setTitle("Your Profile");// TODO change to any
														// users name

					ProfileFragment profileFragment = ProfileFragment.newInstance(userId, Constants.STATE_PROFILE, null, false);
					profileFragment.setRetainInstance(true);
					AlbumsFragment albumsFragment = AlbumsFragment.newInstance(userId, Constants.STATE_PROFILE, null, false);
					albumsFragment.setRetainInstance(true);
					NewsFeedFragment newsFeedFragment = NewsFeedFragment.newInstance(userId, Constants.STATE_PROFILE, null, false);
					newsFeedFragment.setRetainInstance(true);

					ActionBar.Tab tab = mActionBar.newTab().setText("Profile").setTabListener(new CustomTabListener<ProfileFragment>(profileFragment));
					mActionBar.addTab(tab);
					tab = mActionBar.newTab().setText("Wall").setTabListener(new CustomTabListener<NewsFeedFragment>(newsFeedFragment));
					mActionBar.addTab(tab);
					tab = mActionBar.newTab().setText("Photos").setTabListener(new CustomTabListener<AlbumsFragment>(albumsFragment));
					mActionBar.addTab(tab);

					Logger.i(Logger.getClassAndMethod() + "mSelectedTabIndex" + mSelectedTabIndex);
					mActionBar.setSelectedNavigationItem(mSelectedTabIndex);

				}
			});
		}

	}

	public void displayPlaceProfile(final String placeId) {

		// InputUtil.hideKeyboard(mActivity);

		cleanUpActionBarAndCroutons();
		mTabState = STATE_PLACE_PROFILE;
		mTabbedViewObjectId = placeId;

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
					mActionBar.setTitle("Place Profile");// TODO change to
															// place's name

					PlaceProfileFragment placeProfileFragment = PlaceProfileFragment.newInstance(placeId, null, false);
					placeProfileFragment.setRetainInstance(true);

					AlbumsFragment albumsFragment = AlbumsFragment.newInstance(placeId, Constants.STATE_PROFILE, null, false);
					albumsFragment.setRetainInstance(true);

					PlaceMapFragment mapFragment = PlaceMapFragment.newInstance(placeId, null);
					mapFragment.setRetainInstance(true);

					NewsFeedFragment newsFeedFragment = NewsFeedFragment.newInstance(placeId, Constants.STATE_PROFILE, null, false);
					newsFeedFragment.setRetainInstance(true);

					ActionBar.Tab tab = mActionBar.newTab().setText("Profile").setTabListener(new CustomTabListener<ProfileFragment>(placeProfileFragment));
					mActionBar.addTab(tab);

					tab = mActionBar.newTab().setText("Wall").setTabListener(new CustomTabListener<NewsFeedFragment>(newsFeedFragment));
					mActionBar.addTab(tab);

					tab = mActionBar.newTab().setText("Photos").setTabListener(new CustomTabListener<AlbumsFragment>(albumsFragment));
					mActionBar.addTab(tab);

					tab = mActionBar.newTab().setText("Map").setTabListener(new CustomTabListener<PlaceMapFragment>(mapFragment));
					mActionBar.addTab(tab);

					mActionBar.setSelectedNavigationItem(mSelectedTabIndex);

				}
			});
		}

	}

	public void displayNotificationSection(String flag) {

		cleanUpActionBarAndCroutons();
		mTabState = STATE_NOTIFICATIONS;
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		mTabbedViewObjectId = FBClientApplication.getApplication().getFBConnection().getUserId();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
					mActionBar.setTitle("Notificatons");
				}
			});
		}

		NotificationsFragment unreadFragment = NotificationsFragment.newInstance(flag);
		unreadFragment.setRetainInstance(true);

		FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.replace(android.R.id.content, unreadFragment).commit();
	}

	public void displayEventSection(int index) {

		cleanUpActionBarAndCroutons();
		mTabState = STATE_EVENTS;
		mTabbedViewObjectId = FBClientApplication.getApplication().getFBConnection().getUserId();
		mSelectedTabIndex = index;

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
					mActionBar.setTitle("Events");

					EventsFragment eventsFragment = EventsFragment.newInstance(null, null);
					eventsFragment.setRetainInstance(true);
					InvitesFragment invitesFragment = InvitesFragment.newInstance();
					invitesFragment.setRetainInstance(true);
					BirthdaysFragment birthdaysFragment = new BirthdaysFragment();
					birthdaysFragment.setRetainInstance(true);

					ActionBar.Tab tab = mActionBar.newTab().setText("Attending").setTabListener(new CustomTabListener<Fragment>(eventsFragment));
					mActionBar.addTab(tab);
					tab = mActionBar.newTab().setText("Invitations").setTabListener(new CustomTabListener<Fragment>(invitesFragment));
					mActionBar.addTab(tab);
					tab = mActionBar.newTab().setText("Birthdays").setTabListener(new CustomTabListener<Fragment>(birthdaysFragment));
					mActionBar.addTab(tab);

					mActionBar.setSelectedNavigationItem(mSelectedTabIndex);

				}
			});
		}

	}

	public void displayEventDetails(final String eventTitle, final String objectId) {

		cleanUpActionBarAndCroutons();
		mTabState = STATE_EVENT_DETAILS;
		mTabbedViewObjectId = objectId;

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
					if (StringUtil.notEmpty(eventTitle)) {
						mActionBar.setTitle(eventTitle);
					} else {
						mActionBar.setTitle("");
					}

					ProfileFragment profileFragment = ProfileFragment.newInstance(objectId, Constants.STATE_PROFILE, null, false);
					profileFragment.setRetainInstance(true);
					NewsFeedFragment wallFragment = NewsFeedFragment.newInstance(objectId, Constants.STATE_PROFILE, null, false);
					wallFragment.setRetainInstance(true);
					EventUserListFragment attendingFragment = EventUserListFragment.newInstance(Constants.STATE_EVENT_ATTENDING, objectId, null, false);
					attendingFragment.setRetainInstance(true);

					EventUserListFragment declinedFragment = EventUserListFragment.newInstance(Constants.STATE_EVENT_DECLINED, objectId, null, false);
					declinedFragment.setRetainInstance(true);
					EventUserListFragment invitedFragment = EventUserListFragment.newInstance(Constants.STATE_EVENT_INVITED, objectId, null, false);
					invitedFragment.setRetainInstance(true);
					EventUserListFragment maybeFragment = EventUserListFragment.newInstance(Constants.STATE_EVENT_MAYBE, objectId, null, false);
					maybeFragment.setRetainInstance(true);

					ActionBar.Tab tab = mActionBar.newTab().setText("Event Info").setTabListener(new CustomTabListener<Fragment>(profileFragment));
					mActionBar.addTab(tab);
					tab = mActionBar.newTab().setText("Wall").setTabListener(new CustomTabListener<Fragment>(wallFragment));
					mActionBar.addTab(tab);

					tab = mActionBar.newTab().setText("Invited").setTabListener(new CustomTabListener<Fragment>(invitedFragment));
					mActionBar.addTab(tab);
					tab = mActionBar.newTab().setText("Attending").setTabListener(new CustomTabListener<Fragment>(attendingFragment));
					mActionBar.addTab(tab);
					tab = mActionBar.newTab().setText("Declined").setTabListener(new CustomTabListener<Fragment>(declinedFragment));
					mActionBar.addTab(tab);
					tab = mActionBar.newTab().setText("Maybe").setTabListener(new CustomTabListener<Fragment>(maybeFragment));
					mActionBar.addTab(tab);

					mActionBar.setSelectedNavigationItem(mSelectedTabIndex);

				}
			});
		}

	}

	public void displayThemeSection() {
		cleanUpActionBarAndCroutons();
		mTabState = STATE_THEMES;
		mTabbedViewObjectId = FBClientApplication.getApplication().getFBConnection().getUserId();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

				}
			});
		}

		if (mThemeSelectionFragment == null) {
			mThemeSelectionFragment = new ThemeSelectionFragment();
		}
		FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.replace(android.R.id.content, mThemeSelectionFragment).commit();
/*
		ColorPickerFragment colorPickerFragment = new ColorPickerFragment();
		colorPickerFragment.setRetainInstance(true);

		ActionBar.Tab tab = mActionBar.newTab().setText("Themes").setTabListener(new CustomTabListener<ThemeSelectionFragment>(mThemeSelectionFragment));
		mActionBar.addTab(tab);
		tab = mActionBar.newTab().setText("ColorPicker").setTabListener(new CustomTabListener<ColorPickerFragment>(colorPickerFragment));
		mActionBar.addTab(tab);

		mActionBar.setSelectedNavigationItem(mSelectedTabIndex);
		*/

	}

	public void displayNewsFeed() {

		cleanUpActionBarAndCroutons();
		mTabState = STATE_NEWSFEED;
		mTabbedViewObjectId = FBClientApplication.getApplication().getFBConnection().getUserId();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

				}
			});
		}

		if (mNewsFeedFragment == null) {
			mNewsFeedFragment = NewsFeedFragment.newInstance(FBClientApplication.getApplication().getFBConnection().getUserId(), Constants.STATE_NEWSFEED_CURRENT_USER, null, false);
		}
		FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		try {
			ft.replace(android.R.id.content, mNewsFeedFragment).commit();
		} catch (IllegalArgumentException e) {
			// TODO: handle e
		}
	}

	public void displaySettingsSection() {
		cleanUpActionBarAndCroutons();
		mTabState = STATE_SETTINGS;
		mTabbedViewObjectId = FBClientApplication.getApplication().getFBConnection().getUserId();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

				}
			});
		}

		if (mSettingsFragment == null) {
			mSettingsFragment = new SettingsFragment();
		}

		SettingsFragment fragment = new SettingsFragment();

		FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.replace(android.R.id.content, mSettingsFragment).commit();
	}

	public void displayChatSection() {
		cleanUpActionBarAndCroutons();
		mTabState = STATE_CHAT;
		mTabbedViewObjectId = FBClientApplication.getApplication().getFBConnection().getUserId();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

				}
			});
		}

		if (mChatRosterFragment == null) {
			mChatRosterFragment = new ChatRosterFragment();
		}

		FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.replace(android.R.id.content, mChatRosterFragment).commit();
	}

	public void displayFriendsSection() {
		cleanUpActionBarAndCroutons();
		mTabState = STATE_FRIENDS;
		mTabbedViewObjectId = FBClientApplication.getApplication().getFBConnection().getUserId();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

				}
			});
		}

		if (mFriendFragment == null) {
			mFriendFragment = new FriendsFragment();
			Bundle bundle = new Bundle();
			mFriendFragment.setArguments(bundle);
		}
		FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.replace(android.R.id.content, mFriendFragment).commit();
	}

	public void displayMessagesSection() {

		cleanUpActionBarAndCroutons();
		mTabState = STATE_MESSAGES;
		mTabbedViewObjectId = FBClientApplication.getApplication().getFBConnection().getUserId();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

				}
			});
		}

		if (mMessagesFragment == null) {
			mMessagesFragment = new MessagesFragment();
		}

		FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.replace(android.R.id.content, mMessagesFragment).commit();
	}

	public void displayGroupsSection() {

		cleanUpActionBarAndCroutons();
		mTabState = STATE_GROUPS;
		mTabbedViewObjectId = FBClientApplication.getApplication().getFBConnection().getUserId();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

				}
			});
		}

		if (mGroupsFragment == null) {
			mGroupsFragment = GroupsFragment.newInstance(null, null);
		}

		FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.replace(android.R.id.content, mGroupsFragment).commit();
	}

	public void displayFriendRequests() {

		cleanUpActionBarAndCroutons();
		mTabState = STATE_FRIEND_REQUESTS;
		mTabbedViewObjectId = FBClientApplication.getApplication().getFBConnection().getUserId();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
				}
			});
		}

		// if (mFriendRequestsFragment == null) {
		FriendRequestsFragment fragment = new FriendRequestsFragment();
		// }

		FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.replace(android.R.id.content, fragment).commit();
	}

	public void displayLikes() {

		cleanUpActionBarAndCroutons();
		mTabState = STATE_LIKES;
		mTabbedViewObjectId = FBClientApplication.getApplication().getFBConnection().getUserId();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

				}
			});
		}

		if (mLikesFragment == null) {
			mLikesFragment = LikesFragment.newInstance(FBClientApplication.getApplication().getFBConnection().getUserId(), STATE_NO_SELECTION, null);
		}
		FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.replace(android.R.id.content, mLikesFragment).commit();

	}

	public void displaySubscriptions() {

		cleanUpActionBarAndCroutons();
		mTabState = STATE_SUBSCRIPTIONS;
		mTabbedViewObjectId = FBClientApplication.getApplication().getFBConnection().getUserId();

		if (mActivity != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

				}
			});
		}

		if (mSubscriptionsFragment == null) {
			mSubscriptionsFragment = new SubscriptionsFragment();
		}
		FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.replace(android.R.id.content, mSubscriptionsFragment).commit();
	}

	public void setSelectedTabIndex(int selection) {
		mSelectedTabIndex = selection;
	}

	public int getSelectedTabIndex() {
		return mSelectedTabIndex;
	}

	public void setTabbedViewObjectId(String objectId) {
		mTabbedViewObjectId = objectId;
	}

	public String getTabbedViewObjectId() {
		return mTabbedViewObjectId;
	}

	public void setTabState(String tabState) {
		mTabState = tabState;
	}

	public String getTabState() {
		return mTabState;
	}
}