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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.bluebitapps.fbclientbase.Constants;

public class UsageCounter {
	
	public static void setUsageCounter(Context context){
	
		//get old value
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		
		int usageCounter = prefs.getInt(Constants.USAGE_COUNTER_KEY, Constants.USAGE_COUNTER_EMPTY_VALUE);
		
		//set new value
		usageCounter += 1;
		
		Editor editor = prefs.edit();
		editor.putInt(Constants.USAGE_COUNTER_KEY, usageCounter);
		//editor.commit();
		editor.apply();
	}
	
	public static int getUsageCounter(Context context){
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		
		int usageCounter = prefs.getInt(Constants.USAGE_COUNTER_KEY, Constants.USAGE_COUNTER_EMPTY_VALUE);
		
		if(usageCounter < 1){
			usageCounter = 0;
		}
		
		return usageCounter;
	}
	
}
