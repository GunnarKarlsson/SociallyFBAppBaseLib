/**
* Copyright 2012 Gunnar Karlsson.
*/

package com.bluebitapps.fbclientbase.actions;

public class Like {
	
	private String mId;
	private String mName;
	
	public Like() {

	}

	public Like(String id, String name) {
		mId = id;
		mName = name;
	}

	public void setId(String id) {
		mId = id;
	}
	
	public String getId(){
		return mId;
	}
	
	public void setName(String name){
		mName = name;
	}
	
	public String getName(){
		return mName;
	}

}
