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

package com.bluebitapps.fbclientbase.search;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;

import com.bluebitapps.utils.InputUtil;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.events.EventsFragment;
import com.bluebitapps.fbclientbase.friends.FriendsFragment;
import com.bluebitapps.fbclientbase.groups.GroupsFragment;
import com.bluebitapps.fbclientbase.likes.LikesFragment;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

public class SearchFragment extends BaseFragment {

	private String mSearchQuery;
	private int mSpinnerSelection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Logger.i(Logger.getClassAndMethod());
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		mSearchQuery = getArguments().getString(Constants.SEARCH_QUERY_KEY);
		//SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(), SuggestionsProvider.AUTHORITY, SuggestionsProvider.MODE);
		//suggestions.saveRecentQuery(mSearchQuery, null);

		InputUtil.hideKeyboard(getActivity());

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup vg = null;

		if (getActivity() != null) {

			vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.search_list);
			Button button = (Button) vg.findViewById(R.id.searchButton);
			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					InputUtil.hideKeyboard(getActivity());
					startSearch();

				}
			});
			Spinner spinner = (Spinner) vg.findViewById(R.id.searchOptionsSpinner);
			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

					mSpinnerSelection = position;
					InputUtil.hideKeyboard(getActivity());

				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub

				}
			});

		}

		return vg;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if(getActivity()!=null){
			ActionBar actionBar = getActivity().getActionBar();
			if(actionBar!=null){
				actionBar.setTitle(R.string.search);
				actionBar.setSubtitle(null);
			}
		}
	}


	/**
	 * Performs search using URL https://graph.facebook.com/search?q=QUERY&type=OBJECT_TYPE
	 */
	private void startSearch() {
		// 0 - User
		if (mSpinnerSelection == 0) {
			FriendsFragment userFragment = FriendsFragment.newInstance(null, FriendsFragment.STATE_SEARCH, mSearchQuery);
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.addToBackStack(null);
			ft.replace(android.R.id.content, userFragment).commit();
		}

		// 1 - Page
		if (mSpinnerSelection == 1) {
			LikesFragment likesFragment = LikesFragment.newInstance(getObjectId(), LikesFragment.STATE_SEARCH, mSearchQuery);
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.addToBackStack(null);
			ft.replace(android.R.id.content, likesFragment).commit();
		}
		// 2 - Event
		if (mSpinnerSelection == 2) {
			EventsFragment eventsFragment = EventsFragment.newInstance(LikesFragment.STATE_SEARCH, mSearchQuery);
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.addToBackStack(null);
			ft.replace(android.R.id.content, eventsFragment).commit();
		}
		// 3 - Group
		if (mSpinnerSelection == 3) {
			GroupsFragment groupsFragment = GroupsFragment.newInstance(GroupsFragment.STATE_SEARCH, mSearchQuery);
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.addToBackStack(null);
			ft.replace(android.R.id.content, groupsFragment).commit();
		}

	}
}