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

package com.bluebitapps.fbclientbase;

import android.content.Intent;
import com.bluebitapps.fbclientbase.R;


public class Constants {

	public static final String TAB_INDEX_KEY = "tab index key";
	public static final String TAB_INDEX_WALL ="wall";
	
	public static final String[] forbiddenStrings = {"shared a page","likes a photo", "is going to an event", "attended an event", "likes a link", "likes a status", "created an event","commented on an", "commented on a"};
	public static final String[] forbiddenStringsForNotifications =  {"shared","likes a photo", "is going to an event", "attended an event", "likes a link", "likes a status", "created an event","commented on an", "commented on a"};

	public static final String[] forbiddenStrings_ES = {"compartió una página","le gusta una foto","asistirá a un evento","asistió a un evento","le gusta un enlace","le gusta una actualización de estado","ha creado un evento","ha comentado"};
	public static final String[] forbiddenStringsForNotifications_ES = {"compartió","le gusta una foto","asistirá a un evento","asistió a un evento","le gusta un enlace","le gusta una actualización de estado","ha creado un evento","ha comentado"};
	
	public static final String[]forbiddenStrings_PT = {"compartilhou uma página","curtiu uma foto","vai participar de um evento","participou de um evento","curtiu um link","curtiu uma atualização de status","criou um evento","comentou"};
	public static final String[]forbiddenStringsForNotifications_PT = {"compartilhou","curtiu uma foto","vai participar de um evento","participou de um evento","curtiu um link","curtiu uma atualização de status","criou um evento","comentou"};
	
	public static final String ACTION_PHOTO_UPLOAD_RESULT = "action photo upload result";
	
	public static final String USAGE_COUNT_KEY = "usage counter key";

	//Section selection
	public static final String REQUEST_PROFILE = "tabbed view request profile";
	public static final String REQUEST_EVENT_SINGLE = "tabbed view request event single";
	public static final String TABBED_VIEW_REQUEST_GROUPS = "tabbed view request group";

	public static final String TABBED_VIEW_REQUEST_GROUP_SINGLE = "tabbed view request group";
	public static final String TABBED_VIEW_REQUEST_EVENTS = "tabbed view request events";
	public static final String REQUEST_NOTIFICATIONS = "tabbed view notifications";
	public static final String REQUEST_PAGES = "tabbed view request pages";
	public static final String REQUEST_PLACE_PROFILE = "tabbed view request place";
	public static final String REQUEST_FRIEND_REQUESTS = "request friend requests";
	public static final String REQUEST_ALBUMS = "request albums";
	public static final String REQUEST_IMAGE_PAGER = "request image pager";
	public static final String STATE_IMAGE_GRID = "state_image_grid";
	public static final String REQUEST_PROFILE_ACTIVITY = "request profile activity";
	public static final String REQUEST_PLACE_PROFILE_ACTIVITY = "request place profile activity";
	
	public static final String TAB_INDEX_SELECTION_KEY = "tabbed index selection key";

	// Sliding Menu items
	public static final String MENU_ITEM_NEWSFEED = "News feed";
	public static final String MENU_ITEM_COLOR_PICKER = "Color picker";
	public static final String MENU_ITEM_THEME_TEST = "Theme test";
	public static final String MENU_ITEM_PHOTOS = "Photos";
	public static final String MENU_ITEM_NOTIFICATIONS = "Notifications";
	public static final String MENU_ITEM_MESSAGES = "Messages";
	public static final String MENU_ITEM_EVENTS = "Events";
	public static final String MENU_ITEM_GROUPS = "Groups";
	public static final String MENU_ITEM_SETTINGS = "Settings";
	public static final String MENU_ITEM_ABOUT = "About";
	public static final String MENU_ITEM_FRIENDS = "Friends";
	public static final String MENU_ITEM_CHECKINS = "Checkins";
	public static final String MENU_ITEM_PROFILE = "Profile";
	public static final String MENU_ITEM_IMAGE_EDIT = "Image Edit";
	public static final String MENU_ITEM_THEMES = "Themes";
	public static final String MENU_ITEM_ACCOUNTS = "Accounts";
	public static final String MENU_ITEM_CHAT = "Chat";
	public static final String MENU_ITEM_LIKES = "Likes";
	public static final String MENU_ITEM_SUBSCRIPTIONS = "Subscriptions";
	public static final String MENU_ITEM_FRIEND_REQUESTS = "Friend requests";
	public static final String MENU_ITEM_LOGOUT = "Logout";
	public static final String MENU_ITEM_SUB_ITEM_FRIENDS_RECENT = "Friends' recent";
	public static final String MENU_ITEM_SUB_ITEM_NEARBY_LOCATIONS = "Nearby locations";
	public static final String MENU_ITEM_SUB_ITEM_EVENT_LIST = "Attending";
	public static final String MENU_ITEM_SUB_ITEM_EVENT_INVITED = "Invitations";
	public static final String MENU_ITEM_SUB_ITEM_BIRTHDAYS = "Birthdays";
	public static final String MENU_ITEM_RATE ="Rate in market";
	public static final String MENU_ITEM_REMOVE_ADS = "Remove ads";
	
	public static final String MENU_ITEM_SUB_ITEM_ME = "Me";
	public static final String MENU_ITEM_SUB_ITEM_WALL = "My wall";
	public static final String MENU_ITEM_SUB_ITEM_PHOTOS = "My photos";
	
	// Sliding menu config
	public static final int SLIDING_MENU_BEHIND_OFFSET_LARGE_SCREEN_LANDSCAPE = 700;
	public static final int SLIDING_MENU_BEHIND_OFFSET_LARGE_SCREEN_PORTRAIT = 300;
	public static final int SLIDING_MENU_BEHIND_OFFSET_SMARTPHONE_SCREEN_LANDSCAPE = 200;
	

	public static final String THEME_DEFAULT = "theme_blue";

	//Fragment states
	public static final String STATE_KEY = "state key";
	
	public static final String STATE_EVENT_INVITED = "state event invited";
	public static final String STATE_EVENT_ATTENDING = "state event attending";
	public static final String STATE_EVENT_MAYBE = "state event maybe";
	public static final String STATE_EVENT_DECLINED = "state event declined";	
	
	public static final String STATE_EVENT_WALL = "state event wall";
	public static final String STATE_EVENT_PHOTOS = "state event photos";
	
	public static final String STATE_NEWSFEED_CURRENT_USER = "state newsfeed current user";
	public static final String STATE_PROFILE = "state profile";

	
	
	public static final String TRUE = "true";
	public static final String FALSE = "false";

	//keys for maps (intents, bundles, prefs)
	public static final String INTENT_THEME_KEY = "intent theme key";
	public static final String INTENT_OBJECT_ID_KEY = "intent object id key";
	public static final String LIKE_OBJECT_ID_KEY = "like object key";
	public static final String COMMENT_OBJECT_ID_KEY = "comment object id key";
	public static final String MESSAGE_KEY = "message key";
	public static final String THEME_PREFERENCES_KEY = "theme preferences key";
	public static final String SEARCH_QUERY_KEY = "search query key";
	public static final String OBJECT_URL_KEY = "url key";
	// preferences
	// TODO - are these needed / used ?
	/*
	 * public static final String PREFS_NOTIFICATIONS_KEY =
	 * "prefs notifications frequency"; public static final String
	 * PREFS_NOTIFICATIONS_DEFAULT_VALUE = "60"; public static final String
	 * PREFS_THEME_KEY = "prefs theme key"; public static final String
	 * PREFS_THEME_DEFAULT_VALUE = "white";
	 */

	// OnFragmentTappedListener events
	public static final String FRAGMENT_TAPPED_NEWSFEED_ACTIONS = "Fragment tapped newsfeed actions";

	// TODO - are these needed/used?
	public static final String FRAGMENT_TAG_ALBUMS = "fragment tag albums";
	public static final String FRAGMENT_TAG_NEWSFEED = "fragment tag newsfeed";
	public static final String FRAGMENT_TAG_NOTIFICATIONS = "fragment tag notifications";
	public static final String FRAGMENT_TAG_THEME_TEST = "fragment tag theme test";

	/*
	 * public static final String NEWSFEED_ITEM_ID = "newsfeed_item_id"; public
	 * static final String NEWSFEED_ITEM_FROM_ID = "newsfeed_item_from_id";
	 * public static final String NEWSFEED_ITEM_FROM_NAME =
	 * "newsfeed_item_from_name";
	 */
	
	//usage counting
	public static final String USAGE_COUNTER_KEY = "usage_counter_key";
	public static final int USAGE_COUNTER_EMPTY_VALUE = 0;	

	//Action types
	public static final String ACTION_TYPE = "action type";
	public static final String ACTION_TYPE_UNLIKE = "action type unlike";
	public static final String ACTION_TYPE_LIKE = "action type like";
	public static final String ACTION_TYPE_COMMENT = "action type comment";
	public static final String ACTION_TYPE_DELETE_COMMENT = "action type delete comment";

	// Newsfeed types
	public static final String STATUS = "status";
	public static final String CHAT_USER_JABBER_ID_KEY = "chat user jabber id key";
	public static final String CHAT_USER_NAME_KEY = "chat user name key";
	public static final String EVENT_OBJECT_ID_KEY = "event object id key";
	public static final String EVENT_NAME_KEY = "event name key";
	
	public static final int USER_TYPE_FRIEND = 0x0000001;
	public static final int USER_TYPE_CURRENT_USER = 0x0000002;
	public static final int USER_TYPE_NONE = 0x00000003;
	
	public static final String USER_ID_KEY = "user id key";
	public static final String CREATED_TIME_KEY = "created time key";
	public static final String USER_TYPE_KEY = "user type key";
	public static final String EVENT_USER_TYPE_KEY = "event user type key";
	public static final String EVENT_USER_TYPE_INVITED = "event user type invited";
	public static final String EVENT_USER_TYPE_ATTENDING = "event user type attending";
	public static final String EVENT_USER_TYPE_MAYBE = "event user type maybe";
	public static final String EVENT_USER_TYPE_DECLINED = "event user type declined";
	
	public static final String OBJECT_ID_KEY = "object id key";
	public static final String OBJECT_TITLE_KEY = "object title key";
	public static final String OBJECT_TYPE_KEY = "object type key";
	public static final String STATE_USER_ALBUM = "state user album";

	public static final String ACTION_STATUS_UPDATE_RESULT = "action status update result";

	public static final String ACTION_NEW_NOTIFICATIONS = "action new notifications";

	public static final String NOTIFICATION_COUNT_KEY = "notification count key";
	public static final String PHOTO_ACCESS_VIA_NOTIFICATION = "photo access via notification";

	public static final String SEARCH_STRING = "search string";

	public static final String CLEAR_TOP_ON_HOME_SELECTED = "clear top on home selected";
	public static final String FORCE_AUTH_FLAG = "force auth flag";
	
	public static final String EVENT_INVITATION_RESPONSE_JOIN = "event invitation response join";
	public static final String EVENT_INVITATION_RESPONSE_MAYBE = "event invitation response maybe";
	public static final String EVENT_INVITATION_RESPONSE_DECLINE = "event invitation response decline";
	
	public static final String THEME_IS_COLOR_PICKER_COLOR = "theme_is_color_picker_color";
	public static final String COLOR_PICKER_CHOICE = "color picker choice";
	public static final String COLOR_PICKER_CHOICE_SLIDING_MENU = "color picker choice sliding menu";
	public static final String COLOR_PICKER_CHOICE_ACTIONBAR = "color picker choice actionbar";
	
	//request codes
	public static final int COLOR_PICKER_LAUNCH_REQUEST_CODE = 120;
	
	
}
