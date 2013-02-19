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

import java.io.IOException;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.MainActivity;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.utils.OutputUtil;
import com.larswerkman.colorpicker.ColorPicker;
import com.larswerkman.colorpicker.SVBar;

public class ColorPickerFragment extends BaseFragment {

	private boolean shouldBeAppliedToDevice = false;
	private boolean shouldBeAppliedToApp = true;
	private WallpaperManager mWallpaperManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (getActivity() != null) {
			mWallpaperManager = WallpaperManager.getInstance(getActivity());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.color_picker_fragment, null);
		// vg.setBackgroundColor(0x000000);

		final ColorPicker picker = (ColorPicker) vg.findViewById(R.id.picker);
		SVBar svBar = (SVBar) vg.findViewById(R.id.svbar);
		picker.addSVBar(svBar);
		if (getActivity() != null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			int oldColor = prefs.getInt(Constants.COLOR_PICKER_CHOICE, 0x00ff00);
			picker.setColor(oldColor);
		}

		Button btn = (Button) vg.findViewById(R.id.selectionBtn);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (getActivity() == null) {
					return;
				}
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				final View view = inflater.inflate(R.layout.theme_picker_alert, null);

				CheckBox applyToAppCheckBox = (CheckBox) view.findViewById(R.id.applyToAppCheckBox);
				applyToAppCheckBox.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View checkBox) {
						// TODO Auto-generated method stub
						if (((CheckBox) checkBox).isChecked()) {
							shouldBeAppliedToApp = true;
						} else {
							shouldBeAppliedToApp = false;
						}

					}
				});

				CheckBox applyToDeviceCheckBox = (CheckBox) view.findViewById(R.id.applyToDeviceCheckBox);
				applyToDeviceCheckBox.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View checkBox) {
						// TODO Auto-generated method stub
						if (((CheckBox) checkBox).isChecked()) {
							shouldBeAppliedToDevice = true;
						} else {
							shouldBeAppliedToDevice = false;
						}

					}
				});

				if (getActivity() != null) {

					if (getActivity().isFinishing()) {
						return;
					}
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(view).setMessage(getResources().getString(R.string.apply_selection));

				builder.setCancelable(false).setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						/*
						 * shade(menu) rx/2 gx/2 bx/2 tone(ab) (rx+127)/2 (gx +
						 * 127)/2 (bx + 127)/2 Source
						 * :http://www.charles-reace.com/test/colors
						 * .php?color=aaffaa see also: http://stackoverflow
						 * .com/questions/6615002/given
						 * -an-rgb-value-how-do-i-create-a-tint-or-shade
						 */
						if (shouldBeAppliedToApp == false && shouldBeAppliedToDevice == false) {
							return;
						}

						if (shouldBeAppliedToDevice) {
							if (mWallpaperManager != null) {

								Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
								bitmap.eraseColor(picker.getColor());
								try {
									mWallpaperManager.setBitmap(bitmap);
								} catch (IOException e) {
									OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.device_wallpaper_could_not_be_set));
								}

								if (shouldBeAppliedToApp == false) {
									if (getActivity() != null) {
										getActivity().finish();
										return;
									}
								}
							}

						}

						if (shouldBeAppliedToApp) {

							int color = picker.getColor();

							int r = (color >> 16) & 0xFF;
							int g = (color >> 8) & 0xFF;
							int b = (color >> 0) & 0xFF;

							// shade
							int shadeRed = r / 2;
							int shadeGreen = g / 2;
							int shadeBlue = b / 2;

							int shade = ((255 & 0xFF) << 24) | ((shadeRed & 0xFF) << 16) | ((shadeGreen & 0xFF) << 8) | ((shadeBlue & 0xFF) << 0);

							// tone

							int toneRed = (r + 127) / 2;
							int toneGreen = (g + 127) / 2;
							int toneBlue = (b + 127) / 2;

							int tone = ((255 & 0xFF) << 24) | ((toneRed & 0xFF) << 16) | ((toneGreen & 0xFF) << 8) | ((toneBlue & 0xFF) << 0);

							Log.i("feb3", Logger.getClassAndMethod() + color + " #" + Integer.toHexString(color));
							FBClientApplication app = FBClientApplication.getApplication();
							SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);
							Editor editor = prefs.edit();
							editor.putString(Constants.THEME_PREFERENCES_KEY, Constants.THEME_IS_COLOR_PICKER_COLOR);
							editor.putInt(Constants.COLOR_PICKER_CHOICE, color);
							editor.putInt(Constants.COLOR_PICKER_CHOICE_SLIDING_MENU, shade);
							editor.putInt(Constants.COLOR_PICKER_CHOICE_ACTIONBAR, tone);
							editor.apply();

							// Restart
							if (getActivity() != null) {
								getActivity().finish();
							}
						}

					}
				}).setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				AlertDialog alert = builder.create();
				alert.show();

			}
		});

		return vg;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (getActivity() != null) {
			ActionBar actionBar = getActivity().getActionBar();
			if (actionBar != null) {
				actionBar.setDisplayShowTitleEnabled(true);
				actionBar.setTitle(R.string.color_picker_menu_item);
			}
		}
	}
}
