/**
 * Copyright 2012 Gunnar Karlsson.
 */

package com.bluebitapps.fbclientbase.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bluebitapps.fbclientbase.debug.DebugConfig;
import com.bluebitapps.fbclientbase.debug.Logger;
import com.bluebitapps.fbclientbase.notifications.NotificationsService;
import com.bluebitapps.utils.NetworkUtil;

public class NetworkBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (!DebugConfig.isNetworkBroadcastReceiverEnabled)
			return;

		if (NetworkUtil.isConnectedToInternet(context)) {

			Logger.i(NetworkBroadcastReceiver.class.getSimpleName() + "#onReceive" + " isConnectedToInternet =" + NetworkUtil.isConnectedToInternet(context) + ": start NotificationsService");

			context.startService(new Intent(context, NotificationsService.class));

		} else {

			Logger.i(NetworkBroadcastReceiver.class.getSimpleName() + "#onReceive" + " isConnectedtoInternet =" + NetworkUtil.isConnectedToInternet(context) + ": stop NotificationsService");

			context.stopService(new Intent(context, NotificationsService.class));
		}
	}

}
