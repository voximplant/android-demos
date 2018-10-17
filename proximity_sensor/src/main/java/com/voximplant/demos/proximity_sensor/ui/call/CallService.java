/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.proximity_sensor.ui.call;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.voximplant.demos.proximity_sensor.R;
import com.voximplant.demos.proximity_sensor.utils.Constants;
import com.voximplant.demos.proximity_sensor.utils.NotificationHelper;

import static android.os.PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK;
import static android.os.PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY;
import static com.voximplant.demos.proximity_sensor.utils.Constants.APP_TAG;

public class CallService extends Service implements SensorEventListener {
    private PowerManager.WakeLock mProximityWakelock;
    private boolean mIsProximityNear;

    @Override
    public void onCreate() {
    }

    @SuppressLint({"InvalidWakeLockTag", "InlinedApi"})
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(Constants.ACTION_FOREGROUND_SERVICE_START)) {
                Notification notification = NotificationHelper.get()
                        .buildCallNotification(getString(R.string.call_service_notification_text), getApplicationContext());
                startForeground(1, notification);

                SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                if (sensorManager != null) {
                    Sensor proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                    PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    if (powerManager != null && proximity != null) {
                        try {
                            if (mProximityWakelock == null) {
                                mProximityWakelock = powerManager.newWakeLock(PROXIMITY_SCREEN_OFF_WAKE_LOCK, "voximplant-prox-sens-demo");
                            }
                            sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
                        } catch (Exception ex) {
                            Log.e(APP_TAG, "CallService: onStartCommand: exception on proximity sensor initialization: " + ex.getMessage());
                        }
                    }
                }
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
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        if (mProximityWakelock != null && mProximityWakelock.isHeld()) {
            Log.i(APP_TAG, "CallService: onDestroy: release wake lock");
            mProximityWakelock.release();
        }
        super.onDestroy();
    }

    @SuppressLint({"WakelockTimeout", "NewApi"})
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            boolean newIsNear = sensorEvent.values[0] < Math.min(sensorEvent.sensor.getMaximumRange(), 3);
            if (newIsNear != mIsProximityNear) {
                mIsProximityNear = newIsNear;
                try{
                    if(mIsProximityNear && !mProximityWakelock.isHeld()) {
                        Log.i(APP_TAG, "CallService: onSensorChanged: acquire wake lock");
                        mProximityWakelock.acquire();
                    } else if (mProximityWakelock.isHeld()) {
                        Log.i(APP_TAG, "CallService: onSensorChanged: release wake lock");
                        mProximityWakelock.release(RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY);
                    }
                } catch (Exception ex){
                    Log.e(APP_TAG, "CallService: onSensorChanged: exception on proximity sensor: " + ex.getMessage());
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
