/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.quality_issues.ui.calls;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.voximplant.demos.quality_issues.R;
import com.voximplant.demos.quality_issues.ui.call.CallActivity;
import com.voximplant.demos.quality_issues.ui.login.LoginActivity;
import com.voximplant.sdk.Voximplant;

import java.util.ArrayList;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;
import static com.voximplant.demos.quality_issues.utils.Constants.CALL_ANSWERED;
import static com.voximplant.demos.quality_issues.utils.Constants.CALL_ID;
import static com.voximplant.demos.quality_issues.utils.Constants.INCOMING_CALL;
import static com.voximplant.demos.quality_issues.utils.Constants.INCOMING_CALL_RESULT;
import static com.voximplant.demos.quality_issues.utils.Constants.WITH_VIDEO;

public class MakeCallActivity extends AppCompatActivity implements MakeCallContract.View {
    private final static int PERMISSION_NOT_REQUESTED = 1;
    private final static int PERMISSION_REQUESTED_AUDIO = 2;
    private final static int PERMISSION_REQUESTED_VIDEO = 3;

    private EditText mCallToView;
    private ImageButton mAudioCallButton;
    private ImageButton mVideoCallButton;
    private MakeCallContract.Presenter mPresenter;

    private AlertDialog mAlertDialog;

    private int mPermissionRequestedMode = PERMISSION_NOT_REQUESTED;
    private boolean mIsAudioPermissionsGranted;
    private boolean mIsVideoPermissionsGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_call);

        mCallToView = findViewById(R.id.call_to);
        mAudioCallButton = findViewById(R.id.button_audio_call);
        mVideoCallButton = findViewById(R.id.button_video_call);

        mAudioCallButton.setOnClickListener(v -> {
            mPresenter.makeCall(mCallToView.getText().toString(), false);
            hideKeyboard(v);
        });
        mAudioCallButton.setOnTouchListener((v, event) -> {
            changeButton(mAudioCallButton, event.getAction());
            return false;
        });

        mVideoCallButton.setOnClickListener(v -> {
            mPresenter.makeCall(mCallToView.getText().toString(), true);
            hideKeyboard(v);
        });
        mVideoCallButton.setOnTouchListener((v, event) -> {
            changeButton(mVideoCallButton, event.getAction());
            return false;
        });

        mPresenter = new MakeCallPresenter(this);
        mPresenter.start();
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (!intent.getBooleanExtra("processed", false)) {
            intent.putExtra("processed", true);
            String callId = intent.getStringExtra(CALL_ID);
            int result = intent.getIntExtra(INCOMING_CALL_RESULT, -1);
            switch (result) {
                case CALL_ANSWERED:
                    boolean withVideo = intent.getBooleanExtra(WITH_VIDEO, false);
                    mPresenter.answerCall(callId, withVideo);
                    break;
            }
        }

        if (mPermissionRequestedMode == PERMISSION_REQUESTED_VIDEO && mIsAudioPermissionsGranted && mIsVideoPermissionsGranted) {
            mPresenter.permissionsAreGrantedForCall();
        } else if (mPermissionRequestedMode == PERMISSION_REQUESTED_AUDIO && mIsAudioPermissionsGranted) {
            mPresenter.permissionsAreGrantedForCall();
        }
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
    public void onDestroy(){
        super.onDestroy();
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
    }

    @Override
    public void notifyConnectionClosed() {
        showAlertDialog(R.string.alert_title_disconnected, R.string.alert_content_disconnected);
    }

    @Override
    public void notifyInvalidCallUser() {
        mCallToView.setError(getString(R.string.error_field_required));
        mCallToView.requestFocus();
    }

    @Override
    public void startCallActivity(String callId, boolean withVideo, String user, boolean isIncoming) {
        Intent intent = new Intent(getApplicationContext(), CallActivity.class);
        intent.putExtra(CALL_ID, callId);
        intent.putExtra(WITH_VIDEO, withVideo);
        intent.putExtra(INCOMING_CALL, isIncoming);
        startActivity(intent);
    }

    @Override
    public boolean checkPermissionsGrantedForCall(boolean isVideoCall) {
        ArrayList<String> missingPermissions = (ArrayList<String>) Voximplant.getMissingPermissions(getApplicationContext(), isVideoCall);
        if (missingPermissions.size() == 0) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, missingPermissions.toArray(new String[missingPermissions.size()]), PERMISSION_GRANTED);
            mPermissionRequestedMode = isVideoCall ? PERMISSION_REQUESTED_VIDEO : PERMISSION_REQUESTED_AUDIO;
            return false;
        }
    }

    private void showAlertDialog(int resTitle, int resContent) {
        runOnUiThread(() -> mAlertDialog = new AlertDialog.Builder(MakeCallActivity.this)
                .setTitle(resTitle)
                .setMessage(resContent)
                .setPositiveButton(R.string.alert_positive_button, (dialog, which) -> {
                    mPresenter.logout();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .show());
    }

    private void changeButton(ImageButton button, int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                button.setColorFilter(getResources().getColor(R.color.colorWhite));
                button.setBackground(getResources().getDrawable(R.drawable.button_image_active));
                break;
            case MotionEvent.ACTION_UP:
                button.setColorFilter(getResources().getColor(R.color.colorAccent));
                button.setBackground(getResources().getDrawable(R.drawable.button_image_passive));
                break;
        }
    }

    private void hideKeyboard(View v) {
        InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}
