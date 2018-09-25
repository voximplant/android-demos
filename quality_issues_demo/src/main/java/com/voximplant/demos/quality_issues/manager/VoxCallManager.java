/*
 * Copyright (c) 2017, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.quality_issues.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.voximplant.demos.quality_issues.ui.call.CallService;
import com.voximplant.demos.quality_issues.utils.Constants;
import com.voximplant.sdk.call.CallException;
import com.voximplant.sdk.call.CallSettings;
import com.voximplant.sdk.call.CallStats;
import com.voximplant.sdk.call.ICall;
import com.voximplant.sdk.call.ICallListener;
import com.voximplant.sdk.call.IEndpoint;
import com.voximplant.sdk.call.IVideoStream;
import com.voximplant.sdk.call.RejectMode;
import com.voximplant.sdk.call.VideoFlags;
import com.voximplant.sdk.client.IClient;
import com.voximplant.sdk.client.IClientIncomingCallListener;
import com.voximplant.demos.quality_issues.ui.incomingcall.IncomingCallActivity;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.voximplant.demos.quality_issues.utils.Constants.APP_TAG;
import static com.voximplant.demos.quality_issues.utils.Constants.CALL_ID;
import static com.voximplant.demos.quality_issues.utils.Constants.DISPLAY_NAME;
import static com.voximplant.demos.quality_issues.utils.Constants.WITH_VIDEO;

public class VoxCallManager implements IClientIncomingCallListener, ICallListener {
    private ICall mCall = null;
    private final IClient mClient;
    private final Context mAppContext;
    private CopyOnWriteArrayList<ICallEventsListener> mCallEventsListeners = new CopyOnWriteArrayList<>();

    public VoxCallManager(IClient client, Context appContext) {
        mClient = client;
        mAppContext = appContext;
        mClient.setClientIncomingCallListener(this);
    }

    public void addCallEventListener(String callId, ICallEventsListener listener) {
        if (mCall != null && mCall.getCallId().equals(callId)) {
            mCall.addCallListener(this);
            mCallEventsListeners.add(listener);
        }
    }

    public void removeCallEventListener(String callId, ICallEventsListener listener) {
        if (mCall != null && mCall.getCallId().equals(callId)) {
            mCall.removeCallListener(this);
            mCallEventsListeners.remove(listener);
        }
    }

    public String createCall(String user, VideoFlags videoFlags) {
        if (mCall == null) {
            CallSettings callSettings = new CallSettings();
            callSettings.videoFlags = videoFlags;
            ICall call = mClient.call(user, callSettings);
            if (call != null) {
                mCall = call;
                return call.getCallId();
            }
            return null;
        } else {
            Log.e(APP_TAG, "Failed to create a call, this demo supports only one active call");
            return null;
        }
    }

    public void endAllCalls() {
        if (mCall != null) {
            mCall.hangup(null);
        }
    }

    public ICall getCallById(String callId) {
        return mCall != null && mCall.getCallId().equals(callId) ? mCall : null;
    }

    public void startForegroundCallService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(mAppContext, CallService.class);
            intent.setAction(Constants.ACTION_FOREGROUND_SERVICE_START);
            mAppContext.startForegroundService(intent);
        }
    }

    public void stopForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mCall == null) {
            Intent intent = new Intent(mAppContext, CallService.class);
            intent.setAction(Constants.ACTION_FOREGROUND_SERVICE_STOP);
            mAppContext.stopService(intent);
        }
    }

    @Override
    public void onIncomingCall(ICall call, boolean video,  Map<String, String> headers) {
        if (mCall == null) {
            mCall = call;
            Intent incomingCallIntent = new Intent(mAppContext, IncomingCallActivity.class);
            incomingCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            incomingCallIntent.putExtra(CALL_ID, call.getCallId());
            incomingCallIntent.putExtra(WITH_VIDEO, video);
            incomingCallIntent.putExtra(DISPLAY_NAME, call.getEndpoints().get(0).getUserDisplayName());
            mAppContext.startActivity(incomingCallIntent);
        } else {
            Log.e(APP_TAG, "There is already an active call, declining incoming call due to this " +
                    "demo supports only one active call");
            try {
                call.reject(RejectMode.DECLINE, null);
            } catch (CallException e) {
                Log.e(APP_TAG, "Exception on call reject: " + e.getMessage());
            }
        }
    }

    @Override
    public void onCallConnected(ICall call, Map<String, String> headers) {
        for (ICallEventsListener listener : mCallEventsListeners) {
            listener.onCallConnected(headers);
        }
    }

    @Override
    public void onCallDisconnected(ICall call, Map<String, String> headers, boolean answeredElsewhere) {
        for (ICallEventsListener listener : mCallEventsListeners) {
            listener.onCallDisconnected(headers, answeredElsewhere);
        }
        mCall = null;
        stopForegroundService();
    }

    @Override
    public void onCallRinging(ICall call, Map<String, String> headers) {
        for (ICallEventsListener listener : mCallEventsListeners) {
            listener.onCallRinging(headers);
        }
    }

    @Override
    public void onCallFailed(ICall call, int code, String description, Map<String, String> headers) {
        for (ICallEventsListener listener : mCallEventsListeners) {
            listener.onCallFailed(code, description, headers);
        }
        mCall = null;
        stopForegroundService();
    }

    @Override
    public void onCallAudioStarted(ICall call) {
        for (ICallEventsListener listener : mCallEventsListeners) {
            listener.onCallAudioStarted();
        }
    }

    @Override
    public void onSIPInfoReceived(ICall call, String type, String content, Map<String, String> headers) {
        for (ICallEventsListener listener : mCallEventsListeners) {
            listener.onSIPInfoReceived(type, content, headers);
        }
    }

    @Override
    public void onMessageReceived(ICall call, String text) {
        for (ICallEventsListener listener : mCallEventsListeners) {
            listener.onMessageReceived(text);
        }
    }

    @Override
    public void onLocalVideoStreamAdded(ICall call, IVideoStream videoStream) {
        for (ICallEventsListener listener : mCallEventsListeners) {
            listener.onLocalVideoStreamAdded(videoStream);
        }
    }

    @Override
    public void onLocalVideoStreamRemoved(ICall call, IVideoStream videoStream) {
        for (ICallEventsListener listener : mCallEventsListeners) {
            listener.onLocalVideoStreamRemoved(videoStream);
        }
    }

    @Override
    public void onICETimeout(ICall call) {
        for (ICallEventsListener listener : mCallEventsListeners) {
            listener.onICETimeout();
        }
    }

    @Override
    public void onICECompleted(ICall call) {
        for (ICallEventsListener listener : mCallEventsListeners) {
            listener.onICECompleted();
        }
    }

    @Override
    public void onEndpointAdded(ICall call, IEndpoint endpoint) {
        for (ICallEventsListener listener : mCallEventsListeners) {
            listener.onEndpointAdded(endpoint);
        }
    }

    @Override
    public void onCallStatsReceived(ICall call, CallStats callStats) {
        for (ICallEventsListener listener : mCallEventsListeners) {
            listener.onCallStatsReceived(callStats);
        }
    }
}
