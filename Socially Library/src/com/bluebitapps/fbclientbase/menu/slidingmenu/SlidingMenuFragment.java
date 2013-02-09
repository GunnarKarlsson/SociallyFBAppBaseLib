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

package com.bluebitapps.fbclientbase.menu.slidingmenu;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import android.R.integer;
import android.app.Activity;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.readystatesoftware.viewbadger.BadgeView;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.OnSearchQueryListener;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.notifications.NotificationsAlertActivity;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

public class SlidingMenuFragment extends ListFragment {

	String mThemeSelection;
	int mUnreadCount = 0;

	MenuItemAdapter mAdapter;
	OnSlidingMenuItemSelectedListener mListener;
	SearchView mSearchView;
	NotificationsUpdateReceiver mDataUpdateReceiver;

	private int mTextSize;
	private int mTextColor;
	private Typeface mTypeface;

	private class NotificationsUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (Constants.ACTION_NEW_NOTIFICATIONS.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + Constants.ACTION_NEW_NOTIFICATIONS);
				mUnreadCount = 0;
				mUnreadCount = intent.getIntExtra(Constants.NOTIFICATION_COUNT_KEY, 0);
				mAdapter.notifyDataSetChanged();
			}
			
			if(NotificationsAlertActivity.MARKED_AS_READ_IN_POP_UP.equals(intent.getAction())){
				mUnreadCount = (int) FBClientApplication.getApplication().getNotificationsData().getUnreadCount();
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mDataUpdateReceiver == null) {
			mDataUpdateReceiver = new NotificationsUpdateReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Constants.ACTION_NEW_NOTIFICATIONS);
			intentFilter.addAction(NotificationsAlertActivity.MARKED_AS_READ_IN_POP_UP);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			if (getActivity() != null) {
				getActivity().registerReceiver(mDataUpdateReceiver, intentFilter);
			}
		}

		FBClientApplication app = FBClientApplication.getApplication();
		mTextSize = ThemeFactory.getFontSize(app);
		int color = ThemeFactory.getFontColor(app);
		mTextColor = getActivity().getResources().getColor(color);
		mTypeface = ThemeFactory.getFontType(app);

		mUnreadCount = (int) FBClientApplication.getApplication().getNotificationsData().getUnreadCount();

		// getNotificationsCount();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mDataUpdateReceiver != null && getActivity() != null) {
			getActivity().unregisterReceiver(mDataUpdateReceiver);
			mDataUpdateReceiver = null;
		}
	}

	public interface OnSlidingMenuItemSelectedListener {

		public void onSlidingMenuItemSelected(String selection);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnSlidingMenuItemSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnMainMenuItemSelectedListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mThemeSelection = prefs.getString(Constants.THEME_PREFERENCES_KEY, Constants.THEME_DEFAULT);
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.sliding_menu_list, null);
		
		if(Constants.THEME_IS_COLOR_PICKER_COLOR.equals(mThemeSelection)){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			int color= prefs.getInt(Constants.COLOR_PICKER_CHOICE_SLIDING_MENU, 0x000000);
			viewGroup.setBackgroundColor(color);
		}else{			
			viewGroup.setBackgroundResource(ThemeFactory.getSlidingMenuColor(getActivity(), mThemeSelection));
		}
		

		// Remove over-scroll glow.
		ListView list = (ListView) viewGroup.findViewById(android.R.id.list);
		list.setOverScrollMode(AbsListView.OVER_SCROLL_NEVER);

		// SearchManager searchManager = (SearchManager)
		// getActivity().getSystemService(Context.SEARCH_SERVICE);
		mSearchView = (SearchView) viewGroup.findViewById(R.id.searchView);
		// searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
		mSearchView.setOnQueryTextListener(mOnQueryTextListener);

		mSearchView.setIconifiedByDefault(false); // Do not iconify the widget
													// expand it by default

		return viewGroup;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ListFragment#onListItemClick(android.widget.ListView,
	 * android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		mListener.onSlidingMenuItemSelected(mAdapter.getItem(position).getName());
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new MenuItemAdapter(getActivity());

		mAdapter.add(new MenuItem(Constants.MENU_ITEM_THEMES, R.drawable.sliding_menu_icon_themes, MenuItemAdapter.TYPE_MAIN));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_COLOR_PICKER, R.drawable.sliding_menu_icon_color_picker, MenuItemAdapter.TYPE_MAIN));
		
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_NEWSFEED, R.drawable.sliding_menu_icon_newsfeed, MenuItemAdapter.TYPE_MAIN));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_FRIENDS, R.drawable.sliding_menu_icon_friends, MenuItemAdapter.TYPE_MAIN));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_PHOTOS, R.drawable.sliding_menu_icon_albums, MenuItemAdapter.TYPE_MAIN));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_NOTIFICATIONS, R.drawable.sliding_menu_icon_notifications, MenuItemAdapter.TYPE_MAIN));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_MESSAGES, R.drawable.sliding_menu_icon_messages, MenuItemAdapter.TYPE_MAIN));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_CHECKINS, R.drawable.sliding_menu_icon_checkins, MenuItemAdapter.TYPE_MAIN));
		
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_SUB_ITEM_FRIENDS_RECENT, R.drawable.sliding_menu_icon_submenu_mid, MenuItemAdapter.TYPE_SUB_MID));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_SUB_ITEM_NEARBY_LOCATIONS, R.drawable.sliding_menu_icon_submenu_end, MenuItemAdapter.TYPE_SUB_END));
		
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_EVENTS, R.drawable.sliding_menu_icon_events, MenuItemAdapter.TYPE_MAIN));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_SUB_ITEM_EVENT_LIST, R.drawable.sliding_menu_icon_submenu_mid, MenuItemAdapter.TYPE_SUB_MID));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_SUB_ITEM_EVENT_INVITED, R.drawable.sliding_menu_icon_submenu_mid, MenuItemAdapter.TYPE_SUB_MID));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_SUB_ITEM_BIRTHDAYS, R.drawable.sliding_menu_icon_submenu_end, MenuItemAdapter.TYPE_SUB_END));
		
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_GROUPS, R.drawable.sliding_menu_icon_groups, MenuItemAdapter.TYPE_MAIN));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_CHAT, R.drawable.sliding_menu_icon_chat, MenuItemAdapter.TYPE_MAIN));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_PROFILE, R.drawable.sliding_menu_icon_profile, MenuItemAdapter.TYPE_MAIN));
		
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_SUB_ITEM_ME, R.drawable.sliding_menu_icon_submenu_mid, MenuItemAdapter.TYPE_SUB_MID));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_SUB_ITEM_WALL, R.drawable.sliding_menu_icon_submenu_mid, MenuItemAdapter.TYPE_SUB_MID));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_SUB_ITEM_PHOTOS, R.drawable.sliding_menu_icon_submenu_end, MenuItemAdapter.TYPE_SUB_END));
		
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_LIKES, R.drawable.sliding_menu_icon_likes, MenuItemAdapter.TYPE_MAIN));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_SUBSCRIPTIONS, R.drawable.sliding_menu_icon_subscriptions, MenuItemAdapter.TYPE_MAIN));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_FRIEND_REQUESTS, R.drawable.sliding_menu_icon_friend_requests, MenuItemAdapter.TYPE_MAIN));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_SETTINGS, R.drawable.sliding_menu_icon_settings, MenuItemAdapter.TYPE_MAIN));
		//mAdapter.add(new MenuItem(Constants.MENU_ITEM_RATE, R.drawable.sliding_menu_icon_rate, MenuItemAdapter.TYPE_MAIN));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_ABOUT, R.drawable.sliding_menu_icon_about, MenuItemAdapter.TYPE_MAIN));
		mAdapter.add(new MenuItem(Constants.MENU_ITEM_LOGOUT, R.drawable.sliding_menu_icon_logout, MenuItemAdapter.TYPE_MAIN));

		for (int i = 0; i < mAdapter.getCount(); i++) {
			mAdapter.addItemtoPositionMap(i, mAdapter.getItem(i).getType());
		}

		setListAdapter(mAdapter);
	}

	private class MenuItem {
		private String name;
		private Drawable drawable;
		private int type;

		public MenuItem(String tag, int iconRes, int type) {
			this.name = tag;
			this.drawable = (Drawable) getResources().getDrawable(iconRes);
			this.type = type;
		}

		public Drawable getIcon() {
			return drawable;
		}

		public String getName() {
			return name;
		}

		public int getType() {
			return type;
		}

	}

	public class MenuItemAdapter extends ArrayAdapter<MenuItem> {

		private static final int TYPE_MAIN = 0;
		private static final int TYPE_SUB_MID = 1;
		private static final int TYPE_SUB_END = 2;
		private static final int TYPE_MAX_COUNT = TYPE_SUB_END + 1;

		private Map<Integer, Integer> positionToTypeMap = new HashMap<Integer, Integer>();

		private class ViewHolder {
			public ImageView icon;
			public TextView title;
			public TextView badgeHolder;
			public BadgeView badgeView;
		}

		public MenuItemAdapter(Context context) {
			super(context, 0);
		}

		public void addItemtoPositionMap(int position, int type) {
			positionToTypeMap.put(position, type);
		}

		@Override
		public int getItemViewType(int position) {
			return positionToTypeMap.get(position);
		}

		@Override
		public int getViewTypeCount() {
			return TYPE_MAX_COUNT;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			int type = getItemViewType(position);
			View view = convertView;
			ViewHolder holder = null;

			if (convertView == null) {
				holder = new ViewHolder();
				switch (type) {
				case TYPE_MAIN:
					view = LayoutInflater.from(getContext()).inflate(R.layout.sliding_menu_item, null);
					holder.icon = (ImageView) view.findViewById(R.id.row_icon);
					holder.title = (TextView) view.findViewById(R.id.row_title);
					holder.badgeHolder = (TextView) view.findViewById(R.id.badgeHolder);
					holder.badgeView = new BadgeView(getActivity(), holder.badgeHolder);
					holder.badgeView.setBadgePosition(BadgeView.POSITION_CENTER);
					holder.badgeView.setBadgeBackgroundColor(Color.parseColor("#00000000"));
					break;

				case TYPE_SUB_END:
					view = LayoutInflater.from(getContext()).inflate(R.layout.sliding_menu_item_sub, null);
					holder.icon = (ImageView) view.findViewById(R.id.sub_item_icon);
					holder.title = (TextView) view.findViewById(R.id.sub_item_title);
					break;

				case TYPE_SUB_MID:
					view = LayoutInflater.from(getContext()).inflate(R.layout.sliding_menu_item_sub, null);
					holder.icon = (ImageView) view.findViewById(R.id.sub_item_icon);
					holder.title = (TextView) view.findViewById(R.id.sub_item_title);
					break;
				}
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.icon.setImageDrawable(this.getItem(position).getIcon());
			holder.title.setText(this.getItem(position).getName());

			if (type == TYPE_MAIN) {

				if (this.getItem(position).getName().equalsIgnoreCase("Notifications")) {
					holder.badgeView.setText(Integer.toString(mUnreadCount));
					holder.badgeView.show();
				} else {
					holder.badgeView.setText("");
					holder.badgeView.hide();
				}
			}
			return view;
		}
	}

	final private OnQueryTextListener mOnQueryTextListener = new OnQueryTextListener() {

		@Override
		public boolean onQueryTextChange(String newText) {
			if (TextUtils.isEmpty(newText)) {
				// getActivity().getActionBar().setSubtitle("List");
				// grid_currentQuery = null;
				Logger.i("SlidingMenuFragment: search is empty");
			} else {
				// getActivity().getActionBar().setSubtitle("List - Searching for: "
				// + newText);
				// grid_currentQuery = newText;
				Logger.i("SlidingMenuFragment: search textChange " + newText);

			}
			// getLoaderManager().restartLoader(0, null, MyListFragment.this);
			return false;
		}

		@Override
		public boolean onQueryTextSubmit(String query) {
			Logger.i("search text submit " + query);
			// Pass query to Activity
			((OnSearchQueryListener) mListener).onSearchQuery(query);
			return true;
		}
	};

	private int getTextSize() {
		int textSize = mTextSize;

		// if(textSize > 26){
		// textSize = 26;
		if (textSize < 26) {
			textSize = 26;
		}

		return textSize;
	}
}