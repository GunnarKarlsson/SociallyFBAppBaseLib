/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.fbconnection;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.debug.Logger;

public class FBConnection {
	private static final String ACCESS_TOKEN_EMPTY_VALUE = "";
	private static final long EXPIRES_EMPTY_VALUE = 0;
	private static final String EMPTY_STRING_VALUE = "";
	private static final String ACCESS_TOKEN_KEY = "access_token_key";
	private static final String EXPIRES_KEY = "expires_key";
	private static final String USER_ID_KEY = "user_id_key";
	private static final String USER_NAME_KEY = "user_name_key";
	private static String APP_ID = "";

	private String[] mPermissions;

	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;
	private SharedPreferences mSharedPreferences;
	private FBClientApplication mApplication;
	private boolean isPinkVersion = false;

	public FBConnection(FBClientApplication context) {
		Logger.i(FBConnection.class.getSimpleName() + "#onCreated");
		Resources res = context.getResources();
		mPermissions = res.getStringArray(R.array.permissions);

		mApplication = context;
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		isPinkVersion = context.getResources().getBoolean(R.bool.isPinkVersion);
		
		APP_ID = context.getResources().getString(R.string.appid);
		
		mFacebook = new Facebook(APP_ID);
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);

	}

	public synchronized boolean isValidSession() {
		String accessToken = getAccesstokenFromPersistentMemory();
		long expires = getExpiresFromPersistentMemory();

		Logger.i(FBConnection.class.getSimpleName() + "#isSessionValid() " + "accessToken: " + getAccesstokenFromPersistentMemory());
		Logger.i(FBConnection.class.getSimpleName() + "#isSessionValid() " + "expires: " + expires);

		if (accessToken != ACCESS_TOKEN_EMPTY_VALUE) {
			setAccessTokenOnFacebookObject(accessToken);
		}

		if (expires != EXPIRES_EMPTY_VALUE) {
			setExpiresOnFacebookObject(expires);
		}

		Logger.i(FBConnection.class.getSimpleName() + "#isSessionValid() " + (accessToken != ACCESS_TOKEN_EMPTY_VALUE && expires != EXPIRES_EMPTY_VALUE));

		return (accessToken != null && expires != 0);
	}

	private synchronized void setExpiresOnFacebookObject(long expires) {
		mFacebook.setAccessExpires(expires);
	}

	private synchronized void setAccessTokenOnFacebookObject(String accessToken) {
		mFacebook.setAccessToken(accessToken);
	}

	public synchronized void saveExpiresInPersistentMemory() {
		Long expires = mFacebook.getAccessExpires();
		Logger.i(FBConnection.class.getSimpleName() + "#saveExpiresTokenInPersistentMemory() " + "expires: " + expires);
		Editor editor = mSharedPreferences.edit();
		editor.putLong(FBConnection.EXPIRES_KEY, expires);
		// editor.commit();
		editor.apply();
	}

	public synchronized void deleteExpiresInPersistentMemory() {
		Editor editor = mSharedPreferences.edit();
		editor.putLong(FBConnection.EXPIRES_KEY, EXPIRES_EMPTY_VALUE);
		editor.apply();
	}

	public synchronized void saveAccessTokenInPersistentMemory() {
		String accessToken = mFacebook.getAccessToken();
		Log.i("jan21", FBConnection.class.getSimpleName() + "#saveAccessTokenInPersistentMemory() " + "accessToken: " + accessToken);
		Editor editor = mSharedPreferences.edit();
		editor.putString(FBConnection.ACCESS_TOKEN_KEY, accessToken);
		// editor.commit();
		editor.apply();
	}

	public synchronized void deleteAccessTokenInPersistentMemory() {
		Editor editor = mSharedPreferences.edit();
		editor.putString(FBConnection.ACCESS_TOKEN_KEY, ACCESS_TOKEN_EMPTY_VALUE);
		editor.apply();
	}

	
	public synchronized void saveUserNameInPersistentMemory(String name) {
		Editor editor = mSharedPreferences.edit();
		editor.putString(FBConnection.USER_NAME_KEY, name);
		// editor.commit();
		editor.apply();
	}

	/**
	 * 
	 * @return Facebook user's name (not Facebook user's username)
	 */

	public synchronized String getUserName() {
		return mSharedPreferences.getString(USER_NAME_KEY, EMPTY_STRING_VALUE);
	}

	public synchronized void saveUserIdInPersistentMemory(String userId) {
		Editor editor = mSharedPreferences.edit();
		editor.putString(FBConnection.USER_ID_KEY, userId);
		// editor.commit();
		editor.apply();
	}

	public synchronized void setForceAuthFlag(boolean value) {
		Editor editor = mSharedPreferences.edit();
		editor.putBoolean(Constants.FORCE_AUTH_FLAG, value);
		editor.apply();
	}

	public synchronized boolean shouldForceAuth() {
		return mSharedPreferences.getBoolean(Constants.FORCE_AUTH_FLAG, false);
	}

	public synchronized String getUserId() {
		return mSharedPreferences.getString(USER_ID_KEY, EMPTY_STRING_VALUE);
	}

	public synchronized long getExpiresFromPersistentMemory() {
		return mSharedPreferences.getLong(FBConnection.EXPIRES_KEY, EXPIRES_EMPTY_VALUE);
	}

	public synchronized String getAccesstokenFromPersistentMemory() {
		return mSharedPreferences.getString(FBConnection.ACCESS_TOKEN_KEY, ACCESS_TOKEN_EMPTY_VALUE);
	}

	public synchronized String getAppId() {
			return APP_ID;
	}

	public synchronized String[] getPermissions() {
		return mPermissions;
	}

	public synchronized Facebook getFacebook() {
		return mFacebook;
	}

	public synchronized AsyncFacebookRunner getAsyncFacebookRunner() {
		return mAsyncRunner;
	}

}