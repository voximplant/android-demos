/*
 * Copyright (c) 2017, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.quality_issues.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.voximplant.demos.quality_issues.R;
import com.voximplant.demos.quality_issues.ui.call.CallActivity;

import static com.voximplant.demos.quality_issues.utils.Constants.APP_TAG;

public class NotificationHelper {
    private static NotificationHelper instance = null;

    private NotificationHelper(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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

    public Notification buildForegroundServiceNotification(Context context, String text) {

        Intent intent = new Intent(context, CallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        return new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_vox_notification)
                .setContentTitle(APP_TAG)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build();
    }
}
