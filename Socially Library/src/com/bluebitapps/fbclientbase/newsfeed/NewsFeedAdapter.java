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

package com.bluebitapps.fbclientbase.newsfeed;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluebitapps.utils.FacebookUtils;
import com.bluebitapps.utils.StringUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.actions.LikeService;
import com.bluebitapps.fbclientbase.debug.Logger;

public class NewsFeedAdapter extends BaseAdapter {

	private static final int TYPE_ITEM_BASE = 0;
	private static final int TYPE_ITEM_PHOTO = 1;
	private static final int TYPE_ITEM_STATUS = 2;
	private static final int TYPE_MAX_COUNT = TYPE_ITEM_STATUS + 1;

	private Context mContext;
	private List<NewsFeedItem> mNewsFeedItems;
	private ImageLoader mImageLoader;
	private DisplayImageOptions mOptions;
	
	private class ViewHolder {
		public TextView fromName;
		public TextView createdTime;
		public TextView message;
		public TextView name;
		public TextView description;
		public ImageView picture;
		public ImageView fromPicture;
		//public ProgressBar progressBar;
		public ViewGroup storyBox;
		//public ViewGroup actions;
		public Button likeButton;
		public Button commentsButton;
		public TextView likesCount;
		public TextView commentsCount;
	}

	public NewsFeedAdapter(Context context, List<NewsFeedItem> newsFeedItems) {
		this.mContext = context;
		mNewsFeedItems = newsFeedItems;
	}

	@Override
	public int getItemViewType(int position) {

		String type = mNewsFeedItems.get(position).getType();

		if (type.equalsIgnoreCase(NewsFeedItem.NEWSFEED_ITEM_TYPE_PHOTO)) {
			return TYPE_ITEM_PHOTO;
		} else if (type.equalsIgnoreCase(NewsFeedItem.NEWSFEED_ITEM_TYPE_STATUS)) {
			return TYPE_ITEM_STATUS;
		} else {
			return TYPE_ITEM_BASE;
		}
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_MAX_COUNT;
	}

	@Override
	public int getCount() {
		return mNewsFeedItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mNewsFeedItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		int type = getItemViewType(position);

		NewsFeedItem item = mNewsFeedItems.get(position);
		View view = convertView;
		
		switch (type) {

		case TYPE_ITEM_PHOTO:
			return getPhotoView(position, convertView, parent, item, view);

		case TYPE_ITEM_STATUS:
			return getStatusView(position, convertView, parent, item, view);

		default:
			return getLinkView(position, convertView, parent, item, view);
		}
	}

	private void setFromHeaderReferences(View view, ViewHolder holder) {
		holder.fromName = (TextView) view.findViewById(R.id.fromName);
		holder.createdTime = (TextView) view.findViewById(R.id.createdTime);
		
		holder.name = (TextView) view.findViewById(R.id.newsFeedItemName);
		holder.fromPicture = (ImageView) view.findViewById(R.id.fromPicture);
	}

	private void setFromHeaderValues(NewsFeedItem item, ViewHolder holder) {
		holder.fromName.setText(Html.fromHtml(FacebookUtils.getFromStringForNewsFeedItem(item)));			
		holder.createdTime.setText(Html.fromHtml(FacebookUtils.getCreatedTimeInNewsFeed(item)));
		getImageLoader().displayImage(item.getProfilePicture(), holder.fromPicture, getImageDisplayOptions());
	}

	private void setActionFooterReferences(View view, ViewHolder holder) {
		//holder.actions = (ViewGroup) view.findViewById(R.id.actionsFooter);
		holder.likeButton = (Button) view.findViewById(R.id.likeButton);
		holder.commentsButton = (Button)view.findViewById(R.id.commentButton);
		holder.likesCount = (TextView) view.findViewById(R.id.likeCount);
		holder.commentsCount = (TextView) view.findViewById(R.id.commentCount);
	}

	private void setActionFooterValues(final NewsFeedItem item, final ViewHolder holder, final int position) {
		holder.likesCount.setText(item.getLikesCount());
		holder.commentsCount.setText(item.getCommentsCount());

		if (StringUtil.notEmpty(item.getUserLikes()) && item.getUserLikes().equalsIgnoreCase(Constants.TRUE)) {
			holder.likeButton.setText("Unlike");
		} else {
			holder.likeButton.setText("Like");
		}

		holder.likeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(mContext, LikeService.class);
				intent.putExtra(Constants.LIKE_OBJECT_ID_KEY, item.getId());

				int count;
				if (StringUtil.notEmpty(item.getLikesCount())) {
					count = Integer.decode(item.getLikesCount());
				} else {
					count = 0;
				}

				if (item.getUserLikes().equalsIgnoreCase(Constants.TRUE)) {
					holder.likeButton.setText("Like");
					Logger.i("Like btn clicked: User used to like it, now he doesn't and btn should say Like");
					intent.putExtra(Constants.ACTION_TYPE, Constants.ACTION_TYPE_UNLIKE);
					item.setUserLikes(Constants.FALSE);
					count--;
					holder.likesCount.setText(Integer.toString(count));
				} else {
					holder.likeButton.setText("Unlike");
					intent.putExtra(Constants.ACTION_TYPE, Constants.ACTION_TYPE_LIKE);
					item.setUserLikes(Constants.TRUE);
					Logger.i("like btn clicked: User didn't like it, now he does and btn should say Unlike");
					count++;
					holder.likesCount.setText(Integer.toString(count));
				}

				mContext.startService(intent);
			}
		});
		
		holder.commentsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//mListener.onNewsFeedTapped(mNewsFeedItems.get(position));
				
			}
		});
	}

	private View getLinkView(final int position, View convertView, ViewGroup parent, NewsFeedItem item, View view) {
		final ViewHolder holder;
		// Set references
		if (convertView == null) {

			view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.newsfeed_item, null);

			holder = new ViewHolder();

			setFromHeaderReferences(view, holder);
			holder.message = (TextView) view.findViewById(R.id.newsFeedItemMessage);
			holder.description = (TextView) view.findViewById(R.id.newsFeedItemDescription);
			holder.picture = (ImageView) view.findViewById(R.id.newsFeedItemPicture);
			holder.storyBox = (ViewGroup) view.findViewById(R.id.newsFeedItemPictureAndName);
			setActionFooterReferences(view, holder);

			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		// Set values
		setFromHeaderValues(item, holder);
		holder.message.setText(item.getMessage());
		holder.name.setText(item.getName());
		holder.description.setText(item.getDescription());
		getImageLoader().displayImage(item.getPicture(), holder.picture, getImageDisplayOptions());
		setActionFooterValues(item, holder, position);

		Linkify.addLinks(holder.name, Linkify.ALL);
		Linkify.addLinks(holder.message, Linkify.ALL);
		Linkify.addLinks(holder.description, Linkify.ALL);

		final String url = item.getLink();

		holder.storyBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.setData(Uri.parse(url));
				//startActivity(intent);
			}
		});

		return view;

	}

	private View getStatusView(final int position, View convertView, ViewGroup parent, NewsFeedItem item, View view) {
		final ViewHolder holder;
		// Set references
		if (convertView == null) {

			view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.newsfeeditem_status, null);
			holder = new ViewHolder();
			setFromHeaderReferences(view, holder);
			holder.message = (TextView) view.findViewById(R.id.newsFeedItemMessage);
			setActionFooterReferences(view, holder);
			view.setTag(holder);
		} else
			holder = (ViewHolder) view.getTag();

		// Set values
		setFromHeaderValues(item, holder);
		holder.message.setText(item.getMessage());
		setActionFooterValues(item, holder, position);

		Linkify.addLinks(holder.message, Linkify.ALL);

		return view;
	}

	private View getPhotoView(final int position, View convertView, ViewGroup parent, NewsFeedItem item, View view) {
		final ViewHolder holder;

		// Set references
		if (convertView == null) {
			view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.newsfeeditem_photo, null);
			holder = new ViewHolder();
			setFromHeaderReferences(view, holder);
			holder.message = (TextView) view.findViewById(R.id.newsFeedItemMessage);
			holder.picture = (ImageView) view.findViewById(R.id.newsFeedItemPicture);
			setActionFooterReferences(view, holder);
			view.setTag(holder);
		} else
			holder = (ViewHolder) view.getTag();

		// Set values
		setFromHeaderValues(item, holder);
		setActionFooterValues(item, holder, position);
		holder.message.setText(item.getMessage());
		String token = FBClientApplication.getApplication().getFBConnection().getFacebook().getAccessToken();
		getImageLoader().displayImage("https://graph.facebook.com/" + item.getObjectId() + "/picture&small?access_token=" + token, holder.picture, getImageDisplayOptions());

		Linkify.addLinks(holder.message, Linkify.ALL);

		return view;
	}
	
	public ImageLoader getImageLoader() {
		if (mImageLoader == null) {
			mImageLoader = ImageLoader.getInstance();
			mImageLoader.init(ImageLoaderConfiguration.createDefault(mContext));
		}
		return mImageLoader;
	}

	public DisplayImageOptions getImageDisplayOptions() {
		if (mOptions == null) {
			mOptions = new DisplayImageOptions.Builder().showStubImage(R.drawable.image_background).cacheInMemory().cacheOnDisc().build();
		}
		return mOptions;
	}

}
