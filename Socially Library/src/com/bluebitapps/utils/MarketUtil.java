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

import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseSlidingMenuActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class MarketUtil {

	public static void openGooglePlay(Context context, String packageName) {

		if (context == null)
			return;

		if (!StringUtil.notEmpty(packageName))
			return;

		String uri = "market://details?id=" + packageName;
		Uri uriToGooglePlayApp = Uri.parse(uri);
		Intent intentToLaunchGooglePlayApp = new Intent(Intent.ACTION_VIEW, uriToGooglePlayApp);
		try {
			context.startActivity(intentToLaunchGooglePlayApp);
		} catch (ActivityNotFoundException e) {
			// Google Play store not found, link to web store
			String url = "http://google.play.com/store/apps/details?id=" + packageName;
			Uri uriToGooglePlayWebStore = Uri.parse(url);
			Intent intentTolaunchGooglePlayWebStore = new Intent(Intent.ACTION_VIEW, uriToGooglePlayWebStore);
			try {
				context.startActivity(intentTolaunchGooglePlayWebStore);
			} catch (ActivityNotFoundException ex) {
				Toast.makeText(context, R.string.remove_ads_menu_item_error_message, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public static void openAmazonStore(Context context, String packageName){
		if (context == null)
			return;

		if (!StringUtil.notEmpty(packageName))
			return;

		String uri = "amzn://apps.android?p=" + packageName;
		Uri uriToGooglePlayApp = Uri.parse(uri);
		Intent intentToLaunchAmazonAppStoreApp = new Intent(Intent.ACTION_VIEW, uriToGooglePlayApp);
		try {
			context.startActivity(intentToLaunchAmazonAppStoreApp);
		} catch (ActivityNotFoundException e) {
			// Amazon AppStore app not found, link to web store
			String url = "http://www.amazon.com/gp/mas/dl/android?p=" + packageName;
			Uri uriToAmazonWebStore = Uri.parse(url);
			Intent intentTolaunchGooglePlayWebStore = new Intent(Intent.ACTION_VIEW, uriToAmazonWebStore);
			try {
				context.startActivity(intentTolaunchGooglePlayWebStore);
			} catch (ActivityNotFoundException ex) {
				Toast.makeText(context, R.string.remove_ads_menu_item_error_message, Toast.LENGTH_SHORT).show();
			}
		}
	}

}
