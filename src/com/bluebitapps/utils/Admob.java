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

public class Admob {

	private final static String id = "a150ab340c1545c";//For Socially
	private final static String pinkId = "a1511b12cb7ae45";//For Socially Pink
	private final static String testDevice = "E9A58317C31E13C81BDE2E72E4A29663";
	
	private static boolean isDebugging = true;
	
	public static String getId() {
		return id;
	}
	
	public static String getPinkId(){
		return pinkId;
	}
	
	public static String getTestdevice() {
		return testDevice;
	}
	
	public static boolean isDebugging(){
		return isDebugging;
	}
	
}
