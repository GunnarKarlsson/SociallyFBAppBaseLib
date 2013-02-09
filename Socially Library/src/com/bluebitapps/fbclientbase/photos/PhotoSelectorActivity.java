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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.bluebitapps.fbclientbase.debug.Logger;

public class PhotoSelectorActivity extends Activity {

	private static final int PICK_EXISTING_PHOTO_RESULT_CODE = 10;
	private static final int TAKE_PICTURE_WITH_CAMERA_RESULT_CODE = 20;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Logger.i(Logger.getClassAndMethod());
		showEditDialog();
	}

	private void showEditDialog() {
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(PhotoSelectorActivity.this);
		dialog.setTitle("Select:");
		final String[] selectionItems = {"Camera", "Gallery"};
		dialog.setItems(selectionItems, new android.content.DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == 0){
					Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(cameraIntent, TAKE_PICTURE_WITH_CAMERA_RESULT_CODE);
				}else if(which == 1){
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent, "Pick Gallery"), PICK_EXISTING_PHOTO_RESULT_CODE);
				}
				dialog.dismiss();
			}
		});
		dialog.show();
	}
}