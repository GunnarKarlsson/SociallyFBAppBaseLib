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

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;


public class SlidingGridView extends GridView {

	Activity mActivity;
	
	public SlidingGridView(Context context) {
		super(context);
	}
	
	public SlidingGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public SlidingGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public void setActivityContext(Activity activity){
		mActivity = activity;
	}

	public float getYFraction(){
		int height = mActivity.getWindowManager().getDefaultDisplay().getHeight();
		return (height == 0)?0:getY()/(float)height;
	}
	
	public void setYFraction(float yFraction){
		
		int height = mActivity.getWindowManager().getDefaultDisplay().getHeight();
		setY((height > 0)?(yFraction * height) : 0);
		
	}

}
