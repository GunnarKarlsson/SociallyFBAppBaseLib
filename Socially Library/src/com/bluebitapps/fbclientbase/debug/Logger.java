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

package com.bluebitapps.fbclientbase.debug;

import android.util.Log;

public class Logger {

	private static final boolean isLogEnabled = true;
	private static final String TAG = "globalTag";

	public static void i(String message) {
		if (isLogEnabled) {
			Log.i(TAG, message);
		}
	}

	public static void i(String tag, String message) {
		if (isLogEnabled) {
			Log.i(tag, message);
		}
	}

	public static String getMethodName() {
		return getMethodName(3);
	}

	/**
	 * 
	 * @param depth. 2 will return calling method name
	 * @return
	 */
	
	public static String getMethodName(final int depth) {
		final StackTraceElement[] stackTraceElement = Thread.currentThread().getStackTrace();
		return  " " + stackTraceElement[1 + depth].getMethodName() + " ";
	}
	
	public static String getClassAndMethod(){
		return getClassAndMethod(3);
	}
	
	public static String getClassAndMethod(final int depth) {
		final StackTraceElement[] stackTraceElement = Thread.currentThread().getStackTrace();
		String fileName = stackTraceElement[1 + depth].getFileName();
		String className = fileName.substring(0, fileName.lastIndexOf('.'));
		return  className + " " + stackTraceElement[1 + depth].getMethodName() + " ";
	}

}