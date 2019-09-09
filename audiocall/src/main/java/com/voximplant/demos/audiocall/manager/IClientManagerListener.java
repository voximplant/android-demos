/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.manager;

import android.util.Log;

import com.voximplant.sdk.client.LoginError;

import static com.voximplant.demos.audiocall.utils.Constants.APP_TAG;

public interface IClientManagerListener {
    default void onConnectionFailed() {
        Log.e(APP_TAG, "Connection failed");
    }
    default void onConnectionClosed() {
        Log.i(APP_TAG, "Connection closed");
    }
    default void onLoginFailed(LoginError reason) {
        Log.e(APP_TAG, "Login failed " + reason);
    }
    default void onLoginSuccess(String displayName) {
        Log.i(APP_TAG, "Login success: " + displayName);
    }
}
