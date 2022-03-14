/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import com.voximplant.demos.audiocall.ui.call.CallService;
import com.voximplant.demos.audiocall.ui.incomingcall.IncomingCallActivity;
import com.voximplant.demos.audiocall.utils.Constants;
import com.voximplant.demos.audiocall.utils.NotificationHelper;
import com.voximplant.sdk.call.CallException;
import com.voximplant.sdk.call.CallSettings;
import com.voximplant.sdk.call.ICall;
import com.voximplant.sdk.call.ICallListener;
import com.voximplant.sdk.call.VideoFlags;
import com.voximplant.sdk.client.IClient;
import com.voximplant.sdk.client.IClientIncomingCallListener;
import com.voximplant.sdk.internal.Logger;

import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.voximplant.demos.audiocall.utils.Constants.ACTION_DECLINE_CALL;
import static com.voximplant.demos.audiocall.utils.Constants.APP_TAG;
import static com.voximplant.demos.audiocall.utils.Constants.DISPLAY_NAME;
import static com.voximplant.sdk.call.RejectMode.DECLINE;

public class VoxCallManager implements IClientIncomingCallListener, ICallListener {

    private final IClient mClient;
    private ICall mManagedCall;
    private final Context mAppContext;
    private CallActionReceiver mCallActionsReceiver = new CallActionReceiver();

    public VoxCallManager(IClient client, Context appContext) {
        mClient = client;
        mAppContext = appContext;
        mClient.setClientIncomingCallListener(this);
    }

    private class CallActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NotificationHelper.cancelNotification();
                rejectCall(mManagedCall);
                mAppContext.unregisterReceiver(this);
            }
        }
    }

    //region ManagedCall
    public ICall getCall() {
        return mManagedCall;
    }

    private void removeCall() {
        mManagedCall.removeCallListener(this);
        mManagedCall = null;
    }

    @Override
    public void onIncomingCall(ICall call, boolean video, Map<String, String> headers) {
        if (mManagedCall != null) {
            // App will reject incoming calls if already have one, because it supports only single managed call at a time.
            rejectCall(call);
            return;
        }

        mManagedCall = call;
        mManagedCall.addCallListener(this);
        String displayName = null;
        if (!call.getEndpoints().isEmpty()) {
            displayName = call.getEndpoints().get(0).getUserDisplayName();
        }

        Intent incomingCallIntent = new Intent(mAppContext, IncomingCallActivity.class);
        incomingCallIntent.putExtra(DISPLAY_NAME, displayName);
        incomingCallIntent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mAppContext.registerReceiver(mCallActionsReceiver, new IntentFilter(ACTION_DECLINE_CALL));
            NotificationHelper.showCallNotification(mAppContext, incomingCallIntent, displayName);
        } else {
            mAppContext.startActivity(incomingCallIntent);
        }
    }

    public boolean createCall(String user) {
        // App won't start new call if already have one, because it supports only single managed call at a time.
        if (mManagedCall != null) {
            return false;
        }

        CallSettings callSettings = new CallSettings();
        callSettings.videoFlags = new VideoFlags(false, false);
        mManagedCall = mClient.call(user, callSettings);

        if (mManagedCall == null) {
            return false;
        }

        mManagedCall.addCallListener(this);
        try {
            mManagedCall.start();
        } catch (CallException e) {
            Log.e(APP_TAG, "VoxCallManager: startCallException: " + e.getMessage());
            mManagedCall = null;
        }

        return mManagedCall != null;
    }

    public void rejectCall(ICall call) {
        try {
            call.reject(DECLINE, null);
        } catch (CallException e) {
            Log.e(APP_TAG, "VoxCallManager: reject call exception: " + e.getMessage());
        }
    }

    //region ICallListener
    @Override
    public void onCallConnected(ICall call, Map<String, String> headers) {
        startForegroundCallService();
        NotificationHelper.cancelNotification();
    }

    @Override
    public void onCallDisconnected(ICall call, Map<String, String> headers, boolean answeredElsewhere) {
        stopForegroundService();
        NotificationHelper.cancelNotification();
        removeCall();
    }

    @Override
    public void onCallFailed(ICall call, int code, String description, Map<String, String> headers) {
        stopForegroundService();
        NotificationHelper.cancelNotification();
        removeCall();
    }

    @Override
    public void onCallReconnecting(ICall call) {
        Log.d(APP_TAG, "VoxCallManager: onCallReconnecting");
    }

    @Override
    public void onCallReconnected(ICall call) {
        Log.d(APP_TAG, "VoxCallManager: onCallReconnected");
    }

    //endregion

    //endregion

    //region ForegroundServiceChannel
    private void startForegroundCallService() {
        Intent intent = new Intent(mAppContext, CallService.class);
        intent.setAction(Constants.ACTION_FOREGROUND_SERVICE_START);
        mAppContext.startService(intent);
    }

    private void stopForegroundService() {
        Intent intent = new Intent(mAppContext, CallService.class);
        intent.setAction(Constants.ACTION_FOREGROUND_SERVICE_STOP);
        mAppContext.stopService(intent);
    }
    //endregion
}
