/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.photos;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

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

import com.bluebitapps.utils.ImageUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.MainActivity;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.debug.Logger;

//TODO: need to remove albums from db that have been deleted on FB. Need to clear db, then insert new albums.

public class SetProfilePicService extends IntentService {

	public static final String PHOTO_URI_KEY = "photo uri key";
	private Uri mImageUri;
	private Bitmap mBitmap;

	com.facebook.android.UploadDataProgress mUploadListener;
	Notification mNotification;
	Notification.Builder mBuilder;
	NotificationManager mNotificationManager;

	public SetProfilePicService() {
		super("SetProfilePicService");
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
		Logger.i("UploadPhotoService");
		
		if(intent == null){
			return;
		}
		
		if(intent.getExtras()==null){
			return;
		}
		
		Bundle bundle = intent.getExtras();
		mImageUri = bundle.getParcelable(PHOTO_URI_KEY);
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
			Logger.i("UploadPhotoService openInputStream(mImageUri");
			InputStream is = FBClientApplication.getApplication().getContentResolver().openInputStream(mImageUri);
			mBitmap = BitmapFactory.decodeStream(is);
			is.close();
			Logger.i("UploadPhotoService close Inputstream");

		} catch (FileNotFoundException e) {
			Logger.i(e.toString());
		} catch (IOException e) {
			Logger.i(e.toString());
		} catch (OutOfMemoryError e) {

		}

		Matrix matrix = new Matrix();
		float rotation = ImageUtil.rotationForImage(this, mImageUri);
		if (rotation != 0f) {
			matrix.preRotate(rotation);
		}

		int height, width;
		if (rotation != 0 || rotation != 180) {
			height = mBitmap.getWidth();
			width = mBitmap.getHeight();
		} else {
			height = mBitmap.getHeight();
			width = mBitmap.getWidth();
		}
		Bitmap rotatedBitmap = Bitmap.createBitmap(mBitmap, 0, 0, height, width, matrix, true);

		Logger.i(SetProfilePicService.class.getSimpleName() + "UploadPhotoService new ByteArrayOutputstream");
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Logger.i(SetProfilePicService.class.getSimpleName() + "UploadPhotoService compress Bitmap");
		rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		Logger.i(SetProfilePicService.class.getSimpleName() + "UploadPhotoService stream.toByteArray");
		byte[] byteArray = stream.toByteArray();
		Logger.i(SetProfilePicService.class.getSimpleName() + "mBitmap.recycle()");
		// free up heap space
		mBitmap.recycle();

		Logger.i(SetProfilePicService.class.getSimpleName() + "UploadPhotoService put ByteArray");
		params.putByteArray("photo", byteArray);
		params.putString("caption", "Test");
		// This params sets image to profile.
		params.putString("makeprofile", "1");

		Logger.i(SetProfilePicService.class.getSimpleName() + "UploadPhotoService request FB connection");
		FBClientApplication.getApplication().getFBConnection().getAsyncFacebookRunner().request("me/photos", params, "POST", new PhotoUploadListener(), null);
	}

	/*
	 * callback for the photo upload
	 */
	public class PhotoUploadListener implements RequestListener {

		@Override
		public void onComplete(final String response, final Object state) {
			Logger.i(SetProfilePicService.class.getSimpleName() + PhotoUploadListener.class.getSimpleName() + "#onOptionsItemSelected$Post");
			sendNotification();
		}

		public void onFacebookError(FacebookError error) {
			sendErrorNotification();
		}

		@Override
		public void onIOException(IOException e, Object state) {
			sendErrorNotification();
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, Object state) {
			sendErrorNotification();
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, Object state) {
			sendErrorNotification();
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
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
		builder.setContentTitle("Facebook profile picture has been changed").setContentText("Facebook profile picture has been changed").setSmallIcon(R.drawable.upload_ok);

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