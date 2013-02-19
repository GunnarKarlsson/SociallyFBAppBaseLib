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

package com.bluebitapps.fbclientbase.messages;

import java.util.ArrayList;
import java.util.List;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bluebitapps.utils.FacebookUtils;
import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.chat.ChatConversationActivity;
import com.bluebitapps.fbclientbase.chat.ChatConversationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

/**
 * 
 * @author Gunnar Karlsson
 * 
 */
public class MessagesFragment extends BaseNavigationFragment {

	private List<MessageThread> mMessageThreads;
	private ListView mListView;
	private ItemAdapter mAdapter;
	private LoadingView mLoadingView;
	private MessagesDataUpdateReceiver mDataUpdateReceiver;
	private boolean isFirstDataRequest;

	private class MessagesDataUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (MessagesService.REFRESH_MESSAGES_DATA_SUCCESS.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + MessagesService.REFRESH_MESSAGES_DATA_SUCCESS);
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();

				getMessages();
			}

			if (MessagesService.REFRESH_MESSAGES_DATA_FAIL.equals(intent.getAction())) {
				Logger.i(Logger.getClassAndMethod() + MessagesService.REFRESH_MESSAGES_DATA_FAIL);
				if (isFirstDataRequest) {
					isFirstDataRequest = false;
				}

				stopRefreshMenuItemAnimation();

				OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.new_messages_could_not_be_retrieved));
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (getActivity() != null) {
			getActivity().getActionBar().setDisplayShowTitleEnabled(true);
			getActivity().getActionBar().setTitle(getTitle());
		}

		if (mDataUpdateReceiver == null) {
			mDataUpdateReceiver = new MessagesDataUpdateReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(MessagesService.REFRESH_MESSAGES_DATA_SUCCESS);
			intentFilter.addAction(MessagesService.REFRESH_MESSAGES_DATA_FAIL);
			intentFilter.addCategory("com.bluebitapps.fbclientbase");
			if (getActivity() != null) {
				getActivity().registerReceiver(mDataUpdateReceiver, intentFilter);
			}
		}
		startRefreshMenuItemAnimation();

		getMessages();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mDataUpdateReceiver != null && getActivity() != null) {
			getActivity().unregisterReceiver(mDataUpdateReceiver);
			// An activity can be resumed after pausing, so the activity may not
			// register the listener in the second call to onResume.
			mDataUpdateReceiver = null;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setHasOptionsMenu(true);
		isFirstDataRequest = true;
		prepareRefreshMenuItemAnimation();

		setTitle(getResources().getString(R.string.messages_menu_item));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup vg = null;

		if (getActivity() != null) {

			vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(), inflater, container, R.layout.message_threads_list_view);

			mMessageThreads = new ArrayList<MessageThread>();
			mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);

			mListView = (ListView) vg.findViewById(R.id.message_threads_listview);

			mAdapter = new ItemAdapter();

			mListView.setAdapter(mAdapter);
		}

		return vg;
	}

	@Override
	public void onRefresh() {
		Logger.i(Logger.getClassAndMethod());
		getMessagesFromFB();
	}

	private void getMessages() {

		getMessagesFromDatabase();

		if (isFirstDataRequest) {

			if (mMessageThreads.size() > 0) {
				mLoadingView.setVisibility(View.GONE);
			}
			if (getActivity() != null) {
				getActivity().invalidateOptionsMenu();
			}
			getMessagesFromFB();
		} else {
			if (mMessageThreads.size() < 1) {
				OutputUtil.showCrouton(getActivity(), getResources().getString(R.string.no_messages_available));
			}
			mLoadingView.setVisibility(View.GONE);
			if (getActivity() != null) {
				getActivity().invalidateOptionsMenu();
			}
		}
	}

	private void getMessagesFromDatabase() {
		if (getActivity() == null) {
			return;
		}

		mMessageThreads.clear();

		Cursor c = ((FBClientApplication) getActivity().getApplication()).getMessagesData().getMessages();

		if (c != null) {
			if (c.moveToFirst()) {
				do {

					String id = c.getString(c.getColumnIndex(MessageThreadData.C_ID));
					String subject = c.getString(c.getColumnIndex(MessageThreadData.C_SUBJECT));
					String recipients = c.getString(c.getColumnIndex(MessageThreadData.C_RECIPIENTS));
					String snippet = c.getString(c.getColumnIndex(MessageThreadData.C_SNIPPET));
					String unread = c.getString(c.getColumnIndex(MessageThreadData.C_UNREAD));
					String updatedTime = c.getString(c.getColumnIndex(MessageThreadData.C_UPDATED_TIME));
					String friendName = c.getString(c.getColumnIndex(MessageThreadData.C_FRIEND_NAME));

					MessageThread message = new MessageThread();
					message.setId(id);
					message.setSubject(subject);
					message.setRecipients(recipients);
					message.setSnippet(snippet);
					message.setUnread(unread);
					message.setUpdatedTime(updatedTime);
					message.setFriendName(friendName);

					mMessageThreads.add(message);

				} while (c.moveToNext());
			}
		}
		if (c != null) {

			c.close();
		}

		mAdapter.notifyDataSetChanged();
	}

	private void getMessagesFromFB() {
		getActivity().startService(new Intent(getActivity(), MessagesService.class));
	}

	class ItemAdapter extends BaseAdapter {

		private class ViewHolder {

			public ImageView fromPicture;
			public TextView fromName;
			public TextView updatedTime;
			public TextView snippet;
			// public TextView unread;
		}

		@Override
		public int getCount() {
			if (mMessageThreads != null) {
				return mMessageThreads.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return mMessageThreads.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			View view = convertView;
			final ViewHolder holder;

			if (getActivity() != null) {
				final MessageThread thread = mMessageThreads.get(position);
				Logger.i("mMessageThreads in Adapter: " + thread.toString());

				if (convertView == null) {
					view = getActivity().getLayoutInflater().inflate(R.layout.message_thread_list_item, null);

					holder = new ViewHolder();
					holder.fromPicture = (ImageView) view.findViewById(R.id.fromPicture);

					holder.fromName = (TextView) view.findViewById(R.id.fromName);
					configFromText(holder.fromName);

					holder.updatedTime = (TextView) view.findViewById(R.id.createdTime);
					configTimeText(holder.updatedTime);
					holder.snippet = (TextView) view.findViewById(R.id.snippet);
					configText(holder.snippet);
					view.setTag(holder);
				} else
					holder = (ViewHolder) view.getTag();

				holder.fromName.setText(thread.getFriendName());

				String updatedTime = (String) FacebookUtils.convertUnixTimeStampToRelativeTime(thread.getUpdatedTime(), getActivity());

				holder.updatedTime.setText(updatedTime);
				Logger.i("thread.getSnippet: " + thread.getSnippet());
				holder.snippet.setText(thread.getSnippet());

				String token = getApplication().getFBConnection().getFacebook().getAccessToken();
				getImageLoader().displayImage("https://graph.facebook.com/" + thread.getFriendId() + "/picture?access_token=" + token, holder.fromPicture, getImageDisplayOptions());

				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String jabberId = FacebookUtils.getJabberIdFromUserId(thread.getFriendId());
						Intent intent = new Intent(getActivity(), ChatConversationActivity.class);
						intent.putExtra(Constants.CHAT_USER_JABBER_ID_KEY, jabberId);
						intent.putExtra(Constants.CHAT_USER_NAME_KEY, thread.getFriendName());
						intent.putExtra(Constants.CLEAR_TOP_ON_HOME_SELECTED, true);
						getActivity().startActivity(intent);
						getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
					}
				});

			}
			return view;
		}
	}

}