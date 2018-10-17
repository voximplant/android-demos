/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.proximity_sensor.manager;

import android.util.Log;

import com.voximplant.sdk.call.CallStats;
import com.voximplant.sdk.call.IEndpoint;
import com.voximplant.sdk.call.IVideoStream;

import java.util.Map;

import static com.voximplant.demos.proximity_sensor.utils.Constants.APP_TAG;

public interface ICallEventsListener {
    default void onCallConnected(Map<String, String> headers) {
        Log.i(APP_TAG, "onCallConnected");
    }

    default void onCallDisconnected(Map<String, String> headers, boolean answeredElsewhere) {
        Log.i(APP_TAG, "onCallDisconnected");
    }

    default void onCallRinging(Map<String, String> headers) {
        Log.i(APP_TAG, "onCallRinging");
    }

    default void onCallFailed(int code, String description, Map<String, String> headers) {
        Log.i(APP_TAG, "onCallFailed");
    }

    default void onCallAudioStarted() {
        Log.i(APP_TAG, "onCallAudioStarted");
    }

    default void onSIPInfoReceived(String type, String content, Map<String, String> headers) {
        Log.i(APP_TAG, "onSIPInfoReceived");
    }

    default void onMessageReceived(String text) {
        Log.i(APP_TAG, "onMessageReceived");
    }

    default void onLocalVideoStreamAdded(IVideoStream videoStream) {
        Log.i(APP_TAG, "onLocalVideoStreamAdded");
    }

    default void onLocalVideoStreamRemoved(IVideoStream videoStream) {
        Log.i(APP_TAG, "onLocalVideoStreamRemoved");
    }

    default void onICETimeout() {
        Log.i(APP_TAG, "onICETimeout");
    }

    default void onICECompleted() {
        Log.i(APP_TAG, "onICECompleted");
    }

    default void onEndpointAdded(IEndpoint endpoint) {
        Log.i(APP_TAG, "onEndpointAdded");
    }

    default void onCallStatsReceived(CallStats callStats) {
        Log.i(APP_TAG, "onCallStatsReceived");
    }
}
