package com.my.kiki.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.my.kiki.R;
import com.my.kiki.ui.HomeActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private long[] pattern = {300, 300, 300, 300, 300};

    private NotificationManager notifManager,notifManager1;
    NotificationCompat.Builder builder,builder1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e("FireChatMessagingServic", " onMessageReceived remoteMessage.getNotification() "+remoteMessage.getData().get("title")+" PREF_IS_NOTIFY ");

        Log.i("Notify"," onMessageReceived ");


                sendDefaultNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get(""));


    }


    private void sendDefaultNotification(String messageTitle, String messageBody) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String id = getString(R.string.default_notification_channel_id); // default_channel_id
        final int NOTIFY_ID = 0; // ID of notification
        String title = getString(R.string.default_notification_channel_title); // Default Channel

        Bitmap largerIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher);

        if (notifManager == null) {
            notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

//        Notification notification;
        if (messageTitle != null && !messageTitle.isEmpty()) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = notifManager.getNotificationChannel(id);
                if (mChannel == null) {
                    mChannel = new NotificationChannel(id, title, importance);
                    mChannel.enableVibration(true);
                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    notifManager.createNotificationChannel(mChannel);
                }
                builder = new NotificationCompat.Builder(this, id);

                builder.setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(largerIcon)
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setVibrate(pattern)
                        .setColor(getResources().getColor(R.color.colorAccent))
                        .setLights(Color.BLUE, 1, 1)
                        .setSound(defaultSoundUri)
                        .setContentIntent(createPendingIntent())
                        .setGroupSummary(true)
                        .setTicker(messageBody)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody));
            } else {
                builder = new NotificationCompat.Builder(this);
                builder.setContentTitle(messageTitle)
                        .setContentText(messageBody)                         // required
                        .setSmallIcon(R.drawable.ic_launcher) // required
                        .setLargeIcon(largerIcon)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentIntent(createPendingIntent())
                        .setTicker(messageBody)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody));
            }

            /*notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_notification_stat_default)
                    .setLargeIcon(largerIcon)
                    .setContentTitle(messageTitle)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setVibrate(pattern)
                    .setColor(getResources().getColor(R.color.colorAccent))
                    .setLights(Color.BLUE, 1, 1)
                    .setSound(defaultSoundUri)
                    .setContentIntent(createPendingIntent())
                    .setGroupSummary(true)
                    .build();*/
        } else {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = notifManager.getNotificationChannel(id);
                if (mChannel == null) {
                    mChannel = new NotificationChannel(id, "Default Notification", importance);
                    mChannel.enableVibration(true);
                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    notifManager.createNotificationChannel(mChannel);
                }
                builder = new NotificationCompat.Builder(this, id);

                builder.setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(largerIcon)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setVibrate(pattern)
                        .setColor(getResources().getColor(R.color.colorAccent))
                        .setLights(Color.BLUE, 1, 1)
                        .setSound(defaultSoundUri)
                        .setContentIntent(createPendingIntent())
                        .setGroupSummary(true)
                        .setTicker(messageBody)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody));
            } else {
                builder = new NotificationCompat.Builder(this);
                builder.setSmallIcon(R.drawable.ic_launcher)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setVibrate(pattern)
                        .setColor(getResources().getColor(R.color.colorAccent))
                        .setLights(Color.BLUE, 1, 1)
                        .setSound(defaultSoundUri)
                        .setContentIntent(createPendingIntent())
                        .setGroupSummary(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setTicker(messageBody)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody));
            }


            /*notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_notification_stat_default)
                    .setLargeIcon(largerIcon)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setVibrate(pattern)
                    .setColor(getResources().getColor(R.color.colorAccent))
                    .setLights(Color.BLUE, 1, 1)
                    .setSound(defaultSoundUri)
                    .setContentIntent(createPendingIntent())
                    .setGroupSummary(true)
                    .build();*/
        }

        Notification notification = builder.build();
        notifManager.notify(NOTIFY_ID, notification);

        /*NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);*/
    }


    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
