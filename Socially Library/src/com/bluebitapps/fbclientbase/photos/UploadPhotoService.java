/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.photos;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.bluebitapps.utils.ImageUtil;
import com.bluebitapps.utils.StringUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.MainActivity;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.debug.Logger;

//TODO: need to remove albums from db that have been deleted on FB. Need to clear db, then insert new albums.

public class UploadPhotoService extends IntentService {

	public static final String PHOTO_URI_KEY = "photo uri key";
	public static final String CAPTION_KEY = "caption key";
	public static final String PRIVACY_SETTING_KEY = "privacy setting key";
	public static final String ALBUM_ID_KEY = "album name key";
	private Uri mImageUri;
	private Bitmap mBitmap;

	com.facebook.android.UploadDataProgress mUploadListener;
	Notification mNotification;
	Notification.Builder mBuilder;
	NotificationManager mNotificationManager;
	String mCaption = "";
	String mPrivacySetting = "";
	String mAlbumId = "";

	public UploadPhotoService() {
		super("UploadPhotoService");
	}

	/**
	 * Need to 
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.MainActivity;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.utils.ImageUtil;
import com.bluebitapps.utils.StringUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.bluebitapps.fbclientbase.R;

//TODO: need to remove albums from db that have been deleted on FB. Need to clear db, then insert new albums.

public class UploadPhotoService extends IntentService {

	public static final String PHOTO_URI_KEY = "photo uri key";
	public static final String CAPTION_KEY = "caption key";
	public static final String PRIVACY_SETTING_KEY = "privacy setting key";
	public static final String ALBUM_ID_KEY = "album name key";
	private Uri mImageUri;
	private Bitmap mBitmap;

	com.facebook.android.UploadDataProgress mUploadListener;
	Notification mNotification;
	Notification.Builder mBuilder;
	NotificationManager mNotificationManager;
	String mCaption = "";
	String mPrivacySetting = "";
	String mAlbumId = "";

	public UploadPhotoService() {
		super("UploadPhotoService");
	}

	/**
	 * Need to override onStartCommand() or onHandleIntent() will not be called.
	 * Is called for every call from e.g. alarm manager started in
	 * broadcastReceiver.
	 */

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Logger.i(UploadPhotoService.class.getSimpleName() + "#onHandleIntent()");
		Log.i("feb7", Logger.getClassAndMethod());

		if (intent == null) {
			return;
		}

		if (intent.getExtras() == null) {
			return;
		}

		Bundle bundle = intent.getExtras();
		mImageUri = bundle.getParcelable(PHOTO_URI_KEY);
		mCaption = bundle.getString(CAPTION_KEY);
		mPrivacySetting = bundle.getString(PRIVACY_SETTING_KEY);
		mAlbumId = bundle.getString(ALBUM_ID_KEY);

		Log.i("feb7", "mAlbumId: " + mAlbumId);

		boolean isValidSession = ((FBClientApplication) getApplication()).getFBConnection().isValidSession();
		if (isValidSession) {
			uploadPhoto();
		}
	}

	private void uploadPhoto() {

		if (mBitmap != null)
			return;

		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.setAction(Constants.ACTION_PHOTO_UPLOAD_RESULT);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setContentIntent(contentIntent);
		builder.setContentTitle("Picture Upload").setContentText("Upload in progress").setSmallIcon(R.drawable.upload_icon);
		builder.setProgress(100, 100, true);
		Notification notification = builder.getNotification();
		notificationManager.notify(42, notification);

		// find out heap size
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		int memoryClass = am.getMemoryClass();
		Logger.i("memoryClass:" + Integer.toString(memoryClass));// 128

		Bundle params = new Bundle();

		try {
			Logger.i(UploadPhotoService.class.getSimpleName() + "...openInputStream(mImageUri");
			InputStream inputStream = FBClientApplication.getApplication().getContentResolver().openInputStream(mImageUri);
			InputStream inputStreamCopy = FBClientApplication.getApplication().getContentResolver().openInputStream(mImageUri);
			mBitmap = ImageUtil.decodeSampledBitmapFromInputStream(inputStream, inputStreamCopy, 1024, 1024);// BitmapFactory.decodeStream(is);
			inputStream.close();
			inputStreamCopy.close();

			//now we need to rotate it

		} catch (FileNotFoundException e) {
			Logger.i(UploadPhotoService.class.getSimpleName() + e.toString());
		} catch (IOException e) {
			Logger.i(UploadPhotoService.class.getSimpleName() + e.toString());
		} catch (OutOfMemoryError e) {
			Logger.i(UploadPhotoService.class.getSimpleName() + e.toString());
		}

		if(mBitmap == null){
			return;
		}

		Matrix matrix = new Matrix();
		float rotation = ImageUtil.rotationForImage(this, mImageUri);
		if (rotation != 0f) {
			matrix.preRotate(rotation);
		}
		Bitmap rotatedBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
		
		if(rotatedBitmap==null){
			return;
		}
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		mBitmap.recycle();

		params.putByteArray("photo", byteArray);
		params.putString("caption", mCaption);

		if (StringUtil.notEmpty(mPrivacySetting)) {
			JSONObject privacy = new JSONObject();
			try {
				privacy.put("value", mPrivacySetting);
			} catch (JSONException e) {
				Log.i("feb7", Logger.getClassAndMethod() + "privacy value exception: " + e);
			}

			params.putString("privacy", privacy.toString());
		}

		if (StringUtil.notEmpty(mAlbumId)) {
			String path = mAlbumId + "/photos";
			FBClientApplication.getApplication().getFBConnection().getAsyncFacebookRunner().request(path, params, "POST", new PhotoUploadListener(), null);
		} else {

			FBClientApplication.getApplication().getFBConnection().getAsyncFacebookRunner().request("me/photos", params, "POST", new PhotoUploadListener(), null);
		}
	}

	/*
	 * callback for the photo upload
	 */
	public class PhotoUploadListener implements RequestListener {

		@Override
		public void onComplete(final String response, final Object state) {
			Logger.i(UploadPhotoService.class.getSimpleName() + PhotoUploadListener.class.getSimpleName() + response.toString());
			Log.i("feb7", UploadPhotoService.class.getSimpleName() + PhotoUploadListener.class.getSimpleName() + response.toString());
			sendNotification();
		}

		public void onFacebookError(FacebookError e) {
			Logger.i(UploadPhotoService.class.getSimpleName() + PhotoUploadListener.class.getSimpleName() + e.toString());
			sendErrorNotification();
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(UploadPhotoService.class.getSimpleName() + PhotoUploadListener.class.getSimpleName() + e.toString());
			sendErrorNotification();
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			Logger.i(UploadPhotoService.class.getSimpleName() + PhotoUploadListener.class.getSimpleName() + e.toString());
			sendErrorNotification();
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			Logger.i(UploadPhotoService.class.getSimpleName() + PhotoUploadListener.class.getSimpleName() + e.toString());
			sendErrorNotification();
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(UploadPhotoService.class.getSimpleName() + PhotoUploadListener.class.getSimpleName() + e.toString());
			sendErrorNotification();
		}
	}

	private void sendNotification() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notificationIntent.setAction(Constants.ACTION_PHOTO_UPLOAD_RESULT);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setContentIntent(contentIntent);
		builder.setContentTitle("Picture has been uploaded to Facebook").setContentText("Picture successfully uploaded from Socially").setSmallIcon(R.drawable.upload_ok);

		Notification notification = builder.getNotification();
		notificationManager.notify(42, notification);
	}

	private void sendErrorNotification() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notificationIntent.setAction(Constants.ACTION_PHOTO_UPLOAD_RESULT);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setContentIntent(contentIntent);
		builder.setContentTitle("Picture could not be uploaded to Facebook").setContentText("Upload error. Please try again").setSmallIcon(R.drawable.upload_fail);
		Notification notification = builder.getNotification();
		notificationManager.notify(42, notification);
	}
}