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

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;

public class ExitUtil {
	
	public static boolean isLastActivity(Activity context){
		
		if(context==null){
			return false;
		}
		
		final ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		final List<RunningTaskInfo> tasksInfo = am.getRunningTasks(1024);
		final String packageName = context.getPackageName();
		RunningTaskInfo taskInfo;
		final int size = tasksInfo.size();
		for(int i = 0; i < size; i++){
			taskInfo = tasksInfo.get(i);
			if(packageName.equals(taskInfo.baseActivity.getPackageName())){
				return taskInfo.numActivities == 1;
			}
		}
		
		return false;
	}

}
