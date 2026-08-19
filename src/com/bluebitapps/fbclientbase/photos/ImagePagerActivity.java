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

import android.os.Bundle;
import android.view.View;

import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseThemedActivity;

public class ImagePagerActivity extends BaseThemedActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photos_pager_activity);
		setHasNoLoadingImage(true);
		
		//Set theme.
		View view = findViewById(R.id.root);
		setThemeAndConfigureActionBar(view);

		/**
		 * See http://stackoverflow.com/questions/7951730/viewpager-and-fragments-whats-the-right-way-to-store-fragments-state
		 * for explanation of using tags with fragments to avoid recreating fragments on orientation change.
		 */
		
	    ImagePagerFragment fragment;
	    if (savedInstanceState != null) {
	        fragment = (ImagePagerFragment) getFragmentManager().findFragmentByTag("image_pager_fragment_tag");
	    } else {
	        fragment = new ImagePagerFragment();
	        fragment.setArguments(getIntent().getExtras());
	        getFragmentManager().beginTransaction().add(android.R.id.content, fragment, "image_pager_fragment_tag").commit(); 
	    }
	}
	
	

}
