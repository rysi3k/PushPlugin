package com.plugin.gcm;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gcm.GCMBroadcastReceiver;
import static com.google.android.gcm.GCMConstants.DEFAULT_INTENT_SERVICE_CLASS_NAME;

/*
 * Implementation of GCMBroadcastReceiver that hard-wires the intent service to be 
 * com.plugin.gcm.GCMIntentService, instead of your_package.GCMIntentService 
 */
public class CordovaGCMBroadcastReceiver extends GCMBroadcastReceiver {
	
	public static List<String> receivedMsgs;

	@Override
	protected String getGCMIntentServiceClassName(Context context) {
    	return "com.plugin.gcm" + DEFAULT_INTENT_SERVICE_CLASS_NAME;
    }
	
}