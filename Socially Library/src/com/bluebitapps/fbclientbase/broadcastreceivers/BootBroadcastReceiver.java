/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.notifications.NotificationAlarm;

/**
 * 
 * @author Gunnar Karlsson
 * 
 * 
 * From Android Developers website:
 * "Starting with 3.1 when applications are installed they are in a
 * “stopped” state so they will not be able to run until the user
 * explicitly launches them. Pressing Force Stop will return them to
 * this state. Once the user runs the app for the first time (and does not Force
 * Stop it), everything behaves as before — a reboot will cause
 * BOOT_COMPLETED broadcasts to be received and so on. However, if the
 * user installs the app, until and unless they run the app manually, no
 * broadcasts will be received."
 * 
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		Logger.i(BootBroadcastReceiver.class.getSimpleName() + "#onReceive");
		
		NotificationAlarm alarm = new NotificationAlarm(context);
		alarm.start();
	}
}