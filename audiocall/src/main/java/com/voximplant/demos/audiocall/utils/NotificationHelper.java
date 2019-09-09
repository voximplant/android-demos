/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.voximplant.demos.audiocall.R;

import static android.app.NotificationManager.IMPORTANCE_HIGH;
import static android.app.NotificationManager.IMPORTANCE_LOW;
import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Context.NOTIFICATION_SERVICE;
import static com.voximplant.demos.audiocall.utils.Constants.ACTION_DECLINE_CALL;
import static com.voximplant.demos.audiocall.utils.Constants.APP_TAG;
import static com.voximplant.demos.audiocall.utils.Constants.CALL_ANSWERED;
import static com.voximplant.demos.audiocall.utils.NotificationHelper.ChannelType.ForegroundServiceChannel;
import static com.voximplant.demos.audiocall.utils.NotificationHelper.ChannelType.IncomingCallChannel;

public class NotificationHelper {
    private static final String INCOMING_CALL_CHANNEL_ID = "VoximplantChannelIncomingCalls";
    private static final String FOREGROUND_SERVICE_CHANNEL_ID = "VoximplantCallServiceChannel";
    private static final String INCOMING_CALL_CHANNEL_NAME = "CallChannel";
    private static final String INCOMING_CALL_CHANNEL_INFO = "Audio calls notifications";
    private static final String FOREGROUND_SERVICE_CHANNEL_INFO = "Call service notifications";
    private static final String FOREGROUND_SERVICE_CHANNEL_NAME = "ChannelCallService";

    private static NotificationManager mNotificationManager;
    private static int incomingCallNotificationId = 100;

    public static void init(Context context) {
        mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        createChannel(ForegroundServiceChannel);
    }

    //region ChannelType
    public enum ChannelType {
        IncomingCallChannel,
        ForegroundServiceChannel
    }

    private static void createChannel(ChannelType channelType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mNotificationManager.getNotificationChannel(channelType == IncomingCallChannel ? INCOMING_CALL_CHANNEL_ID : FOREGROUND_SERVICE_CHANNEL_ID) == null) {
                String ID          = channelType == IncomingCallChannel ? INCOMING_CALL_CHANNEL_ID : FOREGROUND_SERVICE_CHANNEL_ID;
                String name        = channelType == IncomingCallChannel ? INCOMING_CALL_CHANNEL_NAME : FOREGROUND_SERVICE_CHANNEL_NAME;
                String description = channelType == IncomingCallChannel ? INCOMING_CALL_CHANNEL_INFO : FOREGROUND_SERVICE_CHANNEL_INFO;
                int importance     = channelType == IncomingCallChannel ? IMPORTANCE_HIGH : IMPORTANCE_LOW;

                NotificationChannel notificationChannel = new NotificationChannel(ID, name, importance);
                notificationChannel.setDescription(description);
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }
    //endregion

    //region Notifications
    public static void showCallNotification(Context context, Intent intent, String displayName) {
        NotificationHelper.createChannel(IncomingCallChannel);

        PendingIntent mainActionPendingIntent = PendingIntent.getActivity(context, 0,
                intent, FLAG_UPDATE_CURRENT | FLAG_ONE_SHOT);
        PendingIntent answerPendingIntent = PendingIntent.getActivity(context, 1,
                intent.putExtra(CALL_ANSWERED, true), FLAG_UPDATE_CURRENT | FLAG_ONE_SHOT);
        PendingIntent declinePendingIntent = PendingIntent.getBroadcast(context, 2,
                new Intent().setAction(ACTION_DECLINE_CALL), FLAG_UPDATE_CURRENT | FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, INCOMING_CALL_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_vox_notification)
                .setContentTitle(APP_TAG)
                .setContentText((displayName == null ? context.getString(R.string.somebody) : displayName) + " " + context.getString(R.string.is_calling))
                .setFullScreenIntent(mainActionPendingIntent, true)
                .addAction(R.drawable.ic_vox_notification, context.getString(R.string.notificationAnswer), answerPendingIntent)
                .addAction(R.drawable.ic_vox_notification, context.getString(R.string.notificationDecline), declinePendingIntent);
        mNotificationManager.notify(incomingCallNotificationId, builder.build());
    }

    public static Notification buildForegroundServiceNotification(Context context, String text) {
        return new NotificationCompat.Builder(context, FOREGROUND_SERVICE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_vox_notification)
                .setContentTitle(APP_TAG)
                .setContentText(text)
                .build();
    }

    public static void cancelNotification() {
        mNotificationManager.cancel(incomingCallNotificationId);
    }
    //endregion
}
