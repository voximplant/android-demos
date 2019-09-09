/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.voximplant.demos.audiocall.utils.UIHelper;
import com.voximplant.sdk.Voximplant;

import java.util.ArrayList;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;
import static com.voximplant.demos.audiocall.utils.Constants.INCOMING_CALL;
import static com.voximplant.demos.audiocall.utils.Constants.USERNAME;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    private EditText mCallToView;
    private ImageButton mCallButton;
    private AlertDialog mAlertDialog;
    private TextView mLoggedInAsTextView;

    private MainContract.Presenter mPresenter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCallToView = findViewById(R.id.call_to);
        mCallButton = findViewById(R.id.button_audio_call);
        mCallButton.setOnClickListener(v -> {
            if (checkPermissionsGrantedForCall()) {
                mPresenter.makeCall(mCallToView.getText().toString());
                hideKeyboard(v);
            }
        });
        mCallButton.setOnTouchListener((v, event) -> {
            UIHelper.changeButtonColor(this, mCallButton, event.getAction(), false);
            return false;
        });
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

    //region AppBar menu
    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_log_out) {
            showAlertDialog(R.string.alert_title_logout, R.string.alert_content_logout);
        }
        return super.onOptionsItemSelected(item);
    }
    //endregion

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
        showAlertDialog(R.string.alert_title_disconnected, R.string.alert_content_disconnected);
    }

    @Override
    public void notifyInvalidCallUser() {
        mCallToView.setError(getString(R.string.error_field_required));
        mCallToView.requestFocus();
    }

    @Override
    public void startCallActivity(String user, boolean isIncoming) {
        Intent intent = new Intent(getApplicationContext(), CallActivity.class);
        intent.putExtra(USERNAME, user);
        intent.putExtra(INCOMING_CALL, isIncoming);
        startActivity(intent);
    }

    private void showAlertDialog(int resTitle, int resContent) {
        runOnUiThread(() -> mAlertDialog = new AlertDialog.Builder(MainActivity.this)
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

    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}
