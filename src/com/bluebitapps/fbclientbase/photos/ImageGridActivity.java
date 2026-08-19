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

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.albums.Album;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;

public class ImageGridActivity extends BaseThemedActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.standard_activity);
		ViewGroup rootView = (ViewGroup)findViewById(R.id.rootView);
		setThemeAndConfigureActionBar(rootView);

		Bundle extras = getIntent().getExtras();
		String objectId = extras.getString(Album.ALBUM_ID_KEY);
		String objectTitle = extras.getString(Album.ALBUM_NAME_KEY);
		Boolean clearTop = extras.getBoolean(BaseThemedActivity.CLEAR_TOP_ON_HOME_SELECTED);
		
		Bundle bundle = new Bundle();
		bundle.putString(Album.ALBUM_ID_KEY, objectId);
		bundle.putString(Album.ALBUM_NAME_KEY, objectTitle);
		bundle.putBoolean(BaseThemedActivity.CLEAR_TOP_ON_HOME_SELECTED, clearTop);
		ImageGridFragment fragment = new ImageGridFragment();
		fragment.setArguments(bundle);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.replace(android.R.id.content, fragment).commit();
	}

}
