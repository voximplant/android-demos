/*
 * Copyright (c) 2017, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.proximity_sensor.utils;

import android.content.Context;
import android.util.Log;

import com.voximplant.sdk.Voximplant;
import com.voximplant.sdk.client.ILogListener;
import com.voximplant.sdk.client.LogLevel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLogger implements ILogListener{
    private static FileLogger mInstance = null;
    private static String FILE_NAME = "vox_log.txt";
    private FileOutputStream mOutputStream;
    private DateFormat mDateFormat;

    public static synchronized void create(Context context) {
        if (mInstance == null) {
            mInstance = new FileLogger(context);
        }
    }

    private FileLogger(Context context) {
        mDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        try {
            mOutputStream = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            Log.e("SDKDemoApplication", "FileLogger: failed to open file");
        }
        Voximplant.setLogListener(this);
    }

    public void shutdown() {
        try {
            mOutputStream.close();
        } catch (IOException e) {
            Log.e("SDKDemoApplication", "FileLogger: failed close output stream");
        }
    }

    @Override
    public void onLogMessage(LogLevel level, String logMessage) {
        if (mOutputStream != null) {
            try {
                switch (level) {
                    case ERROR:
                        logMessage = mDateFormat.format(new Date()) + " ERROR:   " + logMessage + "\n";
                        break;
                    case WARNING:
                        logMessage = mDateFormat.format(new Date()) + " WARNING: " + logMessage + "\n";
                        break;
                    case INFO:
                        logMessage = mDateFormat.format(new Date()) + " INFO:    " + logMessage + "\n";
                        break;
                    case DEBUG:
                        logMessage = mDateFormat.format(new Date()) + " DEBUG:   " + logMessage + "\n";
                        break;
                    case VERBOSE:
                    default:
                        logMessage = mDateFormat.format(new Date()) + " VERBOSE: " + logMessage + "\n";
                        break;
                }
                mOutputStream.write(logMessage.getBytes());
            } catch (IOException e) {
                Log.e("SDKDemoApplication", "FileLogger: failed to write log message");
            }
        }
    }
}
