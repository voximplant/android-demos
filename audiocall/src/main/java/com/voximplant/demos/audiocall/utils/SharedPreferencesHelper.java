/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesHelper {

    private static SharedPreferencesHelper instance = null;
    private final SharedPreferences prefs;

    private SharedPreferencesHelper(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesHelper(context);
        }
    }

    public static SharedPreferencesHelper get() {
        if (instance == null) {
            throw new IllegalStateException("SharedPreferencesHelper is not initialized");
        }
        return instance;
    }

    public void saveToPrefs(String key, long value) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public void removeFromPrefs(String key) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        editor.apply();
    }

    public void saveToPrefs(String key, String value) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getStringFromPrefs(String key) {
        try {
            return prefs.getString(key, null);
        } catch (Exception e) {
            return null;
        }
    }

    public long getLongFromPrefs(String key) {
        try {
            return prefs.getLong(key, 0);
        } catch (Exception e) {
            return 0;
        }
    }
}
