/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.proximity_sensor.ui.call;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.voximplant.demos.proximity_sensor.R;
import com.voximplant.demos.proximity_sensor.ui.calls.MakeCallActivity;

import org.webrtc.SurfaceViewRenderer;

import static com.voximplant.demos.proximity_sensor.utils.Constants.CALL_ID;
import static com.voximplant.demos.proximity_sensor.utils.Constants.INCOMING_CALL;
import static com.voximplant.demos.proximity_sensor.utils.Constants.WITH_VIDEO;

public class CallActivity extends AppCompatActivity implements CallContract.View {

    private ImageButton mHangupButton;
    private CallContract.Presenter mPresenter;

    private SurfaceViewRenderer mLocalVideoView;
    private SurfaceViewRenderer mRemoteVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        Intent intent = getIntent();
        String callId = intent.getStringExtra(CALL_ID);
        boolean withVideo = intent.getBooleanExtra(WITH_VIDEO, false);
        boolean isIncomingCall = intent.getBooleanExtra(INCOMING_CALL, false);

        mLocalVideoView = findViewById(R.id.local_video_view);
        mRemoteVideoView = findViewById(R.id.remote_video_view);

        mPresenter = new CallPresenter(this, callId, isIncomingCall, withVideo);
        mPresenter.start();

        mHangupButton = findViewById(R.id.hangup_button);
        mHangupButton.setOnTouchListener((v, event) -> {
            changeButton(mHangupButton, event.getAction());
            return false;
        });
        mHangupButton.setOnClickListener(v -> mPresenter.stopCall());
    }

    @Override
    public void createLocalVideoView() {
        runOnUiThread(() -> {
            mLocalVideoView.setVisibility(View.VISIBLE);
        });
        mPresenter.localVideoViewCreated(mLocalVideoView);
    }

    @Override
    public void removeLocalVideoView() {
        runOnUiThread(() -> mLocalVideoView.setVisibility(View.INVISIBLE));
        mPresenter.localVideoViewRemoved(mLocalVideoView);
    }

    @Override
    public void createRemoteVideoView(String streamId, String displayName) {
        runOnUiThread(() -> mRemoteVideoView.setVisibility(View.VISIBLE));
        mPresenter.remoteVideoViewCreated(streamId, mRemoteVideoView);
    }

    @Override
    public void removeRemoteVideoView(String streamId) {
        runOnUiThread(() -> mRemoteVideoView.setVisibility(View.INVISIBLE));
        mPresenter.remoteVideoViewRemoved(streamId, mRemoteVideoView);
    }

    @Override
    public void removeAllVideoViews() {

    }

    @Override
    public void callDisconnected() {
        Intent intent = new Intent(getApplicationContext(), MakeCallActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void callFailed(String error) {
        runOnUiThread(() -> new AlertDialog.Builder(this)
                .setTitle(R.string.call_failed)
                .setMessage("Reason: " + error)
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(getApplicationContext(), MakeCallActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setOnDismissListener(dialog -> {
                    Intent intent = new Intent(getApplicationContext(), MakeCallActivity.class);
                    startActivity(intent);
                    finish();
                })
                .show()
        );
    }

    @Override
    public void showError(int resError, String param1, String param2) {
        String error = getString(resError, param1, param2);
        runOnUiThread(() -> Toast.makeText(this, error, Toast.LENGTH_SHORT).show());
    }

    private void changeButton(ImageButton button, int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                button.setColorFilter(getResources().getColor(R.color.colorWhite));
                button.setBackground(getResources().getDrawable(R.drawable.button_image_red_active));
                break;
            case MotionEvent.ACTION_UP:
                button.setColorFilter(getResources().getColor(R.color.colorRed));
                button.setBackground(getResources().getDrawable(R.drawable.button_image_red_passive));
                break;
        }
    }
}
