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

package com.bluebitapps.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.util.Random;

import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;

import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

public class OutputUtil {

	public static final void saveImageFromUrlToSDCard(Context context, String appName, String imgName, String urlString) {
		try {

			doDownLoad(context, appName, imgName, urlString);

		} catch (IllegalStateException e) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
		} catch (IOError e) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
		}
	}

	/**
	 * Saved Image to SDCard. Shows Crouton if Exception is thrown.
	 * 
	 * @param activity
	 *            activity context for Crouton.
	 * @param appName
	 *            used for folder name.
	 * @param imgName
	 *            if null, will be set to "img_" + currentTimeMillis().
	 * @param urlString
	 *            patht o resource to download. Must be an image file.
	 */
	public static final void saveImageFromUrlToSDCard(Activity activity, String appName, String imgName, String urlString) {
		try {

			doDownLoad(activity, appName, imgName, urlString);

		} catch (IllegalStateException e) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			OutputUtil.showCrouton(activity, "Photo could not be saved");
		} catch (IOError e) {
			Logger.i(Logger.getClassAndMethod() + e.toString());
			OutputUtil.showCrouton(activity, "Photo could not be saved");
		}
	}

	private static final void createFolder(String path) {
		File folder = new File(path);
		if (!folder.exists()) {
			folder.mkdirs();
		}

	}

	private static final void doDownLoad(Context context, String appName, String imgName, String urlString) {
		
		if(context == null){
			return;
		}
		
		if(urlString == null){
			return;
		}
		
		String folderPathInGallery = "/DCIM/" + appName;

		String imageName;

		if (StringUtil.notEmpty(imgName)) {
			imageName = imgName;
		} else {
			imageName = "img_" + System.currentTimeMillis();
		}

		createFolder(Environment.getExternalStorageDirectory().toString() + folderPathInGallery);

		DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		Uri uri = Uri.parse(urlString);
		DownloadManager.Request request = new Request(uri);
		request.setTitle("Socially: photo download");
		request.setDescription(uri.toString());
		request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		request.setDestinationInExternalPublicDir(folderPathInGallery, imageName);
		downloadManager.enqueue(request);
	}

	/**
	 * Runs Crouton with message "No search results found" on new Thread.
	 * 
	 * @param activity
	 *            Activity reference for Crouton. Is null-safe.
	 * @param loadingView
	 *            . LoadingView that should be removed when Crouton appears. Is
	 *            null-safe.
	 */
	public static final void showNoResultsMessage(final Activity activity, final LoadingView loadingView) {

		if (activity != null) {

			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Crouton.makeText(activity, "No results found", Style.INFO).show();

					if (loadingView != null) {
						loadingView.setVisibility(View.GONE);
					}
				}
			});

		}
	}

	public static final void cancelAllCroutons(final Activity activity) {
		if (activity != null) {

			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Crouton.cancelAllCroutons();

				}
			});
		}
	}

	/**
	 * Runs Crouton with message "Data could not be retrieved" on new Thread.
	 * 
	 * @param activity
	 *            Activity reference for Crouton. Is null-safe.
	 * @param loadingView
	 *            . LoadingView that should be removed when Crouton appears. Is
	 *            null-safe.
	 */

	public static final void showErrorGettingResultsMessage(final Activity activity, final LoadingView loadingView) {

		if (activity != null) {

			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Crouton.makeText(activity, "Data could not be retrieved", Style.INFO).show();

					if (loadingView != null) {
						loadingView.setVisibility(View.GONE);
					}
				}
			});
		}
	}

	public static final void showCrouton(final Activity activity, final String message) {

		if (activity != null) {

			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Crouton.makeText(activity, message, Style.INFO).show();

				}
			});

		}
	}

}
