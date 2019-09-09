/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.quality_issues.ui.call;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.voximplant.demos.quality_issues.R;
import com.voximplant.demos.quality_issues.ui.calls.MakeCallActivity;
import com.voximplant.sdk.Voximplant;
import com.voximplant.sdk.call.QualityIssueLevel;
import com.voximplant.sdk.hardware.AudioDevice;

import org.webrtc.SurfaceViewRenderer;

import java.util.List;

import static com.voximplant.demos.quality_issues.utils.Constants.CALL_ID;
import static com.voximplant.demos.quality_issues.utils.Constants.INCOMING_CALL;
import static com.voximplant.demos.quality_issues.utils.Constants.WITH_VIDEO;

public class CallActivity extends AppCompatActivity implements CallContract.View {

    private ImageButton mHangupButton;
    private ImageButton mCallControlsButton;
    private TextView mQualityIssuesLog;
    private HashMapArrayAdapter mCurrentIssuesAdapter;

    private ConstraintLayout mCallControlsArea;
    private ImageButton mSwitchCameraButton;
    private ImageButton mMuteAudioButton;
    private ImageButton mHoldButton;
    private ImageButton mAudioDeviceButton;
    private CheckBox mSendVideoCheckBox;
    private CheckBox mReceiveVideoCheckBox;

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

        mQualityIssuesLog = findViewById(R.id.issues_log_textview);
        mQualityIssuesLog.setMovementMethod(new ScrollingMovementMethod());
        GridView currentIssuesView = findViewById(R.id.current_issues_view);

        mLocalVideoView = findViewById(R.id.local_video_view);
        mRemoteVideoView = findViewById(R.id.remote_video_view);

        mCallControlsArea = findViewById(R.id.call_controls);
        mHoldButton = findViewById(R.id.hold_button);
        mSwitchCameraButton = findViewById(R.id.switch_camera_button);
        mMuteAudioButton = findViewById(R.id.mute_audio_button);
        mAudioDeviceButton = findViewById(R.id.audio_device_button);
        mSendVideoCheckBox = findViewById(R.id.send_video_checkbox);
        mReceiveVideoCheckBox = findViewById(R.id.receive_video_checkbox);

        mPresenter = new CallPresenter(this, callId, isIncomingCall, withVideo);
        mPresenter.start();

        mHangupButton = findViewById(R.id.hangup_button);
        mHangupButton.setOnTouchListener((v, event) -> {
            changeButton(mHangupButton, event.getAction(), true);
            return false;
        });
        mHangupButton.setOnClickListener(v -> mPresenter.stopCall());

        mCallControlsButton = findViewById(R.id.call_controls_button);
        mCallControlsButton.setOnTouchListener((v, event) -> {
            changeButton(mCallControlsButton, event.getAction(), false);
            return false;
        });
        mCallControlsButton.setOnClickListener(v -> mCallControlsArea.setVisibility(
                mCallControlsArea.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE));

        mHoldButton.setOnClickListener(v -> mPresenter.hold());
        mHoldButton.setOnTouchListener((v, event) -> {
            changeButton(mHoldButton, event.getAction(), false);
            return false;
        });

        mSwitchCameraButton.setOnClickListener(v -> mPresenter.switchCamera());
        mSwitchCameraButton.setOnTouchListener((v, event) -> {
            changeButton(mSwitchCameraButton, event.getAction(), false);
            return false;
        });

        mMuteAudioButton.setOnClickListener(v -> mPresenter.muteAudio());
        mMuteAudioButton.setOnTouchListener((v, event) -> {
            changeButton(mMuteAudioButton, event.getAction(), false);
            return false;
        });

        mAudioDeviceButton.setOnClickListener(v -> showAudioDeviceSelectionDialog(mPresenter.getAudioDevices()));
        mAudioDeviceButton.setOnTouchListener((v, event) -> {
            changeButton(mAudioDeviceButton, event.getAction(), false);
            return false;
        });

        mSendVideoCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> mPresenter.sendVideo(isChecked));
        mReceiveVideoCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> mPresenter.receiveVideo());

        mCurrentIssuesAdapter = new HashMapArrayAdapter(getApplicationContext(), mPresenter.getCurrentQualityIssues());
        currentIssuesView.setAdapter(mCurrentIssuesAdapter);
    }

    @Override
    public void updateMicButton(boolean pressed) {
        runOnUiThread(() -> changeButton(mMuteAudioButton, pressed ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_UP, false));
    }

    @Override
    public void updateHoldButton(boolean pressed) {
        runOnUiThread(() -> changeButton(mHoldButton, pressed ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_UP, false));
    }

    @Override
    public void updateSendVideoCheckbox(boolean checked) {
        runOnUiThread(() -> {
            if (mSendVideoCheckBox.isChecked() != checked) {
                mSendVideoCheckBox.setChecked(checked);
            }
        });
    }

    @Override
    public void updateReceiveVideoCheckbox(boolean checked) {
        runOnUiThread(() -> {
            if (mReceiveVideoCheckBox.isChecked() != checked) {
                mReceiveVideoCheckBox.setChecked(checked);
            }
            if (checked) {
                mReceiveVideoCheckBox.setEnabled(false);
            }
        });
    }

    @Override
    public void updateCameraButton(boolean isFront) {
        runOnUiThread(() -> {
            if (isFront) {
                mSwitchCameraButton.setImageResource(R.drawable.ic_camera_rear_black_35dp);
            } else {
                mSwitchCameraButton.setImageResource(R.drawable.ic_camera_front_black_35dp);
            }
        });
    }

    @Override
    public void updateAudioDeviceButton(AudioDevice audioDevice) {
        switch (audioDevice) {
            case EARPIECE:
                mAudioDeviceButton.setImageResource(R.drawable.ic_hearing_black_35dp);
                break;
            case SPEAKER:
                mAudioDeviceButton.setImageResource(R.drawable.ic_volume_up_black_35dp);
                break;
            case WIRED_HEADSET:
                mAudioDeviceButton.setImageResource(R.drawable.ic_headset_black_35dp);
                break;
            case BLUETOOTH:
                mAudioDeviceButton.setImageResource(R.drawable.ic_bluetooth_audio_black_35dp);
                break;
        }
    }

    @Override
    public void createLocalVideoView() {
        runOnUiThread(() -> mLocalVideoView.setVisibility(View.VISIBLE));
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

    @Override
    public void qualityIssueDetected(QualityIssueLevel level, String issue) {
        runOnUiThread(() -> {
            mCurrentIssuesAdapter.updateData(mPresenter.getCurrentQualityIssues());
            int color;
            switch (level) {
                case MINOR:
                    color = getResources().getColor(R.color.colorYellow);
                    break;
                case MAJOR:
                    color = getResources().getColor(R.color.colorOrange);
                    break;
                case CRITICAL:
                    color = getResources().getColor(R.color.colorRed);
                    break;
                case NONE:
                    default:
                    color = getResources().getColor(R.color.colorGreen);
                    break;
            }
            appendColoredText(issue + "\n", color);
        });
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
        runOnUiThread(() -> builder.create().show());
    }

    private void appendColoredText(String text, int color) {
        int start = mQualityIssuesLog.getText().length();
        mQualityIssuesLog.append(text);
        int end = mQualityIssuesLog.getText().length();

        Spannable spannable = (Spannable) mQualityIssuesLog.getText();
        spannable.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }

    private void changeButton(ImageButton button, int action, boolean isRed) {
        switch (action) {
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
}
