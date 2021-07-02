/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.ui.call;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.voximplant.demos.audiocall.R;
import com.voximplant.demos.audiocall.ui.callfailed.CallFailedActivity;
import com.voximplant.demos.audiocall.ui.main.MainActivity;
import com.voximplant.demos.audiocall.utils.UIHelper;
import com.voximplant.sdk.Voximplant;
import com.voximplant.sdk.hardware.AudioDevice;

import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.voximplant.demos.audiocall.utils.Constants.DISPLAY_NAME;
import static com.voximplant.demos.audiocall.utils.Constants.INCOMING_CALL;
import static com.voximplant.demos.audiocall.utils.Constants.REASON;
import static com.voximplant.demos.audiocall.utils.Constants.USERNAME;

public class CallActivity extends AppCompatActivity implements CallContract.View {

    private ImageButton mMuteAudioButton;
    private ImageButton mHoldButton;
    private ImageButton mAudioDeviceButton;
    private ImageButton mKeypadButton;

    private TextView mMuteAudioTextView;
    private TextView mHoldTextView;
    private TextView mDisplayNameView;
    private TextView mCallStatusTextView;
    private TextView mCallTimerTextView;

    private ConstraintLayout mKeypadView;

    private String mUsername;
    private String mDisplayName;

    // to replace text in mDisplayNameView with DTMF symbols
    private boolean mShouldClearTextView = true;

    private CallContract.Presenter mCallPresenter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        mDisplayNameView = findViewById(R.id.caller_name_view);
        mCallStatusTextView = findViewById(R.id.call_status_view);
        mCallTimerTextView = findViewById(R.id.call_timer_text);
        mKeypadView = findViewById(R.id.keypad_view);

        ImageButton hangupButton = findViewById(R.id.hangup_button);
        hangupButton.setOnClickListener(v -> mCallPresenter.stopCall());
        hangupButton.setOnTouchListener((v, event) -> {
            UIHelper.changeButtonColor(this, hangupButton, event.getAction(), true);
            return false;
        });

        mMuteAudioButton = findViewById(R.id.mute_audio_button);
        mMuteAudioTextView = findViewById(R.id.mute_text_view);
        mMuteAudioButton.setOnClickListener(v -> mCallPresenter.muteAudio());
        mMuteAudioButton.setOnTouchListener((v, event) -> {
            UIHelper.changeButtonColor(this, mMuteAudioButton, event.getAction(), false);
            return false;
        });

        mHoldButton = findViewById(R.id.hold_button);
        mHoldTextView = findViewById(R.id.hold_text_view);
        mHoldButton.setOnClickListener(v -> mCallPresenter.hold());
        mHoldButton.setOnTouchListener((v, event) -> {
            UIHelper.changeButtonColor(this, mHoldButton, event.getAction(), false);
            return false;
        });

        mKeypadButton = findViewById(R.id.keypad_button);
        mKeypadButton.setOnClickListener(v -> showKeypad(true));
        mKeypadButton.setOnTouchListener((v, event) -> {
            UIHelper.changeButtonColor(this, mKeypadButton, event.getAction(), false);
            return false;
        });

        Intent intent = getIntent();
        boolean isIncomingCall = intent.getBooleanExtra(INCOMING_CALL, false);
        mUsername = intent.getStringExtra(USERNAME);
        mDisplayName = intent.getStringExtra(DISPLAY_NAME);
        mCallPresenter = new CallPresenter(this, isIncomingCall);
        mCallPresenter.start();

        updateDisplayName(mDisplayName == null ? mUsername : mDisplayName);

        mAudioDeviceButton = findViewById(R.id.audio_device_button);
        mAudioDeviceButton.setOnClickListener(v -> showAudioDeviceSelectionDialog(mCallPresenter.getAudioDevices()));
        mAudioDeviceButton.setOnTouchListener((v, event) -> {
            UIHelper.changeButtonColor(this, mAudioDeviceButton, event.getAction(), false);
            return false;
        });

        enableButtons(false);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void updateDisplayName(String displayName) {
        runOnUiThread(() -> {
            mDisplayName = displayName;
            mDisplayNameView.setText(displayName);
        });
    }

    @Override
    public void updateTimer(String time) {
        runOnUiThread(() -> mCallTimerTextView.setText(time));
    }

    @Override
    public void updateCallStatus(int resStatus) {
        runOnUiThread(() -> mCallStatusTextView.setText(resStatus));
    }

    @Override
    public void enableButtons(boolean enabled) {
        runOnUiThread(() -> {
            double alpha = enabled ? 1.0 : 0.5;
            mHoldButton.setAlpha((float) alpha);
            mKeypadButton.setAlpha((float) alpha);
            mHoldButton.setEnabled(enabled);
            mKeypadButton.setEnabled(enabled);
        });
    }

    @Override
    public void updateMicButton(boolean pressed) {
        runOnUiThread(() -> {
            mMuteAudioButton.setImageResource(pressed ? R.drawable.ic_mic_black_40dp : R.drawable.ic_mic_off_black_40dp);
            mMuteAudioTextView.setText(pressed ? R.string.unmute : R.string.mute);
            UIHelper.changeButtonColor(this, mMuteAudioButton, pressed ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_UP, false);
        });
    }

    @Override
    public void updateHoldButton(boolean pressed) {
        runOnUiThread(() -> {
            mHoldButton.setImageResource(pressed ? R.drawable.ic_play_arrow_black_40dp : R.drawable.ic_pause_black_40dp);
            mHoldTextView.setText(pressed ? R.string.resume : R.string.hold);
        });
    }

    @Override
    public void updateAudioDeviceButton(AudioDevice audioDevice) {
        switch (audioDevice) {
            case EARPIECE:
                mAudioDeviceButton.setImageResource(R.drawable.ic_hearing_black_40dp);
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

    private void showAudioDeviceSelectionDialog(List<String> audioDevices) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alert_select_audio_device)
                .setItems(audioDevices.toArray(new String[0]), (dialog, which) -> {
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

    @Override
    public void callDisconnected() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void callFailed(String description) {
        Intent intent = new Intent(getApplicationContext(), CallFailedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(REASON, description);
        intent.putExtra(DISPLAY_NAME, mDisplayNameView.getText().toString());
        intent.putExtra(USERNAME, mUsername);
        startActivity(intent);
        finish();
    }

    public void onDTMFSymbolPressed(View view) {
        String symbol = ((Button) view).getText().toString();
        runOnUiThread(() -> {
            if (mShouldClearTextView) {
                mDisplayNameView.setText(symbol);
            } else {
                mDisplayNameView.setText(mDisplayNameView.getText().length() < 15
                        ? mDisplayNameView.getText() + symbol
                        : mDisplayNameView.getText().toString().substring(1) + symbol);
            }
        });
        mShouldClearTextView = false;
        mCallPresenter.sendDTMF(symbol);
    }

    public void onHideKeypadPressed(View view) {
        mDisplayNameView.setText(mDisplayName);
        mShouldClearTextView = true;
        showKeypad(false);
    }

    private void showKeypad(boolean show) {
        mMuteAudioButton.setEnabled(!show);
        mKeypadButton.setEnabled(!show);
        mAudioDeviceButton.setEnabled(!show);
        mHoldButton.setEnabled(!show);
        mKeypadView.setVisibility(show ? VISIBLE : INVISIBLE);
    }

    @Override
    public void showError(int resError, String message, String code) {
        String error = getString(resError, message, code);
        runOnUiThread(() -> Toast.makeText(this, error, Toast.LENGTH_SHORT).show());
    }
}
