/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.ui.main;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.voximplant.demos.audiocall.R;
import com.voximplant.demos.audiocall.ui.call.CallActivity;
import com.voximplant.demos.audiocall.ui.login.LoginActivity;
import com.voximplant.demos.audiocall.utils.ShareHelper;
import com.voximplant.sdk.Voximplant;

import java.util.ArrayList;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;
import static com.voximplant.demos.audiocall.utils.Constants.INCOMING_CALL;
import static com.voximplant.demos.audiocall.utils.Constants.USERNAME;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    private EditText mCallToView;
    private AlertDialog mAlertDialog;
    private TextView mLoggedInAsTextView;

    private MainContract.Presenter mPresenter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Animator reducer = AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.reduce_size);
        Animator increaser = AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.regain_size);

        ImageButton shareButton = findViewById(R.id.share_log_main);
        shareButton.setOnClickListener(v -> {
            Intent shareIntent = ShareHelper.getInstance().createShareIntent(this);
            if (shareIntent != null) {
                startActivity(shareIntent);
            }
        });

        mCallToView = findViewById(R.id.call_to);
        Button callButton = findViewById(R.id.start_call_button);
        callButton.setOnClickListener(v -> {
            if (checkPermissionsGrantedForCall()) {
                mPresenter.makeCall(mCallToView.getText().toString());
                hideKeyboard(v);
            }
        });
        callButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                reducer.setTarget(v);
                reducer.start();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                increaser.setTarget(v);
                increaser.start();
            }
            return false;
        });

        ImageButton logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> mPresenter.logout());
        mLoggedInAsTextView = findViewById(R.id.logged_in_label);

        mPresenter = new MainPresenter(this);
        mPresenter.start();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void showMyDisplayName(String displayName) {
        mLoggedInAsTextView.setText(getString(R.string.logged_in_as) + displayName);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
    }

    //region Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.RECORD_AUDIO) && grantResults[i] == PERMISSION_GRANTED) {
                    mPresenter.makeCall(mCallToView.getText().toString());
                }
            }
        }
    }

    private boolean checkPermissionsGrantedForCall() {
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
    public void notifyConnectionClosed() {
        showAlertDialog(R.string.alert_title_disconnected, R.string.alert_content_disconnected, false);
    }

    @Override
    public void notifyInvalidCallUser() {
        mCallToView.setError(getString(R.string.error_field_required));
        mCallToView.requestFocus();
    }

    @Override
    public void notifyLogoutCompleted() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void notifyCannotMakeCall() {
        showAlertDialog(R.string.alert_title_disconnected, R.string.alert_content_cannot_make_call, false);
    }

    @Override
    public void startCallActivity(String user, boolean isIncoming) {
        Intent intent = new Intent(getApplicationContext(), CallActivity.class);
        intent.putExtra(USERNAME, user);
        intent.putExtra(INCOMING_CALL, isIncoming);
        startActivity(intent);
    }

    private void showAlertDialog(int resTitle, int resContent, boolean startLoginActivity) {
        runOnUiThread(() -> mAlertDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle(resTitle)
                .setMessage(resContent)
                .setPositiveButton(R.string.alert_positive_button, (dialog, which) -> {
                    if (startLoginActivity) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .show());
    }

    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}
