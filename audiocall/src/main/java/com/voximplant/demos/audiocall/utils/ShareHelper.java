package com.voximplant.demos.audiocall.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;

public class ShareHelper {
    private static ShareHelper sShareHelper = null;
    private ShareHelper() {

    }

    public static synchronized ShareHelper getInstance() {
        if (sShareHelper == null) {
            sShareHelper = new ShareHelper();
        }
        return sShareHelper;
    }

    public Intent createShareIntent(Context context) {

        File logFile = new File(context.getFilesDir() + "/vox_log.txt");
        Uri fileUri = null;
        try {
            fileUri = FileProvider.getUriForFile(context, "com.voximplant.demos.audiocall.fileprovider", logFile);
        } catch (IllegalArgumentException e) {
            Log.e("YULIA", "selected file can't be shared");
        }
        if (fileUri != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share log file");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            return shareIntent;
        }
        return null;
    }
}
