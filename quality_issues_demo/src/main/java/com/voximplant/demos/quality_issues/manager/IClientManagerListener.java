/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.quality_issues.manager;

import android.util.Log;

import com.voximplant.sdk.client.LoginError;

import static com.voximplant.demos.quality_issues.utils.Constants.APP_TAG;

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
