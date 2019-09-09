/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.ui.callfailed;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.voximplant.demos.audiocall.R;
import com.voximplant.demos.audiocall.ui.call.CallActivity;
import com.voximplant.demos.audiocall.ui.main.MainActivity;
import com.voximplant.demos.audiocall.utils.UIHelper;

import static com.voximplant.demos.audiocall.utils.Constants.DISPLAY_NAME;
import static com.voximplant.demos.audiocall.utils.Constants.REASON;
import static com.voximplant.demos.audiocall.utils.Constants.USERNAME;

public class CallFailedActivity extends AppCompatActivity implements CallFailedContract.View {

    private String mUsername;
    private CallFailedContract.Presenter mPresenter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_failed);

        Intent intent = getIntent();
        String reason = intent.getStringExtra(REASON);
        String displayName = intent.getStringExtra(DISPLAY_NAME);
        mUsername = intent.getStringExtra(USERNAME);

        ImageButton cancelButton = findViewById(R.id.cancel_call_button);
        cancelButton.setOnClickListener(v -> mPresenter.cancelCall());
        cancelButton.setOnTouchListener((v, event) -> {
            UIHelper.changeButtonColor(this, cancelButton, event.getAction(), true);
            return false;
        });
        ImageButton callBackButton = findViewById(R.id.call_back_button);
        callBackButton.setOnClickListener(v -> mPresenter.makeCall(mUsername));
        callBackButton.setOnTouchListener((v, event) -> {
            UIHelper.changeButtonColor(this, callBackButton, event.getAction(), false);
            return false;
        });
        TextView statusView = findViewById(R.id.call_failed_status);
        statusView.setText(reason);
        TextView displayNameView = findViewById(R.id.caller_name_call_failed);
        displayNameView.setText(displayName);
        mPresenter = new CallFailedPresenter(this);
    }

    @Override
    public void onBackPressed() {
        startMainActivity();
    }

    @Override
    public void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    @Override
    public void startCallActivity(String user, boolean isIncoming) {
        Intent intent = new Intent(getApplicationContext(), CallActivity.class);
        intent.putExtra(USERNAME, user);
        startActivity(intent);
        finish();
    }
}
