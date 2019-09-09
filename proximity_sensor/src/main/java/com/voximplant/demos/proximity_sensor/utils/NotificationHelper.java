/*
 * Copyright (c) 2017, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.proximity_sensor.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import com.voximplant.demos.proximity_sensor.R;
import com.voximplant.demos.proximity_sensor.ui.calls.MakeCallActivity;

public class NotificationHelper {
    private static NotificationHelper instance = null;
	private static NotificationManager mNotificationManager;
	private static int notificationId = 0;

    private NotificationHelper(Context context) {
        mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID,
                    "Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            channel.setDescription("description");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);

            mNotificationManager.createNotificationChannel(channel);
        }
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new NotificationHelper(context);
        }
    }

    public static NotificationHelper get() {
        if (instance == null) {
            throw new IllegalStateException("NotificationHelper is not initialized");
        }
        return instance;
    }


    public Notification buildCallNotification(String text, Context context) {
        Intent notificationIntent = new Intent(context, MakeCallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        return new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Voximplant")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_vox_notification)
                .build();
    }
}
