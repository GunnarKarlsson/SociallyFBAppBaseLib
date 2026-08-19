/**
* Copyright 2012 Gunnar Karlsson.
*/

package com.bluebitapps.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import android.os.Environment;

import com.bluebitapps.fbclientbase.debug.Logger;

public class DataBaseCopyUtil {
	
	public static void doCopy(){
		try {
			File data = Environment.getDataDirectory();
	        File sd = Environment.getExternalStorageDirectory();

	        if (sd.canWrite()) {
	            String currentDBPath = "/data/com.bluebitapps.fbclientbase/databases/messages.db";
	            String backupDBPath = "messages.db";
	            File currentDB = new File(data, currentDBPath);
	            File backupDB = new File(sd, backupDBPath);

	            if (currentDB.exists()) {
	  	  				Logger.i(DataBaseCopyUtil.class.getSimpleName() + "#doCopy(): currentDB.exists == true");
	                FileChannel src = new FileInputStream(currentDB).getChannel();
	                FileChannel dst = new FileOutputStream(backupDB).getChannel();
	                dst.transferFrom(src, 0, src.size());
	                src.close();
	                dst.close();
	            }
	        }
	        
				Logger.i(DataBaseCopyUtil.class.getSimpleName() + "#doCopy(): finished");
	        
	    } catch (Exception e) {
				Logger.i(DataBaseCopyUtil.class.getSimpleName() + "#doCopy() " + e.toString());
	    }
	}
}
