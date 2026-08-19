/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bluebitapps.fbclientbase.broadcastreceivers.BootBroadcastReceiver;
import com.bluebitapps.fbclientbase.debug.Logger;

public class NotificationAlarm {

	Context mContext;

	public NotificationAlarm(Context context) {
		mContext = context;
	}

	public void start() {

		Log.i("feb28", Logger.getClassAndMethod());

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		String key;

		try {
			key = mContext.getResources().getString(com.bluebitapps.fbclientbase.R.string.PREFS_NOTIFICATION_FREQUENCY_KEY);

		} catch (NotFoundException e) {
			return;
		}

		long interval = 60000 * Long.parseLong(prefs.getString(key, "-1"));
		long now = System.currentTimeMillis();

		Log.i("feb28", "interval: " + interval);

		Intent intent = new Intent(mContext, NotificationsService.class);
		int requestCode = 0;// no requestcode
		int flags = 0;// no flags
		PendingIntent pendingIntent = PendingIntent.getService(mContext, requestCode, intent, flags);

		if (interval < 0) {

			try {
				AlarmManager alarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
				alarm.cancel(pendingIntent);
				Log.i("feb28", "alarm.cancel() called");
			} catch (Exception e) {
				return;
			}

			return;
		}

		AlarmManager alarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, (now + interval), interval, pendingIntent);
		Log.i("feb28", BootBroadcastReceiver.class.getSimpleName() + "#onReceive" + "Alarm set to get notifications every " + (interval / 60000) + " minutes");
	}

}
