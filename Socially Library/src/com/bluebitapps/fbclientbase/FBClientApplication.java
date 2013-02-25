/**
m * Copyright 2012 Gunnar Karlsson
 */

package com.bluebitapps.fbclientbase;

import java.util.Locale;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bluebitapps.fbclientbase.albums.AlbumsData;
import com.bluebitapps.fbclientbase.chat.SASLXFacebookPlatformMechanism;
import com.bluebitapps.fbclientbase.checkins.CheckinsData;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.events.BirthdaysData;
import com.bluebitapps.fbclientbase.events.EventsData;
import com.bluebitapps.fbclientbase.fbconnection.FBConnection;
import com.bluebitapps.fbclientbase.friends.FriendsData;
import com.bluebitapps.fbclientbase.groups.GroupsData;
import com.bluebitapps.fbclientbase.likes.LikesData;
import com.bluebitapps.fbclientbase.messages.MessageThreadData;
import com.bluebitapps.fbclientbase.newsfeed.NewsFeedData;
import com.bluebitapps.fbclientbase.notifications.NotificationAlarm;
import com.bluebitapps.fbclientbase.notifications.NotificationsData;
import com.bluebitapps.fbclientbase.page.PageData;
import com.bluebitapps.fbclientbase.profile.ProfileData;

@ReportsCrashes(formKey = "dHdFOEE2X09rY1ZsaWZ3UG9VdXVVRmc6MQ")
public class FBClientApplication extends Application implements OnSharedPreferenceChangeListener {

	private static FBClientApplication mApplication;

	private FBConnection mFBConnection;

	private NewsFeedData mNewsFeedData;
	private NewsFeedData mNewsFeedDataOlder;
	private NewsFeedData mWallData;
	private NewsFeedData mWallDataOlder;

	private NotificationsData mNotificationsData;
	private AlbumsData mAlbumsData;
	private EventsData mEventsData;
	private GroupsData mGroupsData;
	private MessageThreadData mMessagesData;
	private FriendsData mFriendsData;
	private CheckinsData mCheckinsData;
	private BirthdaysData mBirthdaysData;
	private ProfileData mProfileData;
	private LikesData mLikesData;
	private PageData mPageData;
	private boolean hasCameraFeature = true;
	private boolean hasLocationFeature = true;

	private static XMPPConnection mXmppConnection;
	
	public boolean isEnglish(){
		Configuration conf = getResources().getConfiguration();
		if(conf.locale.toString().contains("en")){
			return true;
		}
		
		return false;
	}
	
	public boolean hasKindleFeatureSet(){
		return !hasCameraFeature && !hasLocationFeature;
	}
	
	public boolean hasCameraFeature(){
		return hasCameraFeature;
	}
	
	public boolean hasLocationFeature(){
		return hasLocationFeature;
	}
	
	public static XMPPConnection getXMPPConnection() {

		if (mXmppConnection == null || mXmppConnection.isConnected() == false) {
			try {
				ConnectionConfiguration config = new ConnectionConfiguration("chat.facebook.com", 5222, "chat.facebook.com");
				config.setDebuggerEnabled(true);
				XMPPConnection connection = new XMPPConnection(config);

				connection.connect();

				SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
				SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", SASLXFacebookPlatformMechanism.class);

				FBClientApplication app = getApplication();
				String accessToken = getApplication().getFBConnection().getAccesstokenFromPersistentMemory();
				String apiKey = app.getFBConnection().getAppId();
				Logger.i(FBClientApplication.class.getSimpleName() + "apiKey: " + apiKey);

				connection.login(apiKey, accessToken, "Application");

			} catch (XMPPException e) {
				Logger.i(FBClientApplication.class.getSimpleName() + e.toString());
				mXmppConnection.disconnect();
				return null;
			}
		}
		return mXmppConnection;
	}

	public PageData getPageData() {
		if (mPageData == null) {
			mPageData = new PageData(this);
		}
		return mPageData;
	}

	public LikesData getLikesData() {
		if (mLikesData == null) {
			mLikesData = new LikesData(this);
		}
		return mLikesData;
	}

	public ProfileData getProfileData() {
		if (mProfileData == null) {
			mProfileData = new ProfileData(this);
		}
		return mProfileData;
	}

	public BirthdaysData getBirthdaysData() {
		if (mBirthdaysData == null) {
			mBirthdaysData = new BirthdaysData(this);
		}
		return mBirthdaysData;
	}

	public CheckinsData getCheckinsData() {
		if (mCheckinsData == null) {
			mCheckinsData = new CheckinsData(this);
		}
		return mCheckinsData;
	}

	public FriendsData getFriendsData() {
		if (mFriendsData == null) {
			mFriendsData = new FriendsData(this);
		}
		return mFriendsData;
	}

	public MessageThreadData getMessagesData() {
		if (mMessagesData == null) {
			mMessagesData = new MessageThreadData(this);
		}
		return mMessagesData;
	}

	public GroupsData getGroupsData() {
		if (mGroupsData == null) {
			mGroupsData = new GroupsData(this);
		}
		return mGroupsData;
	}

	public EventsData getEventsData() {
		if (mEventsData == null) {
			mEventsData = new EventsData(this);
		}
		return mEventsData;
	}

	public NotificationsData getNotificationsData() {
		if (mNotificationsData == null) {
			mNotificationsData = new NotificationsData(this);
		}
		return mNotificationsData;
	}

	public AlbumsData getAlbumsData() {
		if (mAlbumsData == null) {
			mAlbumsData = new AlbumsData(this);
		}
		return mAlbumsData;
	}

	public NewsFeedData getNewsFeedData(String type) {
		if (type == NewsFeedData.REQUEST_WALL_OLDER_FROM_DB) {
			if (mWallDataOlder == null) {
				mWallDataOlder = new NewsFeedData(this, NewsFeedData.DB_NAME_WALL_OLDER, NewsFeedData.DB_TABLE_NAME_WALL_OLDER);
			}
			return mWallDataOlder;

		} else if (type == NewsFeedData.REQUEST_NEWSFEED_OLDER_FROM_DB) {
			if (mNewsFeedDataOlder == null) {
				mNewsFeedDataOlder = new NewsFeedData(this, NewsFeedData.DB_NAME_NEWSFEED_OLDER, NewsFeedData.DB_TABLE_NAME_NEWSFEED_OLDER);
			}
			return mNewsFeedDataOlder;
		} else if (type == NewsFeedData.REQUEST_NEWSFEED_FROM_DB) {
			if (mNewsFeedData == null) {
				mNewsFeedData = new NewsFeedData(this, NewsFeedData.DB_NAME_NEWSFEED, NewsFeedData.DB_TABLE_NAME_NEWSFEED);
			}
			return mNewsFeedData;
		} else {
			// type == NewsFeedConstants.REQUEST_WALL_FROM_DB
			// wall is base case since object/feed is accessible for current
			// user while object/home is not.
			if (mWallData == null) {
				mWallData = new NewsFeedData(this, NewsFeedData.DB_NAME_WALL, NewsFeedData.DB_TABLE_NAME_WALL);
			}
			return mWallData;
		}
	}

	@Override
	public void onCreate() {
		ACRA.init(this);
		super.onCreate();

		mApplication = this;
		
		//TODO: test: set locale to PT
		/*
		Configuration configuration = getResources().getConfiguration();
		configuration.locale = new Locale("es");
		getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
		*/
		//TODO: end test
		
		mFBConnection = new FBConnection(this);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		onSharedPreferenceChanged(null, null);

		Logger.i(FBClientApplication.class.getSimpleName() + "#onCreated");
		
		checkSystemFeatures();
		
		Log.i("feb22", "language code: " + getResources().getConfiguration().locale.getLanguage());
		
	}
	
	private void checkSystemFeatures(){
		PackageManager pm = getPackageManager();
		if(pm == null){
			return;
		}
		hasCameraFeature = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
		hasLocationFeature = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
		Logger.i(FBClientApplication.class.getSimpleName() + "#onDestroyed");
	}

	public synchronized FBConnection getFBConnection() {
		return mFBConnection;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Logger.i(FBClientApplication.class.getSimpleName() + "onSharedPreferenceChanged");
		startAlarmManagerForNotifications();
	}

	private void startAlarmManagerForNotifications() {
		NotificationAlarm alarm = new NotificationAlarm(this);
		alarm.start();
	}

	public static FBClientApplication getApplication() {
		return mApplication;
	}

	public void incrementUsageCount() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int usageCount = prefs.getInt(Constants.USAGE_COUNT_KEY, 0);
		usageCount++;
		Editor editor = prefs.edit();
		editor.putInt(Constants.USAGE_COUNT_KEY, usageCount);
		//editor.commit();
		editor.apply();
	}

	public int getUsageCount() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int usageCount = prefs.getInt(Constants.USAGE_COUNT_KEY, 0);
		return usageCount;
	}

}
