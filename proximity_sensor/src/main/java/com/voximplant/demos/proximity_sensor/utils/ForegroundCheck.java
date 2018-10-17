/*
 * Copyright (c) 2017, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.proximity_sensor.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class ForegroundCheck implements Application.ActivityLifecycleCallbacks {

    private static ForegroundCheck instance;
    private boolean foreground = false;

    public static ForegroundCheck init(Application application) {
        if (instance == null) {
            instance = new ForegroundCheck();
            application.registerActivityLifecycleCallbacks(instance);
        }
        return instance;
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
    public void onActivityResumed(Activity activity) {
        foreground = true;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        foreground = false;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}

