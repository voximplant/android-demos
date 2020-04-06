/*
 * Copyright (c) 2011- 2020, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.videoconf.ui.connecting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.voximplant.demos.videoconf.R;
import com.voximplant.demos.videoconf.ui.BaseView;
import com.voximplant.demos.videoconf.ui.conf.ConfActivity;
import com.voximplant.sdk.Voximplant;

import java.util.ArrayList;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;
import static com.voximplant.demos.videoconf.Constants.MEETING_ID;
import static com.voximplant.demos.videoconf.Constants.USERNAME;

public class ConnectingActivity extends AppCompatActivity implements ConnectingContract.View {
    private ConnectingContract.Presenter mPresenter;
    private String mMeetingId;
    private String mUsername;

    private boolean mIsAudioPermissionsGranted;
    private boolean mIsVideoPermissionsGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connecting);

        mPresenter = new ConnectingPresenter(this);
        mPresenter.start();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mMeetingId = extras.getString(MEETING_ID);
            mUsername = extras.getString(USERNAME);
        }
        mPresenter.loginUser(mMeetingId, mUsername);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsAudioPermissionsGranted && mIsVideoPermissionsGranted) {
            navigateToConfScreen();
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.RECORD_AUDIO) && grantResults[i] == PERMISSION_GRANTED) {
                    mIsAudioPermissionsGranted = true;
                }
                if (permissions[i].equals(Manifest.permission.CAMERA) && grantResults[i] == PERMISSION_GRANTED) {
                    mIsVideoPermissionsGranted = true;
                }
            }
        }
    }

    @Override
    public void showError(String error) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Connection issues")
                    .setMessage(error)
                    .setPositiveButton("OK", (dialog, which) -> {
                        finish();
                    })
                    .setOnDismissListener(dialog -> {
                        finish();
                    });
            builder.create().show();
        });
    }

    @Override
    public void navigateToConfScreen() {
        if (permissionsGrantedForCall()) {
            Intent intent = new Intent(getApplicationContext(), ConfActivity.class);
            intent.putExtra(MEETING_ID, mMeetingId);
            startActivity(intent);
            finish();
        }
    }

    private boolean permissionsGrantedForCall() {
        ArrayList<String> missingPermissions = (ArrayList<String>) Voximplant.getMissingPermissions(getApplicationContext(), true);
        if (missingPermissions.size() == 0) {
            mIsAudioPermissionsGranted = true;
            mIsVideoPermissionsGranted = true;
            return true;
        } else {
            mIsAudioPermissionsGranted = !missingPermissions.contains(android.Manifest.permission.RECORD_AUDIO);
            mIsVideoPermissionsGranted = !missingPermissions.contains(android.Manifest.permission.CAMERA);
            ActivityCompat.requestPermissions(this, missingPermissions.toArray(new String[missingPermissions.size()]), PERMISSION_GRANTED);
            return false;
        }
    }
}
