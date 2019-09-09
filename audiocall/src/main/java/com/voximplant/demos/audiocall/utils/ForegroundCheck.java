/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;

public class ForegroundCheck implements Application.ActivityLifecycleCallbacks {

    private static ForegroundCheck instance;
    private boolean foreground = false;

    public static void init(Application application) {
        if (instance == null) {
            instance = new ForegroundCheck();
            application.registerActivityLifecycleCallbacks(instance);
        }
    }

    public static ForegroundCheck get() {
        if (instance == null) {
            throw new IllegalStateException("ForegroundCheck is not initialized");
        }
        return instance;
    }

    public boolean isForeground() {
        return foreground;
    }


    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        foreground = true;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        foreground = false;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}

