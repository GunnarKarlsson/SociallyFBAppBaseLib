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
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.MainActivity;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;

public class ThemeSelectionFragment extends BaseFragment {

	ProgressBar mProgressBar;
	ListView mListView;
	ArrayList<Theme> mThemes;
	ThemeAdapter mAdapter;
	private boolean shouldBeAppliedToApp = true;
	private boolean shouldBeAppliedToDevice = false;
	private WallpaperManager mWallpaperManager;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Logger.i(Logger.getClassAndMethod());
		super.onCreate(savedInstanceState);
		if (getActivity() != null) {
			mWallpaperManager = WallpaperManager.getInstance(getActivity());
		}

		setHasOptionsMenu(true);

		mThemes = ThemeFactory.getThemesList();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.theme_grid, null);
		GridView gridView = (GridView) vg.findViewById(R.id.gridview);
		mAdapter = new ThemeAdapter();
		gridView.setAdapter(mAdapter);

		return vg;
	}

	@Override
	public void onResume() {
		if (getActivity() != null) {
			getActivity().getActionBar().setDisplayShowTitleEnabled(true);
			String str = mThemes.size() + " " + getResources().getString(R.string.free_themes_lowercase);
			getActivity().getActionBar().setTitle(str);
			getActivity().getActionBar().setSubtitle(R.string.tap_a_theme_to_apply_it);
		}
		super.onResume();
	}

	class ThemeAdapter extends BaseAdapter {

		private Bitmap bitmap;

		private class ViewHolder {
			public TextView text;
			public ImageView backgroundColor;
			public ImageView abColor;
			public ImageView menuColor;
			public ViewGroup container;
			public ImageView imageStorage;
		}

		@Override
		public int getCount() {
			if (mThemes != null) {
				return mThemes.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		public Bitmap getCurrentItemBitmap() {
			return bitmap;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			View view = convertView;
			final ViewHolder holder;

			if (getActivity() != null) {

				if (convertView == null) {
					view = getActivity().getLayoutInflater().inflate(R.layout.theme_grid_item, null);

					holder = new ViewHolder();
					holder.container = (ViewGroup) view.findViewById(R.id.container);
					holder.text = (TextView) view.findViewById(R.id.themeName);
					holder.backgroundColor = (ImageView) view.findViewById(R.id.themeBackground);
					holder.abColor = (ImageView) view.findViewById(R.id.themeActionBarColor);
					holder.menuColor = (ImageView) view.findViewById(R.id.themeMenuColor);
					holder.imageStorage = (ImageView) view.findViewById(R.id.imageStorage);
					view.setTag(holder);
				} else
					holder = (ViewHolder) view.getTag();

				holder.abColor.setBackgroundResource(mThemes.get(position).getActionBarColor());
				holder.menuColor.setBackgroundResource(mThemes.get(position).getSlidingMenuColor());
				holder.text.setText(mThemes.get(position).getDisplayName());
				holder.backgroundColor.setBackgroundResource(mThemes.get(position).getIcon());
				holder.imageStorage.setBackgroundResource(mThemes.get(position).getBackgroundResource());
				holder.imageStorage.setDrawingCacheEnabled(true);

				holder.container.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						final int positionToSave = position;

						LayoutInflater inflater = LayoutInflater.from(getActivity());

						if (inflater == null) {
							return;
						}

						final View alertView = inflater.inflate(R.layout.theme_picker_alert, null);

						if (alertView == null) {
							return;
						}

						CheckBox applyToAppCheckBox = (CheckBox) alertView.findViewById(R.id.applyToAppCheckBox);

						applyToAppCheckBox.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View checkBox) {
								if (((CheckBox) checkBox).isChecked()) {
									shouldBeAppliedToApp = true;
								} else {
									shouldBeAppliedToApp = false;
								}

							}
						});

						CheckBox applyToDeviceCheckBox = (CheckBox) alertView.findViewById(R.id.applyToDeviceCheckBox);
						applyToDeviceCheckBox.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View checkBox) {
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

						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(alertView).setMessage("Apply selection?");

						builder.setCancelable(false).setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								if (shouldBeAppliedToApp == false && shouldBeAppliedToDevice == false) {
									return;
								}

								if (shouldBeAppliedToDevice) {
									if (mWallpaperManager != null) {

										int resId = 0;

										String resName = mThemes.get(position).getResourceName();
										String imageFileName = "";
										try {
											imageFileName = resName.substring(0, resName.lastIndexOf('_')) + "_image";
										} catch (Exception e) {
											OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.error_theme_could_not_be_set));
										}

										if (!StringUtil.notEmpty(imageFileName)) {
											OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.error_theme_could_not_be_set));
										}
										String packageName = getActivity().getPackageName();
										resId = getResources().getIdentifier(imageFileName, "drawable", packageName);

										Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(), resId);

										Display display = getActivity().getWindowManager().getDefaultDisplay();
										int width = display.getWidth();
										int height = display.getHeight();

										// Create a new bitmap with the size of
										// the view
										Bitmap bgBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
										Canvas canvas = new Canvas(bgBitmap);

										// Translate down by the remainder
										Matrix matrix = new Matrix();
										matrix.setTranslate(0, height % bitmap.getHeight());
										canvas.setMatrix(matrix);

										// Tile the image
										Paint paint = new Paint();
										paint.setShader(new BitmapShader(bitmap, TileMode.REPEAT, TileMode.REPEAT));
										canvas.drawPaint(paint);

										try {

											mWallpaperManager.setBitmap(bgBitmap);
										} catch (IOException e) {
											OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.wallpaper_could_not_be_set));
										}

									}

									if (shouldBeAppliedToApp == false) {
										if (getActivity() != null) {
											getActivity().finish();
											return;
										}
									}
								}

								// Save selection
								if (shouldBeAppliedToApp) {
									SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
									Editor editor = prefs.edit();
									editor.putString(Constants.THEME_PREFERENCES_KEY, mThemes.get(positionToSave).getThemeName());
									editor.apply();

									getActivity().finish();
									Intent intent = new Intent(getActivity(), MainActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									startActivity(intent);
								}
							}
						}).setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
						AlertDialog alert = builder.create();
						alert.show();

						// end onclick container
					}
				});
			}

			return view;

		}
	}

}