/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluebitapps.fbclientbase.Constants;
import com.bluebitapps.fbclientbase.FBClientApplication;
import com.bluebitapps.fbclientbase.R;
import com.bluebitapps.fbclientbase.actions.LikeService;
import com.bluebitapps.fbclientbase.actions.Likeable;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.newsfeed.NewsFeedItem;
import com.bluebitapps.fbclientbase.newsfeed.NewsFeedItemActivity;

public class FacebookUtils {

	public static final String LIKE_CONTEXT_NEWSFEED_LIST = "like context newsfeed list";
	public static final String LIKE_CONTEXT_SINGLE_NEWSFEED_ITEM = "like context single newsfeed item";
	public static final String LIKE_CONTEXT_COMMENT = "like context comment";

	private FacebookUtils() {
	}

	/**
	 * @param createdTime
	 * @return relative time formatted {@link CharSequence};
	 */

	public static String getJabberIdFromUserId(String userId) {
		String str = "-" + userId + "@chat.facebook.com";
		return str;
	}

	public static String getUserIdFromJabberId(String jabberId) {
		Pattern intsOnlyPattern = Pattern.compile("\\d+");
		Matcher match = intsOnlyPattern.matcher(jabberId);
		match.find();
		return match.group();
	}

	public static void setLikeButtonState(Button likeButton, Likeable item, Context context) {

		if (context == null) {
			return;
		}

		if (StringUtil.notEmpty(item.getUserLikes()) && item.getUserLikes().equalsIgnoreCase(Constants.TRUE)) {
			likeButton.setText(context.getResources().getString(R.string.unlike_on_button));
		} else {
			likeButton.setText(context.getResources().getString(R.string.like_on_button));
		}
	}

	public static void handleLikeButtonClick(Context context, Likeable item, Button likeButton, TextView likesCount, View view, String type) {

		if (context == null || item == null || likeButton == null || likesCount == null) {
			return;
		}

		Intent intent = new Intent(context, LikeService.class);
		// Logger.i(FacebookUtils.class.getSimpleName() +
		// Logger.getMethodName(2) + "item.getId(): "+item.getId());
		intent.putExtra(Constants.LIKE_OBJECT_ID_KEY, item.getId());

		int count = 0;
		if (StringUtil.notEmpty(item.getLikesCount())) {
			count = Integer.decode(item.getLikesCount());
		} else {
			count = 0;
		}

		if (item.getUserLikes() != null && item.getUserLikes().equalsIgnoreCase(Constants.TRUE)) {
			likeButton.setText(context.getResources().getString(R.string.like_on_button));
			// Logger.i(FacebookUtils.class.getSimpleName() +
			// "handleLikeButtonClick()" +
			// "Like btn clicked: User used to like it, now he doesn't and btn should say Like");
			intent.putExtra(Constants.ACTION_TYPE, Constants.ACTION_TYPE_UNLIKE);
			item.setUserLikes(Constants.FALSE);
			if (count > 0) {
				count--;
			} else {
				count = 0;
			}
			likesCount.setText(Integer.toString(count));
		} else {
			likeButton.setText(context.getResources().getString(R.string.unlike_on_button));
			intent.putExtra(Constants.ACTION_TYPE, Constants.ACTION_TYPE_LIKE);
			item.setUserLikes(Constants.TRUE);
			// Logger.i(FacebookUtils.class.getSimpleName() +
			// "handleLikeButtonClick()" +
			// "like btn clicked: User didn't like it, now he does and btn should say Unlike");
			count++;
			item.setLikesCount(Integer.toString(count));
			likesCount.setText(Integer.toString(count));
		}

		item.setLikesCount(Integer.toString(count));
		context.startService(intent);

		if (LIKE_CONTEXT_SINGLE_NEWSFEED_ITEM.equals(type)) {
			ImageView likeIcon = (ImageView) view.findViewById(R.id.likeIcon);
			final TextView likeCount = (TextView) view.findViewById(R.id.likeCount);
			if (item.getLikesCount() != null && Integer.parseInt(item.getLikesCount()) > 0) {
				likeIcon.setVisibility(View.VISIBLE);
				likeCount.setVisibility(View.VISIBLE);
				likeCount.setText(item.getLikesCount());
			} else {
				likeIcon.setVisibility(View.GONE);
				likeCount.setVisibility(View.GONE);
			}

			Intent i = new Intent();
			i.putExtra(NewsFeedItemActivity.LIKE_CHANGE, count);

			i.putExtra(NewsFeedItemActivity.LIKE_CHANGE_OBJECT_ID, item.getId());
			i.putExtra(NewsFeedItemActivity.USER_LIKES_BOOLEAN, item.getUserLikes());
			((Activity) context).setResult(NewsFeedItemActivity.RESULT_CODE_LIKES_CHANGE, i);
		}
	}

	public static String getFromStringForNewsFeedItem(NewsFeedItem item) {

		String from;

		if (FBClientApplication.getApplication().isEnglish()) {

			from = "<font><font color=\"#000000\">" + item.getFromName() + "</font>" + "<font><font color=\"#aaaaaa\">";
			if (StringUtil.notEmpty(item.getStatusType()) && item.getStatusType().equalsIgnoreCase(NewsFeedItem.NEWSFEED_ITEM_STATUS_TYPE_SHARED_STORY)) {
				from += " shared a story" + "</font>";
			} else if (item.getStatusType() != null && item.getStatusType().equalsIgnoreCase(NewsFeedItem.NEWSFEED_ITEM_STATUS_TYPE_MOBILE_STATUS_UPDATE)) {
				from += " posted a status update" + "</font>";
			} else if (StringUtil.notEmpty(item.getStatusType()) && item.getStatusType().equalsIgnoreCase(NewsFeedItem.NEWSFEED_ITEM_STATUS_TYPE_ADDED_PHOTOS)) {
				from += " added photos." + "</font>";
			} else if (StringUtil.notEmpty(item.getStatusType()) && item.getStatusType().equalsIgnoreCase(NewsFeedItem.NEWSFEED_ITEM_STATUS_TYPE_TAGGED_IN_PHOTO)) {
				from += " was tagged in a photo." + "</font>";
			} else if (StringUtil.notEmpty(item.getStatusType()) && item.getStatusType().equalsIgnoreCase(NewsFeedItem.NEWSFEED_ITEM_TYPE_CHECKIN)) {
				from += " checked in at a place." + "</font>";
			} else {

				from += "</font>";
			}
			return from;
		} else {
			return from = "<font><font color=\"#000000\">" + item.getFromName() + "</font>" + "<font><font color=\"#aaaaaa\">";
		}
	}

	public static String getCreatedTimeInNewsFeed(NewsFeedItem item, Context context) {
		
		if(context == null){
			return null;
		}
		
		String createdTime = (String) FacebookUtils.convertFacebookCreatedTimeToRelativeTime(item.getCreatedTime(), context);
		return createdTime;
	}

	public static CharSequence convertFacebookCreatedTimeToRelativeTime(String createdTime, Context context) {

		if(context == null){
			return null;
		}
		
		if (!StringUtil.notEmpty(createdTime)) {
			return "";
		}

		long longDate;
		SimpleDateFormat formatter;
		Date date;
		formatter = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss+SSSS");
		TimeZone gmtTime = TimeZone.getTimeZone("GMT");
		formatter.setTimeZone(gmtTime);

		try {
			date = (Date) formatter.parse(createdTime);
			longDate = date.getTime();

			return DateUtils.getRelativeTimeSpanString(longDate);// date.toString();//relativeTime;
		} catch (ParseException e) {
			return context.getResources().getString(R.string.a_while_ago);
		}

	}

	public static CharSequence convertFacebookEventTimeToRelativeTime(String time, Context context) {

		if(context==null){
			return null;
		}
		
		if (!StringUtil.notEmpty(time)) {
			return context.getResources().getString(R.string.a_while_ago);
		}

		try {

			SimpleDateFormat sdfSource = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss", context.getResources().getConfiguration().locale);
			Date date = sdfSource.parse(time);
			SimpleDateFormat sdfDestination = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", context.getResources().getConfiguration().locale);
			return sdfDestination.format(date).toString();

		} catch (ParseException e) {
			return context.getResources().getString(R.string.a_while_ago);
		}

	}

	public static String convertInvitedToEventTimeStamp(String timestamp) {
		int stamp = Integer.parseInt(timestamp);
		java.util.Date time = new java.util.Date((long) stamp * 1000);
		return time.toString();
	}

	public static CharSequence convertUnixTimeStampToRelativeTime(String timeStamp, Context context) {

		if(context==null){
			return null;
		}
		
		// Logger.i(FacebookUtils.class.getSimpleName() +
		// "convertUnixTimeStampToRelativeTime()" + "in: " + timeStamp);

		if (!StringUtil.notEmpty(timeStamp)) {
			return context.getResources().getString(R.string.a_while_ago);
		} else {
			Date time = new java.util.Date(Long.parseLong(timeStamp) * 1000);
			long longDate = time.getTime();
			return DateUtils.getRelativeTimeSpanString(longDate);
		}
	}

	public static long convertFacebookTimeToUnixTimeStamp(String createdTime) {

		if (!StringUtil.notEmpty(createdTime)) {
			return 0;
		}

		long timeMillis;

		try {
			SimpleDateFormat date = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss+SSSS");
			date.setTimeZone(TimeZone.getTimeZone("GMT"));

			timeMillis = date.parse(createdTime).getTime();
		} catch (Exception e) {
			timeMillis = -1;
		}

		return timeMillis;
	}

}