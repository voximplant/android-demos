/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.utils;

import android.util.Log;

import java.security.MessageDigest;


public class MD5{
    public static String get(String key) {
        try {
            byte[] bytesOfMessage = key.toLowerCase().getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(bytesOfMessage);

            StringBuilder builder = new StringBuilder();
            for (byte aDigest : digest) builder.append(String.format("%02X", aDigest));
            return builder.toString().toLowerCase();

        } catch (Exception e) {
            Log.e("MD5","onOneTimeKey: exception " + e);
        }
        return null;
    }
}
