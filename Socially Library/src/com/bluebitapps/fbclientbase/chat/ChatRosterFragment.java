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

package com.bluebitapps.fbclientbase.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bluebitapps.utils.ClearableEditText;
import com.bluebitapps.utils.InputUtil;
import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.ClearableEditText.OnClearClickListener;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.friends.Friend;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

public class ChatRosterFragment extends BaseNavigationFragment implements OnClearClickListener{

	private List<ChatUser> mRoster;
	private List<ChatUser>mOriginalChatUserList;
	private ItemAdapter mAdapter;
	private static final String SAVED_ROSTER_INSTANCE_STATE_KEY = "saved roster instance state key";

	private boolean hasSavedInstanceState;
	private boolean isConnected;
	private LoadingView mLoadingView;
	private ClearableEditText mEditText;
	private CustomTextWatcher mTextWatcher;
	private ListView mListView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mRoster = new ArrayList<ChatUser>();

		if (savedInstanceState != null) {
			Logger.i(Logger.getClassAndMethod() + " savedInstanceState != null");
		} else {
			Logger.i(Logger.getClassAndMethod() + "savedInstanceState == null");
		}

		if (savedInstanceState != null && savedInstanceState.getParcelableArrayList(SAVED_ROSTER_INSTANCE_STATE_KEY) != null) {
			mRoster = savedInstanceState.getParcelableArrayList(SAVED_ROSTER_INSTANCE_STATE_KEY);
			Logger.i(Logger.getClassAndMethod() + "mRoster.size(): " + mRoster.size());
			hasSavedInstanceState = true;
		} else {
			hasSavedInstanceState = false;
		}

		if (getActivity() == null) {
			return;
		}

		setHasOptionsMenu(true);

		setTitle("Chat");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup vg = null;

		if (getActivity() != null) {
//old: item_list
			vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.image_list_search);

			mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);

			mEditText = (ClearableEditText)vg.findViewById(R.id.edit_text);
			mEditText.setOnClearClickListener(this);
			mTextWatcher = new CustomTextWatcher();
			mEditText.addTextChangedListener(mTextWatcher);
			
		    mListView = (ListView) vg.findViewById(R.id.image_list_view);
			TextView padding = new TextView(getActivity());
			padding.setHeight(getResources().getDimensionPixelOffset(R.dimen.item_list_padding));
			mListView.addHeaderView(padding);
			mListView.addFooterView(padding);


			mAdapter = new ItemAdapter();
			mListView.setAdapter(mAdapter);
			mListView.setTextFilterEnabled(true);
		}

		return vg;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(SAVED_ROSTER_INSTANCE_STATE_KEY, (ArrayList<ChatUser>)mRoster);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onRefresh() {
		Logger.i(Logger.getClassAndMethod());
		connect();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (hasSavedInstanceState) {
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
		} else if (!isConnected) {
			connect();
		}
	}

	/**
	 * connect(): Based on
	 * http://chat.stackoverflow.com/transcript/5098/2012/3/23/9-12
	 * 
	 * @author Gunnar Karlsson
	 */

	private void connect() {
		
		startRefreshMenuItemAnimation();

		Thread thread = new Thread() {

			@Override
			public void run() {

				OutputUtil.showCrouton(getActivity(), "Connecting to chat...");

				XMPPConnectionSingleton connectionSingleton = XMPPConnectionSingleton.getInstance();

				try{
					
				XMPPConnection connection = connectionSingleton.getConnection();
				
				if(connection == null){
					if(getActivity()!=null){						
						OutputUtil.showCrouton(getActivity(), "Could not connect to chat");
					}
					return;
				}
				
				connection.addConnectionListener(new FBChatConnectionListener());

				isConnected = true;
				Roster roster = connection.getRoster();
				
				if(roster == null){
					OutputUtil.showCrouton(getActivity(), "Could not retrieve friend list");
				}

				Collection<RosterEntry> entries = roster.getEntries();
				
				if(entries == null){
					OutputUtil.showCrouton(getActivity(), "Could not retrieve friend list");
				}

				OutputUtil.showCrouton(getActivity(), "Connected to chat.");
				stopRefreshMenuItemAnimation();

				Logger.i(Logger.getClassAndMethod() + "connect(). \n\n" + entries.size() + " buddy(ies):");

				final ArrayList<ChatUser> rosterList = new ArrayList<ChatUser>();

				for (RosterEntry entry : entries) {
					ChatUser chatUser = new ChatUser();
					chatUser.setName(entry.getName());
					chatUser.setJabberId(entry.getUser());
					rosterList.add(chatUser);
				}

				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {

						@Override
						public void run() {
							if (mRoster != null) {
								mRoster.clear();
								mRoster = rosterList;
							}
							if (mAdapter != null) {
								mAdapter.notifyDataSetChanged();
							}
							if(mLoadingView != null){								
								mLoadingView.setVisibility(View.GONE);
							}
						}
					});
				}
				}catch(IllegalStateException e){
					OutputUtil.showCrouton(getActivity(), "Could not connect to chat server");
				}
			}
		};

		thread.start();

	}

	private class FBChatConnectionListener implements ConnectionListener {

		@Override
		public void connectionClosed() {
			Logger.i(ChatRosterFragment.class.getSimpleName() + "." + FBChatConnectionListener.class.getSimpleName() + "#connectionClosed");
			OutputUtil.showCrouton(getActivity(), "Chat connection closed...");

		}

		@Override
		public void connectionClosedOnError(Exception arg0) {
			Logger.i(ChatRosterFragment.class.getSimpleName() + "." + FBChatConnectionListener.class.getSimpleName() + "#connectionClosedOnError");
			OutputUtil.showCrouton(getActivity(), "Chat connection error...");

		}

		@Override
		public void reconnectingIn(int arg0) {
			Logger.i(ChatRosterFragment.class.getSimpleName() + "." + FBChatConnectionListener.class.getSimpleName() + "#reconnectingIn");
			OutputUtil.showCrouton(getActivity(), "Reconnecting to chat...");

		}

		@Override
		public void reconnectionFailed(Exception arg0) {
			Logger.i(ChatRosterFragment.class.getSimpleName() + "." + FBChatConnectionListener.class.getSimpleName() + "#reconnectionFailer");
			OutputUtil.showCrouton(getActivity(), "Chat reconnection failed...");

		}

		@Override
		public void reconnectionSuccessful() {
			Logger.i(ChatRosterFragment.class.getSimpleName() + "." + FBChatConnectionListener.class.getSimpleName() + "#reconnectionSuccessful");
			OutputUtil.showCrouton(getActivity(), "Connected to chat again...");
		}

	}

	class ItemAdapter extends BaseAdapter implements Filterable{

		private class ViewHolder {
			public ImageView picture;
			public TextView name;
		}

		@Override
		public int getCount() {
			if (mRoster != null) {
				return mRoster.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			if (mRoster != null) {
				return mRoster.get(position);
			} else {
				return new ChatUser();
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final ChatUser chatUser = mRoster.get(position);

			View view = convertView;
			final ViewHolder holder;
			if (convertView == null && getActivity() != null) {
				view = getActivity().getLayoutInflater().inflate(R.layout.friend_item, null);

				holder = new ViewHolder();
				holder.name = (TextView) view.findViewById(R.id.friendName);
				configBodyText(holder.name);
				holder.picture = (ImageView) view.findViewById(R.id.image);

				view.setTag(holder);
			} else
				holder = (ViewHolder) view.getTag();

			if (mRoster != null) {

				holder.name.setText(mRoster.get(position).getName());

				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (getActivity() != null) {

							Intent intent = new Intent(getActivity(), ChatConversationActivity.class);
							intent.putExtra(Constants.CHAT_USER_JABBER_ID_KEY, chatUser.getJabberId());
							intent.putExtra(Constants.CHAT_USER_NAME_KEY, chatUser.getName());
							intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
							getActivity().startActivity(intent);
							getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
							
						}
					}
				});

				String token = getApplication().getFBConnection().getFacebook().getAccessToken();

				String query = "https://graph.facebook.com/" + chatUser.getFbId() + "/picture?access_token=" + token;
				Logger.i(query);

				getImageLoader().displayImage(query, holder.picture, getImageDisplayOptions());
			}

			return view;
		}

		@Override
		public Filter getFilter() {

			Filter filter = new Filter() {

				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					mRoster = (List<ChatUser>) results.values;
					notifyDataSetChanged();
				}

				@Override
				protected FilterResults performFiltering(CharSequence constraint) {

					List<ChatUser> chatUSerMatchingSearch = new ArrayList<ChatUser>();

					FilterResults results = new FilterResults();
					chatUSerMatchingSearch.clear();

					if (constraint == null || constraint.length() == 0) {
						if (mRoster != null) {

							results.count = mRoster.size();
							results.values = mRoster;

							chatUSerMatchingSearch = mRoster;
						}
					} else {
						constraint = constraint.toString().toLowerCase();
						if (mRoster != null) {

							for (int i = 0; i < mRoster.size(); i++) {
								String name = mRoster.get(i).getName().toLowerCase();
								if (name.toLowerCase().startsWith(constraint.toString())) {
									chatUSerMatchingSearch.add(mRoster.get(i));
								}
							}

							results.count = chatUSerMatchingSearch.size();
							results.values = chatUSerMatchingSearch;
						}
					}

					return results;
				}
			};

			return filter;
		}
	}

	@Override
	public void onClearButtonClicked() {
		Logger.i(Logger.getClassAndMethod());
		if (mOriginalChatUserList != null) {
			mRoster = new ArrayList<ChatUser>(mOriginalChatUserList);
		}
		mAdapter.notifyDataSetChanged();
		InputUtil.hideKeyboard(getActivity());
	}
	
	class CustomTextWatcher implements TextWatcher{

		@Override
		public void afterTextChanged(Editable arg0) {
			//Do nothing			
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			//Do nothing			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			mAdapter.getFilter().filter(s);
			mAdapter.notifyDataSetChanged();
			
		}
		
	}
}