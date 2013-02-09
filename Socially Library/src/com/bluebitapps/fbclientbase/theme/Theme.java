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

package com.bluebitapps.fbclientbase.theme;

public class Theme {

	private String displayName;
	private String themeName;
	private int icon;
	private int backgroundResource;
	private int slidingMenuColor;
	private int actionBarColor;
	private String resourceName;

	public Theme() {
	}

	public void setResourceName(String value){
		resourceName = value;
	}
	
	public String getResourceName(){
		return resourceName;
	}
	
	public String getDisplayName(){
		return displayName;
	}
	
	public void setDisplayName(String name){
		displayName = name;
	}
	
	public String getThemeName() {
		return themeName;
	}

	public void setThemeName(String themeName) {
		this.themeName = themeName;
	}
	
	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public int getBackgroundResource() {
		return backgroundResource;
	}

	public void setBackgroundResource(int backgroundResource) {
		this.backgroundResource = backgroundResource;
	}

	public int getSlidingMenuColor() {
		return slidingMenuColor;
	}

	public void setSlidingMenuColor(int slidingMenuColor) {
		this.slidingMenuColor = slidingMenuColor;
	}

	public int getActionBarColor() {
		return actionBarColor;
	}

	public void setActionBarColor(int actionBarColor) {
		this.actionBarColor = actionBarColor;
	}

}