/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.quality_issues.ui.call;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.voximplant.demos.quality_issues.R;
import com.voximplant.demos.quality_issues.utils.Constants;
import com.voximplant.demos.quality_issues.utils.NotificationHelper;

public class CallService extends Service {

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Constants.ACTION_FOREGROUND_SERVICE_START)) {
                int numberOfCalls = 0;
                if (intent.getExtras() != null) {
                    numberOfCalls = intent.getExtras().getInt(Constants.SERVICE_NOTIFICATION_DETAILS);
                }
                Notification notification = NotificationHelper.get()
                        .buildCallNotification(numberOfCalls + " " + getString(R.string.call_service_notification_text), getApplicationContext());
                startForeground(1, notification);
            } else if (action.equals(Constants.ACTION_FOREGROUND_SERVICE_STOP)) {
                stopSelf();
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {

    }
}
