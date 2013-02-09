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

package com.bluebitapps.fbclientbase.groups;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;

public class Group {

	private String version;
	private String name;
	private String id;
	private String unread;
	private String bookmarkOrder;
	private String description;
	
	public static Group fromJSON(JSONObject obj) {
		Group group = new Group();

		try {
			group.setId(obj.getString("id"));
			group.setName(obj.getString("name"));
			group.setVersion(obj.getString("version"));
			group.setUnread(obj.getString("unread"));
			group.setBookmarkOrder(obj.getString("bookmark_order"));
			group.setDescription(obj.getString("description"));

		} catch (JSONException e) {
			// TODO: handle exception
		}

		return group;
	}

	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		values.clear();
		values.put(GroupsData.C_ID, getId());
		values.put(GroupsData.C_NAME, getName());
		values.put(GroupsData.C_VERSION, getVersion());
		values.put(GroupsData.C_UNREAD, getUnread());
		values.put(GroupsData.C_BOOKMARK_ORDER, getBookmarkOrder());
		values.put(GroupsData.C_DESCRIPTION, getDescription());
		return values;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUnread() {
		return unread;
	}

	public void setUnread(String unread) {
		this.unread = unread;
	}

	public String getBookmarkOrder() {
		return bookmarkOrder;
	}

	public void setBookmarkOrder(String bookmarkOrder) {
		this.bookmarkOrder = bookmarkOrder;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
