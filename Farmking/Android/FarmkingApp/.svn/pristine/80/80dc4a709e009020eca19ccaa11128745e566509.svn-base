package com.matescorp.soyu.farmkingapp.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.matescorp.soyu.farmkingapp.MainActivity;
import com.matescorp.soyu.farmkingapp.R;

/**
 * Created by jin-won on 2018. 1. 23..
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFMS";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived / From : " + remoteMessage.getFrom());

        // Check if message contains a data payload
        // Data message - Processed by application
        if (remoteMessage.getData().size() > 0){
            Log.d(TAG, "Message data payload : " + remoteMessage.getData());
            sendNotification(remoteMessage.getData().get("message"));
        }

        // Check if message contains a notification payload
        // Notification message - Processed by Operation System
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody());
        }


    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        String channel_id = getString(R.string.default_notification_channel_id);
        Log.d(TAG, "String message = " + message);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        /**
         * NotificationCompat.Builder(Context) is deprecated in API level 26.
         * Use Notification.Builder(Context, String) instead.
         * All posted Notifications must specify a NotificationChannel Id.
         */
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channel_id)
                .setSmallIcon(R.mipmap.noti_farmking)
                .setContentTitle("FarmKing Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            if (notificationManager != null)
                notificationManager.notify(0, notificationBuilder.build());
        } catch (NullPointerException ne) {
            ne.printStackTrace();
        }
    }
}
