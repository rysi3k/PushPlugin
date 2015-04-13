package com.plugin.gcm;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.plugin.gcm.CordovaGCMBroadcastReceiver;

import com.google.android.gcm.GCMBaseIntentService;

@SuppressLint("NewApi")
public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";
	
	public GCMIntentService() {
		super("GCMIntentService");
	}

	@Override
	public void onRegistered(Context context, String regId) {

		Log.v(TAG, "onRegistered: "+ regId);

		JSONObject json;

		try
		{
			json = new JSONObject().put("event", "registered");
			json.put("regid", regId);

			Log.v(TAG, "onRegistered: " + json.toString());

			// Send this JSON data to the JavaScript application above EVENT should be set to the msg type
			// In this case this is the registration ID
			PushPlugin.sendJavascript( json );

		}
		catch( JSONException e)
		{
			// No message to the user is sent, JSON failed
			Log.e(TAG, "onRegistered: JSON exception");
		}
	}

	@Override
	public void onUnregistered(Context context, String regId) {
		Log.d(TAG, "onUnregistered - regId: " + regId);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.d(TAG, "onMessage - context: " + context);

		// Extract the payload from the message
		Bundle extras = intent.getExtras();
		if (extras != null)
		{
			if (CordovaGCMBroadcastReceiver.receivedMsgs == null)
                CordovaGCMBroadcastReceiver.receivedMsgs = new ArrayList<String>();

            List<String> msgs = null;
            if (extras.getString("message") != null && extras.getString("message").length() != 0) {
            	CordovaGCMBroadcastReceiver.receivedMsgs.add(extras.getString("message"));
            	extras.putString("message", concatMsgs(CordovaGCMBroadcastReceiver.receivedMsgs));
            	msgs = CordovaGCMBroadcastReceiver.receivedMsgs;
            }	

            boolean alwaysNotify = (extras.getString("alwaysNotify") != null) && (extras.getString("alwaysNotify").equals("true"));
           	boolean showNotify   = (msgs != null) && (msgs.size()>0) && alwaysNotify && (extras.getString("message") != null);

			// if we are in the foreground, just surface the payload, else post it to the statusbar
           if ((PushPlugin.isInForeground()) && (!showNotify)) {
           		CordovaGCMBroadcastReceiver.receivedMsgs = null; //Discard the received messages
				extras.putBoolean("foreground", true);
                PushPlugin.sendExtras(extras);
			}
			else {
				extras.putBoolean("foreground", PushPlugin.isInForeground());

                // Send a notification if there is a message
            	if ((msgs != null)&&(msgs.size()>0)){
            		String notMsg = msgs.get(msgs.size()-1);
            		createNotification(context, extras, notMsg, msgs.size());
            	}
            }
        }
	}
	
	public String concatMsgs(List<String> list){
		String str = "[";
		for (int i = 0; i < list.size();i++){
			if (i>0)
				str += ",";
			str = str + "\"" + list.get(i) + "\"";
		}
		str += "]";
		return str;
	}

	public void createNotification(Context context, Bundle extras, String notMsg, int msgCnt)
	{
		int notId = 0;
		
		try {
			notId = Integer.parseInt(extras.getString("notId"));
			
		}
		catch(NumberFormatException e) {
			Log.e(TAG, "Number format exception - Error parsing Notification ID: " + e.getMessage());
		}
		catch(Exception e) {
			Log.e(TAG, "Number format exception - Error parsing Notification ID" + e.getMessage());
		}
		
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String appName = getAppName(this);

		Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		notificationIntent.putExtra("pushBundle", extras);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		int defaults = Notification.DEFAULT_ALL;

		if (extras.getString("defaults") != null) {
			try {
				defaults = Integer.parseInt(extras.getString("defaults"));
			} catch (NumberFormatException e) {}
		}
		
		NotificationCompat.Builder mBuilder =
			new NotificationCompat.Builder(context)
				.setDefaults(defaults)
				.setSmallIcon(context.getApplicationInfo().icon)
				.setWhen(System.currentTimeMillis())
				.setContentTitle(extras.getString("title"))
				.setTicker(extras.getString("title"))
				.setContentIntent(contentIntent)
				.setAutoCancel(true);

		if (notMsg != null) {
			mBuilder.setContentText(notMsg);
		} else {
			mBuilder.setContentText("<missing message content>");
		}

		mBuilder.setNumber(msgCnt);
		
	
		mNotificationManager.notify((String) appName, notId, mBuilder.build());
	}
	
	private static String getAppName(Context context)
	{
		CharSequence appName = 
				context
					.getPackageManager()
					.getApplicationLabel(context.getApplicationInfo());
		
		return (String)appName;
	}
	
	@Override
	public void onError(Context context, String errorId) {
		Log.e(TAG, "onError - errorId: " + errorId);
	}

}
