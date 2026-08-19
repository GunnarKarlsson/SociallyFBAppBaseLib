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

package com.bluebitapps.fbclientbase.theme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.text.GetChars;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.utils.StringUtil;

public class ThemeFactory {

	private static Map<String, Theme> mThemes = Themes.newMap();

	static class Themes {
		private Themes() {
		}

		private static Map<String, Theme> newMap() {
			Map<String, Theme> m = new java.util.HashMap<String, Theme>();
			Resources res = FBClientApplication.getApplication().getResources();

			try {
				XmlPullParser xpp = FBClientApplication.getApplication().getResources().getXml(R.xml.app_themes);

				while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
					if (xpp.getEventType() == XmlPullParser.START_TAG) {
						if (xpp.getName().equals("apptheme")) {

							Theme theme = new Theme();
							theme.setThemeName(xpp.getAttributeValue(null, "name"));
							theme.setDisplayName(xpp.getAttributeValue(null, "displayname"));
							int iconResource = res.getIdentifier(xpp.getAttributeValue(null, "icon"), "drawable", FBClientApplication.getApplication().getPackageName());
							theme.setIcon(iconResource);
							int backgroundResource;
							String resName;

							backgroundResource = res.getIdentifier(xpp.getAttributeValue(null, "background"), "drawable", FBClientApplication.getApplication().getPackageName());
							resName = xpp.getAttributeValue(null, "background");

							theme.setResourceName(resName);
							theme.setBackgroundResource(backgroundResource);
							int actionBarColor = res.getIdentifier(xpp.getAttributeValue(null, "actionbarcolor"), "color", FBClientApplication.getApplication().getPackageName());
							theme.setActionBarColor(actionBarColor);
							Log.i("feb8", FBClientApplication.getApplication().getPackageName());
							Log.i("feb8", Logger.getClassAndMethod() + "actionBarColor: " +actionBarColor);

							int menuColor = res.getIdentifier(xpp.getAttributeValue(null, "menucolor"), "color", FBClientApplication.getApplication().getPackageName());
							theme.setSlidingMenuColor(menuColor);

							m.put(theme.getThemeName(), theme);
						}
					}

					xpp.next();
				}
			} catch (Throwable t) {
				Logger.i(ThemeFactory.class.getSimpleName() + t.toString());

			}

			return Collections.unmodifiableMap(m);
		}
	}

	private static Map<String, Typeface> mTypeFaces = TypeFaces.newMap();

	static class TypeFaces {
		private TypeFaces() {
		}

		private static Map<String, Typeface> newMap() {
			Map<String, Typeface> typefaces = new java.util.HashMap<String, Typeface>();
			Resources res = FBClientApplication.getApplication().getResources();

			Typeface roboto = Typeface.createFromAsset(res.getAssets(), "fonts/ROBOTO.TTF");
			typefaces.put("roboto", roboto);
			Typeface verdana = Typeface.createFromAsset(res.getAssets(), "fonts/VERDANA.TTF");
			typefaces.put("verdana", verdana);
			Typeface arial = Typeface.createFromAsset(res.getAssets(), "fonts/GARAMOND.OTF");
			typefaces.put("garamond", arial);
			Typeface cute = Typeface.createFromAsset(res.getAssets(), "fonts/CUTE.OTF");
			typefaces.put("cute", cute);
			Typeface gill = Typeface.createFromAsset(res.getAssets(), "fonts/GILL.TTF");
			typefaces.put("gill", gill);
			Typeface bmsolid = Typeface.createFromAsset(res.getAssets(), "fonts/BMSOLID.TTF");
			typefaces.put("bmsolid", bmsolid);

			return Collections.unmodifiableMap(typefaces);
		}
	}

	public static int getSlidingMenuColor(Context context, String selection) {

		Log.i("feb3", "selection: " + selection);

		if (mThemes == null) {
			mThemes = Themes.newMap();
		}

		if (!StringUtil.notEmpty(selection)) {
			selection = Constants.THEME_DEFAULT;
		}

		Theme theme = mThemes.get(selection);
		int resource = theme.getSlidingMenuColor();
		return resource;
	}

	public static ColorDrawable getActionBarColorDrawable(String selection, Context context) {

		if (Constants.THEME_IS_COLOR_PICKER_COLOR.equals(selection)) {

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			int color = prefs.getInt(Constants.COLOR_PICKER_CHOICE_ACTIONBAR, 0x000000);

			return new ColorDrawable(color);
		}

		if (mThemes == null) {
			mThemes = Themes.newMap();
		}

		Logger.i(ThemeFactory.class.getSimpleName() + ".getActionBarColorDrawable().selection" + selection);

		if (!StringUtil.notEmpty(selection)) {
			selection = Constants.THEME_DEFAULT;
		}

		Theme theme = mThemes.get(selection);
		Log.i("feb8", Logger.getClassAndMethod() + "theme.getActionBarColor: " + theme.getActionBarColor());
		
		ColorDrawable color = new ColorDrawable(context.getResources().getColor(theme.getActionBarColor()));

		return color;
	}

	public static ViewGroup getViewGroup(String selection, Context oldContext, LayoutInflater inflater, ViewGroup container, int layout) {
		if (mThemes == null) {
			mThemes = Themes.newMap();
		}

		if (!StringUtil.notEmpty(selection)) {
			selection = Constants.THEME_DEFAULT;
		}

		if (Constants.THEME_IS_COLOR_PICKER_COLOR.equals(selection)) {

			FBClientApplication app = FBClientApplication.getApplication();
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);
			int color = sharedPreferences.getInt(Constants.COLOR_PICKER_CHOICE, 0x888888);

			ViewGroup vg = (ViewGroup) inflater.inflate(layout, container, false);
			vg.setBackgroundColor(color);
			return vg;

		} else {

			Theme theme = mThemes.get(selection);

			ViewGroup vg = (ViewGroup) inflater.inflate(layout, container, false);
			vg.setBackgroundResource(theme.getBackgroundResource());
			return vg;
		}
	}

	public static void setActivityTheme(Context context, View view, String selection) {

		if (Constants.THEME_IS_COLOR_PICKER_COLOR.equals(selection)) {
			FBClientApplication app = FBClientApplication.getApplication();
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);
			int color = sharedPreferences.getInt(Constants.COLOR_PICKER_CHOICE, 0x888888);

			view.setBackgroundColor(color);
			return;
		}

		if (mThemes == null) {
			mThemes = Themes.newMap();
		}

		if (!StringUtil.notEmpty(selection)) {
			selection = Constants.THEME_DEFAULT;
		}

		Theme theme = mThemes.get(selection);

		view.setBackgroundResource(theme.getBackgroundResource());
	}

	public static void setThemeAsViewBackground(View view, String selection) {

		if (Constants.THEME_IS_COLOR_PICKER_COLOR.equals(selection)) {
			FBClientApplication app = FBClientApplication.getApplication();
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);
			int color = sharedPreferences.getInt(Constants.COLOR_PICKER_CHOICE, 0x888888);

			view.setBackgroundColor(color);
			return;
		}

		if (mThemes == null) {
			mThemes = Themes.newMap();
		}

		if (!StringUtil.notEmpty(selection)) {
			selection = Constants.THEME_DEFAULT;
		}

		Theme theme = mThemes.get(selection);

		view.setBackgroundResource(theme.getBackgroundResource());
	}

	public static ArrayList<Theme> getThemesList() {
		return new ArrayList<Theme>(mThemes.values());
	}

	public static int getFontSize(FBClientApplication app) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);

		String fontSizeKey;

		try {
			fontSizeKey = app.getResources().getString(com.bluebitapps.fbclientbase.R.string.PREFS_FONT_SIZE_KEY);

		} catch (NotFoundException e) {
			return 15;
		}

		String size = sharedPreferences.getString(fontSizeKey, "15");

		return Integer.parseInt(size);
	}

	public static int getFontColor(FBClientApplication app) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);

		String colorValue;

		try {
			colorValue = app.getResources().getString(com.bluebitapps.fbclientbase.R.string.PREFS_FONT_COLOR_KEY);

		} catch (NotFoundException e) {
			return 0x000000;
		}

		int fontColor;
		String color = sharedPreferences.getString(colorValue, "black");

		if ("grey".equalsIgnoreCase(color)) {
			fontColor = android.R.color.darker_gray;
		} else if ("BlueDark".equalsIgnoreCase(color)) {
			fontColor = android.R.color.holo_blue_dark;
		} else if ("BlueBright".equalsIgnoreCase(color)) {
			fontColor = android.R.color.holo_blue_bright;
		} else if ("BlueLight".equalsIgnoreCase(color)) {
			fontColor = android.R.color.holo_blue_light;
		} else if ("GreenDark".equalsIgnoreCase(color)) {
			fontColor = android.R.color.holo_green_dark;
		} else if ("GreenLight".equalsIgnoreCase(color)) {
			fontColor = android.R.color.holo_green_light;
		} else if ("OrangeDark".equalsIgnoreCase(color)) {
			fontColor = android.R.color.holo_orange_dark;
		} else if ("OrangeLight".equalsIgnoreCase(color)) {
			fontColor = android.R.color.holo_orange_light;
		} else if ("RedDark".equalsIgnoreCase(color)) {
			fontColor = android.R.color.holo_red_dark;
		} else if ("RedLight".equalsIgnoreCase(color)) {
			fontColor = android.R.color.holo_red_light;
		} else if ("purple".equalsIgnoreCase(color)) {
			fontColor = android.R.color.holo_purple;
		} else {
			fontColor = android.R.color.black;
		}

		return fontColor;
	}

	public static Typeface getFontType(FBClientApplication app) {

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);

		String fontType;

		try {
			fontType = app.getResources().getString(com.bluebitapps.fbclientbase.R.string.PREFS_FONT_TYPE_KEY);

		} catch (NotFoundException e) {
			return mTypeFaces.get("roboto");
		}

		if ("droid".equalsIgnoreCase(fontType)) {
			return null;
		}

		String font = sharedPreferences.getString(fontType, "roboto");

		return mTypeFaces.get(font);

	}
}
