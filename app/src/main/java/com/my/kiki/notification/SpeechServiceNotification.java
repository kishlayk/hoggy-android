package com.my.kiki.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.my.kiki.R;
import com.my.kiki.ui.HomeActivity;

public class SpeechServiceNotification {

    private Context context;
    public static final String CHANNEL_ID = "hoggy_channel_id";
    public static final int SPEECH_NOTIFICATION_ID = 300;
    public static final String SPEECH_CHANNEL_NAME = "hoggy_speech_channel";
    public static final String SPEECH_CHANNEL_DESCRIPTION = "hoggy_speech_channel_description";
    public static final String ACTION_NAME = "disconnect";

    private int notificationTapRequestCode = 201;
    private int actionTapRequestCode = 204;
    private NotificationManager notificationManager;
    private NotificationManagerCompat notificationManagerCompat;
    private NotificationCompat.Builder mBuilder;

    public SpeechServiceNotification(Context context) {
        this.context = context;
    }

    public void buildNotification(String contentTopic){

        Log.d("notifi","build()");

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.appicon);
        String title = "Hoggy";
        String content = contentTopic;

        Intent intent = new Intent(context, HomeActivity.class);
        intent.addCategory("android.intent.category.LAUNCHER")
                .setAction("android.intent.action.MAIN");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationTapRequestCode, intent, 0);

        Intent disconnectIntent = new Intent(context, HomeActivity.class);
        disconnectIntent.putExtra("action",ACTION_NAME);
//        disconnectIntent.setAction(ACTION_NAME);
        PendingIntent disconnectPendingIntent =
                PendingIntent.getActivity(context, actionTapRequestCode, disconnectIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_small_icon)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(content))
                .setLights(Color.GREEN, 1, 1)
                .setAutoCancel(false)
                .addAction(R.drawable.ic_bluetooth_connected_black_24dp ,context.getString(R.string.notification_action_disconnect),
                        disconnectPendingIntent);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            mBuilder.setLargeIcon(largeIcon);

        notificationManagerCompat = NotificationManagerCompat.from(context);

        notificationManagerCompat.notify(SPEECH_NOTIFICATION_ID,mBuilder.build());

    }

    public void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,SPEECH_CHANNEL_NAME, importance);
            channel.setDescription(SPEECH_CHANNEL_DESCRIPTION);
            notificationManager = context.getSystemService(NotificationManager.class);
            try {
                notificationManager.createNotificationChannel(channel);
            }catch (NullPointerException e){
                Log.w("notifi","could not create channel, " + e);
            }
        }
    }

    public void removeNotification(int id) {

        Log.d("notifi", "remove()");
        try {
            if(notificationManagerCompat != null)
            notificationManagerCompat.cancel(id);
        } catch (NullPointerException e) {
            Log.w("notifi", e.toString());
        }
    }
}
