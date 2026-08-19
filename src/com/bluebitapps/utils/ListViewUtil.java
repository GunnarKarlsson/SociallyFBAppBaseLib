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
import android.widget.ListView;
import android.widget.TextView;

public class ListViewUtil {
	
	public static void addTopAndBottomPadding(Context context, ListView listView){
		
		TextView padding = new TextView(context);
		
			padding.setHeight(20);
		
		listView.addHeaderView(padding);
		listView.addFooterView(padding);		
	}
}
