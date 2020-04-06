/*
 * Copyright (c) 2011- 2020, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.videoconf.manager;

public interface IClientManagerListener {
    default void onLoginSuccessful() {}
    default void onLoginFailed(String error) {}
    default void onConnectionClosed() {}
}
