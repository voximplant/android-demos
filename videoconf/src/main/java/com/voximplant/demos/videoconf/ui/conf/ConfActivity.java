/*
 * Copyright (c) 2011- 2020, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.videoconf.ui.conf;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.voximplant.demos.videoconf.Constants;
import com.voximplant.demos.videoconf.R;
import com.voximplant.demos.videoconf.ui.start.StartConfActivity;
import com.voximplant.sdk.Voximplant;
import com.voximplant.sdk.hardware.AudioDevice;

import java.util.List;

import static com.voximplant.demos.videoconf.Constants.MEETING_ID;

public class ConfActivity extends AppCompatActivity implements ConfContract.View {
    private ConfContract.Presenter mPresenter;
    private VideoViewsHelper mVideoViewsHelper;

    private ImageButton mSendAudioButton;
    private ImageButton mSendVideoButton;
    private ImageButton mAudioDeviceButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        RelativeLayout videoViewsLayout = findViewById(R.id.video_views);

        String meetingId = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            meetingId = extras.getString(MEETING_ID);
        }

        mVideoViewsHelper = new VideoViewsHelper(getApplicationContext(), videoViewsLayout);

        mPresenter = new ConfPresenter(this, meetingId);

        ImageButton exitButton = findViewById(R.id.exit_button);
        exitButton.setOnClickListener(view -> mPresenter.stopCall());

        mSendAudioButton = findViewById(R.id.send_audio_button);
        mSendAudioButton.setOnClickListener(view -> mPresenter.muteAudio());

        mSendVideoButton = findViewById(R.id.send_video_button);
        mSendVideoButton.setOnClickListener(view -> mPresenter.sendVideo());

        mAudioDeviceButton = findViewById(R.id.audio_device_button);
        mAudioDeviceButton.setOnClickListener(view -> showAudioDeviceSelectionDialog(mPresenter.getAudioDevices()));

        ImageButton switchCameraButton = findViewById(R.id.switch_camera_button);
        switchCameraButton.setOnClickListener(view -> mPresenter.switchCamera());

        mPresenter.start();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void createVideoView(String streamId, String displayName) {
        runOnUiThread(() ->
                mVideoViewsHelper.addVideoView(streamId, displayName));
    }
    @Override
    public void removeVideoView(String streamId) {
        runOnUiThread(() ->
                mVideoViewsHelper.removeVideoView(streamId));
    }

    @Override
    public void requestVideoViewForEndpoint(String endpointId) {
        runOnUiThread(() ->
                mPresenter.receivedVideoViewForEndpoint(endpointId, mVideoViewsHelper.getRendererForEndpoint(endpointId)));
    }

    @Override
    public void removeAllVideoViews() {
        runOnUiThread(() -> mVideoViewsHelper.removeAllVideoViews());
    }

    @Override
    public void updateVideoView(String streamId, String displayName) {
        runOnUiThread(() -> mVideoViewsHelper.updateVideoView(streamId, displayName));
    }

    @Override
    public void updateAudioDeviceButton(AudioDevice audioDevice) {
        switch (audioDevice) {
            case EARPIECE:
                mAudioDeviceButton.setImageResource(R.drawable.ic_volume_down_black_24dp);
                break;
            case SPEAKER:
                mAudioDeviceButton.setImageResource(R.drawable.ic_volume_up_black_24dp);
                break;
            case WIRED_HEADSET:
                mAudioDeviceButton.setImageResource(R.drawable.ic_headset_black_24dp);
                break;
            case BLUETOOTH:
                mAudioDeviceButton.setImageResource(R.drawable.ic_bluetooth_audio_black_24dp);
                break;
        }
    }

    @Override
    public void updateMicButton(boolean muted) {
        runOnUiThread(() -> mSendAudioButton.setImageResource(muted ? R.drawable.ic_mic_off : R.drawable.ic_mic_on));
    }

    @Override
    public void updateSendVideoButton(boolean send) {
        runOnUiThread(() -> mSendVideoButton.setImageResource(send ? R.drawable.ic_video_on : R.drawable.ic_video_off));
    }

    @Override
    public void callDisconnected() {
        Intent intent = new Intent(getApplicationContext(), StartConfActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showError(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Call failed")
                .setMessage(error)
                .setPositiveButton("OK", (dialog, which) -> {
                    finish();
                })
                .setOnDismissListener(dialog -> {
                    finish();
                });
        builder.create().show();
    }

    @Override
    public void startForegroundService() {
        Intent intent = new Intent(getApplicationContext(), ConfService.class);
        intent.setAction(Constants.ACTION_FOREGROUND_SERVICE_START);
        startService(intent);
    }

    @Override
    public void stopForegroundService() {
        Intent intent = new Intent(getApplicationContext(), ConfService.class);
        intent.setAction(Constants.ACTION_FOREGROUND_SERVICE_STOP);
        stopService(intent);
    }

    private void showAudioDeviceSelectionDialog(List<String> audioDevices) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alert_select_audio_device)
                .setItems(audioDevices.toArray(new String[audioDevices.size()]), (dialog, which) -> {
                    if (audioDevices.get(which).equals(AudioDevice.EARPIECE.toString())) {
                        Voximplant.getAudioDeviceManager().selectAudioDevice(AudioDevice.EARPIECE);
                    } else if (audioDevices.get(which).equals(AudioDevice.SPEAKER.toString())) {
                        Voximplant.getAudioDeviceManager().selectAudioDevice(AudioDevice.SPEAKER);
                    } else if (audioDevices.get(which).equals(AudioDevice.WIRED_HEADSET.toString())) {
                        Voximplant.getAudioDeviceManager().selectAudioDevice(AudioDevice.WIRED_HEADSET);
                    } else if (audioDevices.get(which).equals(AudioDevice.BLUETOOTH.toString())) {
                        Voximplant.getAudioDeviceManager().selectAudioDevice(AudioDevice.BLUETOOTH);
                    }
                });
        builder.create().show();
    }
}
