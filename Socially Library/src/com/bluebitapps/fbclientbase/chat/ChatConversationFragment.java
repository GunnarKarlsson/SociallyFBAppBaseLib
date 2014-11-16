/*******************************************************************************
 * Copyright 2012 Gunnar Karlsson.
 *******************************************************************************/

package com.bluebitapps.fbclientbase.chat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bluebitapps.utils.FacebookUtils;
import com.bluebitapps.utils.InputUtil;
import com.bluebitapps.utils.OutputUtil;
import com.bluebitapps.utils.StringUtil;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.base.BaseFragment;
import com.bluebitapps.fbclientbase.base.BaseNavigationFragment;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.layout.LoadingView;
import com.bluebitapps.fbclientbase.messages.Message;
import com.bluebitapps.fbclientbase.messages.MessageThread;
import com.bluebitapps.fbclientbase.messages.MessageThreadData;
import com.bluebitapps.fbclientbase.theme.ThemeFactory;

public class ChatConversationFragment extends BaseNavigationFragment {

	private static final String TAG = ChatConversationFragment.class
			.getSimpleName();
	private static final String SAVED_INSTANCE_STATE_KEY = "saved instance state key";

	ArrayList<Message> mMessages;

	String mChatUserName;
	String mChatUserJabberId;

	EditText mEditText;
	Button mSendButton;
	String mMessage = "";
	ListView mListView;
	ItemAdapter mAdapter;
	String mFriendId;
	String mCurrentUserId;

	// private ViewSwitcher mPrevMessagesView;
	private int mOffset;
	private int mOffsetIncrement = 16;
	// private int mMessagesCount;
	private boolean hasSavedInstanceState;

	private JSONArray mMessagesJsonArray;
	private JSONArray mNamesJsonArray;

	private LoadingView mLoadingView;

	XMPPConnectionSingleton mConnectionSingleton;

	Chat mNewChat;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mMessages = new ArrayList<Message>();

		if (savedInstanceState != null
				&& savedInstanceState
						.getParcelableArrayList(SAVED_INSTANCE_STATE_KEY) != null) {
			mMessages = savedInstanceState
					.getParcelableArrayList(SAVED_INSTANCE_STATE_KEY);
			hasSavedInstanceState = true;
		} else {
			hasSavedInstanceState = false;
		}

		Bundle bundle = getArguments();

		if (bundle == null) {
			return;
		}

		mChatUserJabberId = bundle.getString(Constants.CHAT_USER_JABBER_ID_KEY);

		Pattern intsOnlyPattern = Pattern.compile("\\d+");
		Matcher match = intsOnlyPattern.matcher(mChatUserJabberId);
		match.find();
		mFriendId = match.group();

		mChatUserName = bundle.getString(Constants.CHAT_USER_NAME_KEY);

		mCurrentUserId = FBClientApplication.getApplication().getFBConnection()
				.getUserId();

		Log.i(TAG, "mChatUserName: " + mChatUserName + " ,mCurrentUserId: "
				+ mCurrentUserId + " ,mFriendId: " + mFriendId);
		setHasOptionsMenu(true);

		mConnectionSingleton = XMPPConnectionSingleton.getInstance();

		mOffset = 0;

		setTitle("Chat");

		prepareRefreshMenuItemAnimation();

		if (mConnectionSingleton != null) {

			XMPPConnection conn = mConnectionSingleton.getConnection();

			if (conn != null) {

				ChatManager chatmanager = conn.getChatManager();
				// Create Chat Manager

				mNewChat = chatmanager.createChat(mChatUserJabberId,
						new MessageListener() {

							@Override
							public void processMessage(
									Chat chat,
									org.jivesoftware.smack.packet.Message message) {

								if (!StringUtil.notEmpty(message.getBody())) {
									return;
								}

								Log.d(TAG,
										"chatPariticipant "
												+ chat.getParticipant()
												+ " said ->"
												+ message.getBody());

								long currentTimeStamp = System
										.currentTimeMillis() / 1000;
								addMessageToList(FacebookUtils
										.getUserIdFromJabberId(chat
												.getParticipant()), Long
										.toString(currentTimeStamp), message
										.getBody(), null);

							}
						});

			} else {
				Log.d(TAG, "connection is null");
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(SAVED_INSTANCE_STATE_KEY, mMessages);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup vg = null;

		if (getActivity() != null) {

			vg = ThemeFactory.getViewGroup(getThemeSelection(), getActivity(),
					inflater, container, R.layout.chat_list_edit_text);

			mSendButton = (Button) vg.findViewById(R.id.postButton);
			mEditText = (EditText) vg.findViewById(R.id.editText);
			mListView = (ListView) vg.findViewById(R.id.list);
			mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

			mLoadingView = (LoadingView) vg.findViewById(R.id.loadingView);

			mSendButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					mMessage = mEditText.getText().toString();

					sendMessage();

				}
			});

			mAdapter = new ItemAdapter();

			mListView.setAdapter(mAdapter);
		}

		return vg;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (hasSavedInstanceState && mListView != null && mAdapter != null) {
			mListView.smoothScrollToPosition(mMessages.size() - 1);
			mAdapter.notifyDataSetChanged();
		} else {
			getMessageHistory();
		}
	}

	private void getMessageHistory() {

		startRefreshMenuItemAnimation();

		Cursor c = FBClientApplication.getApplication().getMessagesData()
				.getThreadFromFriendId(mFriendId);

		// Cursor c = ((FBClientApplication)
		// getActivity().getApplication()).getMessagesData().getMessages();

		String threadId = "";
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					threadId = c.getString(c
							.getColumnIndex(MessageThreadData.C_ID));
				} while (c.moveToNext());
			}
		}
		if (c != null) {
			c.close();
		}

		Log.i(TAG, Logger.getClassAndMethod() + "threadid: " + threadId);

		if (StringUtil.notEmpty(threadId)) {
			String query = threadId + "/comments";
			((FBClientApplication) getApplication()).getFBConnection()
					.getAsyncFacebookRunner()
					.request(query, new MessageHistoryListener());
		} else {
			if (getActivity() != null) {
				OutputUtil.showCrouton(
						getActivity(),
						getActivity().getResources().getString(
								R.string.error_retrieving_message_history));
				Log.i(TAG, Logger.getClassAndMethod() + "threadId is null");
				stopRefreshMenuItemAnimation();
			}
		}
	}

	private class MessageHistoryListener implements RequestListener {

		@Override
		public void onComplete(String response, Object state) {

			Log.i(TAG, "response: " + response.toString());

			final ArrayList<Message> messages = new ArrayList<Message>();
			try {

				JSONObject dataJsonObject = new JSONObject(response);
				final JSONArray jsonArray = dataJsonObject.getJSONArray("data");

				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {

						JSONObject obj = jsonArray.getJSONObject(i);

						// add top item to list

						Log.d(TAG, obj.toString());

						if (obj.has("from")) {

							Message messageAtTop = new Message();

							JSONObject fromTop = obj.getJSONObject("from");

							String fromNameTop = fromTop.getString("name");
							messageAtTop.setFromName(fromNameTop);
							String fromNameIdTop = fromTop.getString("id");
							messageAtTop.setFromId(fromNameIdTop);

							if (obj.has("message")) {
								String messageTop = obj.getString("message");
								messageAtTop.setMessageText(messageTop);
							}

							if (obj.has("created_time")) {
								String updatedTimeTop = obj
										.getString("created_time");
								messageAtTop.setCreatedTime(updatedTimeTop);
							}

							if (obj.has("unread")) {
								String unreadTop = obj.getString("unread");
								// TODO set unread
							}

							messages.add(messageAtTop);
						}

						// add 'comments' to list
						/*
						if (obj.has("comments")) {

							JSONObject comments = obj.getJSONObject("comments");
							JSONArray data = comments.getJSONArray("data");
							for (int j = 0; j < data.length(); j++) {
								JSONObject item = data.getJSONObject(j);

								if (item.has("from")) {

									Message message = new Message();

									JSONObject from = item
											.getJSONObject("from");

									String fromName = from.getString("name");

									message.setFromName(fromName);
									String fromId = from.getString("id");
									message.setFromId(fromId);

									if (item.has("message")) {
										String body = item.getString("message");
										message.setMessageText(body);
									}

									if (item.has("created_time")) {
										Log.d(TAG, "created_time: " + item.getString("created_time"));
										String updatedTime = item
												.getString("created_time");// 2013-01-14T07:13:27+0000
										message.setCreatedTime(updatedTime);
									}

									messages.add(message);

								}
							}
						}
						*/

					}
				}
			} catch (JSONException e) {
				Log.i(TAG, ChatConversationFragment.class.getSimpleName() + "."
						+ MessageHistoryListener.class.getSimpleName() + "."
						+ e.toString());
				if (getActivity() != null) {
					OutputUtil.showCrouton(
							getActivity(),
							getActivity().getResources().getString(
									R.string.error_retrieving_message_history));
					stopRefreshMenuItemAnimation();
				}
			}

			if (getActivity() != null) {

				getActivity().runOnUiThread(new Runnable() {
					public void run() {

						// Collections.reverse(messages);

						if (mMessages != null) {
							mMessages.clear();
							mMessages.addAll(messages);
						}

						mOffset += mOffsetIncrement;

						if (mAdapter != null) {
							mAdapter.notifyDataSetChanged();
						}

						if (mListView != null) {
							mListView.smoothScrollToPosition(mMessages.size() - 1);
						}

						if (mLoadingView != null) {
							mLoadingView.setVisibility(View.GONE);
						}

						stopRefreshMenuItemAnimation();

					}
				});
			}

		}

		@Override
		public void onIOException(IOException e, Object state) {
			Logger.i(ChatConversationFragment.class.getSimpleName() + "."
					+ MessageHistoryListener.class.getSimpleName() + "."
					+ e.toString());
			if (getActivity() != null) {
				OutputUtil
						.showCrouton(
								getActivity(),
								getActivity()
										.getResources()
										.getString(
												R.string.message_history_could_not_be_retrieved));
				stopRefreshMenuItemAnimation();
			}

		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			Logger.i(ChatConversationFragment.class.getSimpleName() + "."
					+ MessageHistoryListener.class.getSimpleName() + "."
					+ e.toString());
			if (getActivity() != null) {
				OutputUtil
						.showCrouton(
								getActivity(),
								getActivity()
										.getResources()
										.getString(
												R.string.message_history_could_not_be_retrieved));
				stopRefreshMenuItemAnimation();
			}
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			Logger.i(ChatConversationFragment.class.getSimpleName() + "."
					+ MessageHistoryListener.class.getSimpleName() + "."
					+ e.toString());
			if (getActivity() != null) {
				OutputUtil
						.showCrouton(
								getActivity(),
								getActivity()
										.getResources()
										.getString(
												R.string.message_history_could_not_be_retrieved));
				stopRefreshMenuItemAnimation();
			}
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Logger.i(ChatConversationFragment.class.getSimpleName() + "."
					+ MessageHistoryListener.class.getSimpleName() + "."
					+ e.toString());
			if (getActivity() != null) {
				OutputUtil
						.showCrouton(
								getActivity(),
								getActivity()
										.getResources()
										.getString(
												R.string.message_history_could_not_be_retrieved));
				stopRefreshMenuItemAnimation();
			}
		}
	}

	@Override
	public void onPause() {
		// TODO detach chat listeners
		super.onPause();
	}

	private void sendMessage() {

		InputUtil.hideKeyboard(getActivity());

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {

				try {

					if (!StringUtil.notEmpty(mMessage)) {
						if (getActivity() != null) {
							OutputUtil.showCrouton(
									getActivity(),
									getActivity().getResources().getString(
											R.string.enter_a_message));
							return;
						}
					}

					org.jivesoftware.smack.packet.Message newMessage = new org.jivesoftware.smack.packet.Message();
					newMessage.setBody(mMessage);
					newMessage.setProperty("itemKey", "itemValue");
					mNewChat.sendMessage(newMessage);

					String fromName = FBClientApplication.getApplication()
							.getFBConnection().getUserName();
					String fromId = FBClientApplication.getApplication()
							.getFBConnection().getUserId();
					long currentTimeStamp = System.currentTimeMillis() / 1000;
					addMessageToList(fromName, Long.toString(currentTimeStamp),
							mMessage, fromId);

				} catch (XMPPException e) {
					Logger.i(ChatConversationFragment.class.getSimpleName()
							+ "."
							+ MessageHistoryListener.class.getSimpleName()
							+ "." + e.toString());
					if (getActivity() != null) {
						OutputUtil
								.showCrouton(
										getActivity(),
										getActivity()
												.getResources()
												.getString(
														R.string.message_could_not_be_delivered));
					}
				} catch (IllegalStateException e) {
					Logger.i(ChatConversationFragment.class.getSimpleName()
							+ "."
							+ MessageHistoryListener.class.getSimpleName()
							+ "." + e.toString());
					if (getActivity() != null) {
						OutputUtil
								.showCrouton(
										getActivity(),
										getActivity()
												.getResources()
												.getString(
														R.string.message_could_not_be_delivered));
					}
				} catch (Exception e) {
					Logger.i(ChatConversationFragment.class.getSimpleName()
							+ "."
							+ MessageHistoryListener.class.getSimpleName()
							+ "." + e.toString());
					if (getActivity() != null) {
						OutputUtil
								.showCrouton(
										getActivity(),
										getActivity()
												.getResources()
												.getString(
														R.string.message_could_not_be_delivered));
					}
				}

			}
		});

		thread.start();
	}

	class ItemAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView name;
			public ImageView picture;
			public TextView createdTime;
			public TextView message;
		}

		@Override
		public int getCount() {
			if (mMessages != null) {
				return mMessages.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			return mMessages.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			View view = convertView;
			final ViewHolder holder;
			if (convertView == null && getActivity() != null) {
				view = getActivity().getLayoutInflater().inflate(
						R.layout.chat_message_item, null);

				holder = new ViewHolder();
				holder.name = (TextView) view.findViewById(R.id.fromName);
				configFromText(holder.name);
				holder.picture = (ImageView) view
						.findViewById(R.id.fromPicture);
				holder.createdTime = (TextView) view
						.findViewById(R.id.createdTime);
				configTimeText(holder.createdTime);
				holder.message = (TextView) view.findViewById(R.id.message);
				configBodyText(holder.message);

				view.setTag(holder);
			} else
				holder = (ViewHolder) view.getTag();

			if (mMessages != null) {

				holder.name.setText(mMessages.get(position).getFromName());

				String token = getApplication().getFBConnection().getFacebook()
						.getAccessToken();
				Logger.i("mMessages.get(position).getIdFromJabberId()"
						+ mMessages.get(position).getIdFromJabberId());
				String query = "https://graph.facebook.com/"
						+ mMessages.get(position).getFromId()
						+ "/picture?access_token=" + token;
				getImageLoader().displayImage(query, holder.picture,
						getImageDisplayOptions());
				
				String createdTime= mMessages.get(position).getCreatedTime();
				if (createdTime != null) {
					Log.d(TAG, "createdTime: " + createdTime);
				}
				if (createdTime != null && createdTime.contains(":")) {
					if (getActivity() != null) {
						String convertedTime = FacebookUtils.convertFacebookCreatedTimeToRelativeTime(createdTime, getActivity()).toString();
						holder.createdTime.setText(convertedTime);						
					}
				}
				/*
				 * if (mMessages.get(position).getCreatedTime().contains(":")) {
				 * holder.createdTime.setText(FacebookUtils
				 * .convertFacebookCreatedTimeToRelativeTime(mMessages
				 * .get(position).getCreatedTime(), getActivity()));
				 * 
				 * }
				 */

				holder.message
						.setText(mMessages.get(position).getMessageText());

				configText(holder.message);

			}

			return view;
		}
	}

	private void addMessageToList(final String from, final String time,
			final String body, final String fromId) {

		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Message msg = new Message();

				if (StringUtil.notEmpty(from)) {
					msg.setFromName(from);
				}

				if (StringUtil.notEmpty(fromId)) {
					msg.setFromId(fromId);
				}

				if (StringUtil.notEmpty(time)) {
					msg.setCreatedTime(time);
				}

				if (StringUtil.notEmpty(body)) {
					msg.setMessageText(body);
				}

				if (mMessages != null) {
					mMessages.add(msg);
				}

				if (mListView != null) {
					mListView.smoothScrollToPosition(mMessages.size() - 1);
				}

				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}

			}
		});
	}
}