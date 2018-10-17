/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.proximity_sensor.ui.incomingcall;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.voximplant.demos.proximity_sensor.ui.calls.MakeCallActivity;
import com.voximplant.sdk.Voximplant;
import com.voximplant.demos.proximity_sensor.R;

import java.util.ArrayList;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static com.voximplant.demos.proximity_sensor.utils.Constants.CALL_ANSWERED;
import static com.voximplant.demos.proximity_sensor.utils.Constants.CALL_ID;
import static com.voximplant.demos.proximity_sensor.utils.Constants.DISPLAY_NAME;
import static com.voximplant.demos.proximity_sensor.utils.Constants.INCOMING_CALL_RESULT;
import static com.voximplant.demos.proximity_sensor.utils.Constants.WITH_VIDEO;

public class IncomingCallActivity extends AppCompatActivity implements IncomingCallContract.View {
    private IncomingCallContract.Presenter mPresenter;
    private boolean mIsAudioPermissionsGranted;
    private boolean mIsVideoPermissionsGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_incoming_call);

        String callId = null;
        boolean isVideo = false;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            TextView callFrom = findViewById(R.id.incoming_call_from);
            callFrom.setText(extras.getString(DISPLAY_NAME));
            callId = extras.getString(CALL_ID);
            isVideo = extras.getBoolean(WITH_VIDEO);
        }

        mPresenter = new IncomingCallPresenter(this, callId);

        ImageButton answerWithAudio = findViewById(R.id.answer_call_button);
        answerWithAudio.setOnTouchListener((View v, MotionEvent event) -> {
            changeButton(answerWithAudio, event, false);
            return false;
        });
        answerWithAudio.setOnClickListener(view -> {
            if (permissionsGrantedForCall(false)) {
                answerCall(false);
            }
        });

        ImageButton answerWithVideo = findViewById(R.id.answer_with_video_button);
        answerWithVideo.setVisibility(isVideo ? View.VISIBLE : View.GONE);
        answerWithVideo.setOnTouchListener((View v, MotionEvent event) -> {
            changeButton(answerWithVideo, event, false);
            return false;
        });
        answerWithVideo.setOnClickListener(view -> {
            if (permissionsGrantedForCall(true)) {
                answerCall(true);
            }
        });

        ImageButton reject = findViewById(R.id.decline_call_button);
        reject.setOnTouchListener((View v, MotionEvent event) -> {
            changeButton(reject, event, true);
            return false;
        });
        reject.setOnClickListener(view -> mPresenter.rejectCall());

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPresenter.isVideoCall() && mIsAudioPermissionsGranted && mIsVideoPermissionsGranted) {
            answerCall(true);
            finish();
        } else if (!mPresenter.isVideoCall() && mIsAudioPermissionsGranted) {
            answerCall(false);
            finish();
        }
    }

    private void changeButton(ImageButton button, MotionEvent event, boolean isRed) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                button.setColorFilter(getResources().getColor(R.color.colorWhite));
                button.setBackground(isRed ? getResources().getDrawable(R.drawable.button_image_red_active) : getResources().getDrawable(R.drawable.button_image_active));
                break;
            case MotionEvent.ACTION_UP:
                button.setColorFilter(isRed ? getResources().getColor(R.color.colorRed) : getResources().getColor(R.color.colorAccent));
                button.setBackground(isRed ? getResources().getDrawable(R.drawable.button_image_red_passive) : getResources().getDrawable(R.drawable.button_image_passive));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        mPresenter.rejectCall();
        finish();
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

    private void answerCall(boolean withVideo) {
        mPresenter.answerCall();

        Intent intent = new Intent(getApplicationContext(), MakeCallActivity.class);
        intent.putExtra(INCOMING_CALL_RESULT, CALL_ANSWERED);
        intent.putExtra(CALL_ID, mPresenter.getCallId());
        intent.putExtra(WITH_VIDEO, withVideo);

        startMainActivity(intent);
    }

    private void startMainActivity(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private boolean permissionsGrantedForCall(boolean isVideoCall) {
        ArrayList<String> missingPermissions = (ArrayList<String>) Voximplant.getMissingPermissions(getApplicationContext(), isVideoCall);
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

    @Override
    public void onCallEnded(String callId) {
        Intent intent = new Intent(getApplicationContext(), MakeCallActivity.class);
        intent.putExtra(CALL_ID, callId);
        startMainActivity(intent);
    }
}
