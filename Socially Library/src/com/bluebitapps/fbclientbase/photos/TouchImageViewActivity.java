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

package com.bluebitapps.fbclientbase.photos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.TouchImageView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class TouchImageViewActivity extends BaseThemedActivity {

	private String mUrl;
	private TouchImageView mTouchView;
	private LoadingView mLoadingView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.touchimageview_activity);
		setHasNoLoadingImage(true);

		ViewGroup rootView = (ViewGroup) findViewById(R.id.rootView);

		setThemeAndConfigureActionBar(rootView);

		Bundle bundle = getIntent().getExtras();
		mUrl = bundle.getString(Constants.OBJECT_URL_KEY);

		mLoadingView = (LoadingView)findViewById(R.id.loadingView);
		mTouchView = (TouchImageView) findViewById(R.id.image);
		mTouchView.setMaxZoom(4f);

		getImageLoader().displayImage(mUrl, mTouchView);
		
		getImageLoader().displayImage(mUrl, mTouchView, getImageDisplayOptions(), new ImageLoadingListener() {
			@Override
			public void onLoadingStarted() {
				mLoadingView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingFailed(FailReason failReason) {

				OutputUtil.showCrouton(TouchImageViewActivity.this, "Error loading image");
				mLoadingView.setVisibility(View.GONE);
				mTouchView.setImageResource(android.R.drawable.ic_delete);
			}

			@Override
			public void onLoadingComplete(Bitmap loadedImage) {
				if (mLoadingView != null) {
					mLoadingView.setVisibility(View.GONE);
				}
					Animation anim = AnimationUtils.loadAnimation(TouchImageViewActivity.this, android.R.anim.fade_in);
					if (anim != null) {
						mTouchView.setAnimation(anim);
					}
					anim.start();
			}

			@Override
			public void onLoadingCancelled() {
				// Do nothing
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.imagepagermenu, menu);
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (getActionBar() != null) {
			getActionBar().setDisplayShowTitleEnabled(true);
			getActionBar().setTitle("Socially Image Viewer");
			getActionBar().setSubtitle("Pinch to zoom");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		if (item.getItemId() == R.id.saveMenuItem) {

			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_menu_save).setTitle("Save Image to SD Card").setPositiveButton("Save", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					OutputUtil.saveImageFromUrlToSDCard(TouchImageViewActivity.this, "Socially", null, mUrl);
				}

			}).setNegativeButton("Cancel", null).show();
			return true;
		} else {
			return false;
		}

	}
}
