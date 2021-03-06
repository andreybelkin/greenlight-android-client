package com.globalgrupp.greenlight.androidclient.service;

/**
 * Created by Lenovo on 19.01.2016.
 */

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import com.globalgrupp.greenlight.androidclient.R;
import com.globalgrupp.greenlight.androidclient.controller.EventDetailsActivity;
import com.globalgrupp.greenlight.androidclient.controller.EventListActivity;
import com.globalgrupp.greenlight.androidclient.util.GCMRegistrationHelper;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GCMNotificationIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GCMNotificationIntentService() {
        super("GcmIntentService");
    }

    public static final String TAG = "GCMNotificationInten";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
                    .equals(messageType)) {
//                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
                    .equals(messageType)) {
//                sendNotification("Deleted messages on server: "
//                        + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
                    .equals(messageType)) {
                for (int i = 0; i < 3; i++) {
                    Log.i(TAG,
                            "Working... " + (i + 1) + "/5 @ "
                                    + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());

                try {
                    //todo fields from push notification
                    String senderId = extras.get("senderId") != null ? extras.get("senderId").toString() : "";
                    sendNotification("Новое событие: "
                            + extras.get("message"), new Long(extras.get("eventId").toString()), senderId);
                    Log.i(TAG, "Received: " + extras.toString());
                } catch (Exception e) {

                    e.printStackTrace();
                }

            }
        }

        com.globalgrupp.greenlight.androidclient.service.GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg, Long id, String senderId) {
        Log.d(TAG, "Preparing to send notification...: " + msg);
        String registrationId = GCMRegistrationHelper.getRegistrationId(getApplicationContext());
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                EventListActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        String gettedIds = prefs.getString("eventIdsFromPush", "");
        List<String> oldId = new ArrayList<String>(Arrays.asList(gettedIds.split(",")));
        if (oldId.contains(id.toString())) {
            return;
        } else {
            oldId.add(id.toString());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("eventIdsFromPush", TextUtils.join(",", oldId));
            editor.commit();
        }

        if (!senderId.equals(registrationId)) {
            mNotificationManager = (NotificationManager) this
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            Intent newEventIntent = new Intent(this, EventDetailsActivity.class);
            newEventIntent.putExtra("eventId", id);
//        newEventIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            newEventIntent.setAction(Long.toString(System.currentTimeMillis()));


            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    newEventIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    this).setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("Greenlight")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                    .setContentText(msg).setAutoCancel(true)
                    .setSound(notification);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            Log.d(TAG, "Notification sent successfully.");
        }


        Intent intent = new Intent("newEventBroadCast");
        //put whatever data you want to send, if any
        intent.putExtra("eventId", id);
        //send broadcast
        sendBroadcast(intent);

    }

}
