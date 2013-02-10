/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.util.Log;

import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.base.BaseSlidingMenuActivity;
import com.bluebitapps.fbclientbase.chat.SASLXFacebookPlatformMechanism;
import com.bluebitapps.fbclientbase.chat.XMPPConnectionSingleton;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.fbconnection.FBConnection;
import com.bluebitapps.fbclientbase.photos.UploadPhotoActivity;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

public class MainActivity extends BaseSlidingMenuActivity {
	
	FBClientApplication mApplication;
	Context mContext;

	/**
	 * {@link MainActivity#onCreate(Bundle)} get references for
	 * {@link FBClientApplication}, {@link FBConnection#getPermissions()}, and
	 * calls
	 * {@link Facebook#authorize(android.app.Activity, String[], DialogListener)}
	 * 
	 * {@link FBConnection} will set accessToken and expire time on Facebook
	 * object and set session validity. If it is not valid,
	 * {@link MainActivity#onCreate(Bundle) will request authorization.
	 */

	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Logger.i(Logger.getClassAndMethod());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		mApplication = (FBClientApplication) getApplication();

		mContext = this;

		/*
		 * if there is no valid FB session, do authorization. If there is a
		 * valid session, request user's profile data.
		 */

		mApplication.incrementUsageCount();

		if (mApplication.getFBConnection().shouldForceAuth() || !mApplication.getFBConnection().isValidSession()) {
			Log.i("jan21", Logger.getClassAndMethod() + "should force auth or not valid session");
			mApplication.getFBConnection().setForceAuthFlag(false);
			doAuthorization();
		}

		else {
			showFirstFragment();
			mApplication.getFBConnection().getAsyncFacebookRunner().request("me", new UserProfileRequestListener());
		}

		if (getIntent() != null) {
			notificationReceived(getIntent());
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	public void onResume() {
		super.onResume();

		mApplication.getFBConnection().getFacebook().extendAccessTokenIfNeeded(this, null);
	}

	private void doAuthorization() {
		Log.i("jan21", Logger.getClassAndMethod());
		Logger.i(Logger.getClassAndMethod());
		String[] permissions = mApplication.getFBConnection().getPermissions();
		mApplication.getFBConnection().getFacebook().authorize(this, permissions, Facebook.FORCE_DIALOG_AUTH, new AuthListener());// forces
																																	// dialog
		// .authorize(this, permissions, new AuthListener());
	}

	/**
	 * Single sign-on is triggered via activity.startActivityForResult(). We
	 * need to implement onActivityResult() in our calling activity and include
	 * a call to the authorizeCallback method() for SSO to work properly.
	 */

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Logger.i(Logger.getClassAndMethod());
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == BaseNavigationFragment.PICK_EXISTING_PHOTO_RESULT_CODE) {
			if (resultCode == Activity.RESULT_OK) {

				Uri uri = data.getData();
				startUploadActivity(uri);
			}
		} else if (requestCode == BaseNavigationFragment.TAKE_PICTURE_WITH_CAMERA_RESULT_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				startUploadActivity(getFileUri());
			}

		} else {

			if (mApplication != null) {
				if (mApplication.getFBConnection() != null) {
					if (mApplication.getFBConnection().getFacebook() != null) {
						mApplication.getFBConnection().getFacebook().authorizeCallback(requestCode, resultCode, data);
					}
				}
			}
		}
	}
	
	private void startUploadActivity(Uri uri) {
		Log.i("jan23", Logger.getClassAndMethod());
			Intent intent = new Intent(MainActivity.this, UploadPhotoActivity.class);
			intent.putExtra(UploadPhotoActivity.IMAGE_URI_KEY, uri);
			startActivity(intent);
	}

	/**
	 * LoginListener listens for results from
	 * {@link Facebook#authorize(android.app.Activity, String[], DialogListener)}
	 * 
	 */

	private class AuthListener implements DialogListener {

		@Override
		public void onComplete(Bundle values) {
			Logger.i(MainActivity.class.getSimpleName() + "." + Logger.getClassAndMethod() + "#onComplete");

			mApplication.getFBConnection().saveAccessTokenInPersistentMemory();
			mApplication.getFBConnection().saveExpiresInPersistentMemory();

			mApplication.getFBConnection().getAsyncFacebookRunner().request("me", new UserProfileRequestListener());
			// showFirstFragment();
		}

		@Override
		public void onFacebookError(FacebookError e) {
			Log.i("jan21", Logger.getClassAndMethod() + e.toString());
			// TODO Try again alertdialog.
		}

		@Override
		public void onError(DialogError e) {
			Logger.i(MainActivity.class.getSimpleName() + "." + Logger.getClassAndMethod() + e.toString());
			Log.i("jan21", Logger.getClassAndMethod() + e.toString());
			// TODO: Try again alertdialog.

		}

		@Override
		public void onCancel() {
			Log.i("jan21", Logger.getClassAndMethod());
		}

	}

	/**
	 * UserProfileRequestListener listen for results from the call to
	 * AsyncFacebookRunner() .request("me", new UserProfileRequestListener()) in
	 * {@link LoginDialogListener#onComplete(Bundle)}. It retrieved user profile
	 * data.
	 * 
	 * @author Gunnar Karlsson
	 * 
	 */

	private class UserProfileRequestListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {
			try {

				JSONObject json = Util.parseJson(response);
				String userId = json.getString("id");
				String name = json.getString("name");

				mApplication.getFBConnection().saveUserIdInPersistentMemory(userId);

				mApplication.getFBConnection().saveUserNameInPersistentMemory(name);

				showFirstFragment();

				connectToChat();

			} catch (JSONException e) {
				Logger.i(MainActivity.class.getSimpleName() + "." + UserProfileRequestListener.class.getSimpleName() + e.toString());
			} catch (FacebookError e) {
				Logger.i(MainActivity.class.getSimpleName() + "." + UserProfileRequestListener.class.getSimpleName() + e.toString());
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(MainActivity.class.getSimpleName() + "." + UserProfileRequestListener.class.getSimpleName() + e.toString());
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(MainActivity.class.getSimpleName() + "." + UserProfileRequestListener.class.getSimpleName() + e.toString());
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(MainActivity.class.getSimpleName() + "." + UserProfileRequestListener.class.getSimpleName() + e.toString());
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(MainActivity.class.getSimpleName() + "." + UserProfileRequestListener.class.getSimpleName() + e.toString());
		}

	}

	private void connectToChat() {

		FBClientApplication app = (FBClientApplication) this.getApplication();
		final String accessToken = app.getFBConnection().getAccesstokenFromPersistentMemory();
		final String apiKey = app.getFBConnection().getAppId();

		Thread thread = new Thread() {

			@Override
			public void run() {

				ConnectionConfiguration config = new ConnectionConfiguration("chat.facebook.com", 5222, "chat.facebook.com");
				
				config.setDebuggerEnabled(true);
				XMPPConnection connection = new XMPPConnection(config);
				XMPPConnectionSingleton connectionSingleton = XMPPConnectionSingleton.getInstance();
				connectionSingleton.setConnection(connection);

				try {
					connection.connect();
					SASLAuthentication.supportSASLMechanism("X-FACEBOOK-PLATFORM", 0);
					SASLAuthentication.registerSASLMechanism("X-FACEBOOK-PLATFORM", SASLXFacebookPlatformMechanism.class);
					connection.login(apiKey, accessToken, "Application");

					if (connection.isConnected()) {
						Logger.i(MainActivity.class.getSimpleName() + "." + "connectToChat()." + "isConnected==true");
					} else {
						Logger.i(MainActivity.class.getSimpleName() + "." + "connectToChat()." + "isConnected==false");
					}

				}catch(IllegalStateException e){
					Logger.i(Logger.getClassAndMethod() + e);
					try{
						connection.disconnect();
					}catch(Exception ex){
						Logger.i(Logger.getClassAndMethod() + ex);
					}
				} catch (Exception e) {
					Logger.i(MainActivity.class.getSimpleName() + "." + "connectToChat()." + e.toString());
					try{						
						connection.disconnect();
					}catch(Exception ex){
						Logger.i(Logger.getClassAndMethod() + ex);
					}
				}
			}
		};

		thread.start();
	}
}