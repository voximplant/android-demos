/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.ui.incomingcall;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.voximplant.demos.audiocall.R;
import com.voximplant.demos.audiocall.ui.call.CallActivity;
import com.voximplant.demos.audiocall.utils.UIHelper;
import com.voximplant.sdk.Voximplant;

import java.util.ArrayList;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;
import static com.voximplant.demos.audiocall.utils.Constants.CALL_ANSWERED;
import static com.voximplant.demos.audiocall.utils.Constants.DISPLAY_NAME;
import static com.voximplant.demos.audiocall.utils.Constants.INCOMING_CALL;
import static com.voximplant.demos.audiocall.utils.Constants.USERNAME;

public class IncomingCallActivity extends AppCompatActivity implements IncomingCallContract.View {

    private IncomingCallContract.Presenter mPresenter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_incoming_call);

        mPresenter = new IncomingCallPresenter(this);

        ImageButton answerWithAudio = findViewById(R.id.answer_call_button);
        answerWithAudio.setOnTouchListener((View v, MotionEvent event) -> {
            UIHelper.changeButtonColor(this, answerWithAudio, event.getAction(), false);
            return false;
        });
        answerWithAudio.setOnClickListener(view -> {
            if (permissionsGrantedForCall()) {
                mPresenter.answerCall();
            }
        });

        ImageButton reject = findViewById(R.id.decline_call_button);
        reject.setOnTouchListener((View v, MotionEvent event) -> {
            UIHelper.changeButtonColor(this, reject, event.getAction(), true);
            return false;
        });
        reject.setOnClickListener(view -> mPresenter.rejectCall());

        // Used with Android Q and notifications
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            TextView callFrom = findViewById(R.id.incoming_call_from);
            callFrom.setText(extras.getString(DISPLAY_NAME));
            if (extras.getBoolean(CALL_ANSWERED)) {
                if (permissionsGrantedForCall()) {
                    mPresenter.answerCall();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        mPresenter.rejectCall();
        finishActivity();
    }

    //region Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.RECORD_AUDIO) && grantResults[i] == PERMISSION_GRANTED) {
                    mPresenter.answerCall();
                }
            }
        }
    }

    private boolean permissionsGrantedForCall() {
        ArrayList<String> missingPermissions = (ArrayList<String>) Voximplant.getMissingPermissions(getApplicationContext(), false);
        if (missingPermissions.size() == 0) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, missingPermissions.toArray(new String[0]), PERMISSION_GRANTED);
            return false;
        }
    }
    //endregion

    @Override
    public void startCallActivity(String user, boolean isIncoming) {
        Intent intent = new Intent(getApplicationContext(), CallActivity.class);
        intent.putExtra(USERNAME, user);
        intent.putExtra(INCOMING_CALL, true);
        startActivity(intent);
        finishActivity();
    }

    @Override
    public void finishActivity() {
        finish();
    }
}
