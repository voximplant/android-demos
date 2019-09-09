/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.ui.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.voximplant.demos.audiocall.R;
import com.voximplant.demos.audiocall.ui.main.MainActivity;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    private EditText mLoginView;
    private EditText mPasswordView;
    private View mProgressView;
    private ConstraintLayout mLoginButtonsContainerLayout;
    private ConstraintLayout mTokenButtonsContainerLayout;
    private LoginContract.Presenter mLoginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle(getResources().getString(R.string.voximplant));

        mLoginView = findViewById(R.id.email);
        mProgressView = findViewById(R.id.login_progress);
        mLoginButtonsContainerLayout = findViewById(R.id.login_buttons_layout);
        mTokenButtonsContainerLayout = findViewById(R.id.login_with_token_elements_layout);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == 100 || i == EditorInfo.IME_NULL) {
                hideKeyboard(textView);
                mLoginPresenter.loginWithPassword(mLoginView.getText().toString(), mPasswordView.getText().toString());
                return true;
            }
            return false;
        });

        Button loginButton = findViewById(R.id.email_sign_in_button);
        loginButton.setOnClickListener(view -> {
            hideKeyboard(view);
            mLoginPresenter.loginWithPassword(mLoginView.getText().toString(), mPasswordView.getText().toString());
        });
        Button loginWithTokenButton = findViewById(R.id.email_sign_in_key_button);
        loginWithTokenButton.setOnClickListener(view -> {
            hideKeyboard(view);
            mLoginPresenter.loginWithAccessToken(mLoginView.getText().toString() + ".voximplant.com");
        });

        mLoginPresenter = new LoginPresenter(this);
        mLoginPresenter.start();
        mLoginPresenter.checkIfTokensExist();
    }

    @Override
    public void setTokenViewVisibility(boolean visible) {
        if (visible) {
            mTokenButtonsContainerLayout.setVisibility(VISIBLE);
            mTokenButtonsContainerLayout.setEnabled(true);
        } else {
            mTokenButtonsContainerLayout.setVisibility(INVISIBLE);
            mTokenButtonsContainerLayout.setEnabled(false);
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void fillUsername(String username) {
        runOnUiThread(() -> mLoginView.setText(username));
    }

    @Override
    public void showProgress(boolean show) {
        runOnUiThread(() -> {
            mLoginButtonsContainerLayout.setVisibility(show ? INVISIBLE : VISIBLE);

            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mProgressView.setVisibility(show ? VISIBLE : View.GONE);
            mProgressView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mProgressView.setVisibility(show ? VISIBLE : View.GONE);
                        }
                    });
        });
    }

    @Override
    public void loginSuccess(String displayName) {
        mLoginPresenter.stop();
        runOnUiThread(() -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void usernameInvalid(int error) {
        runOnUiThread(() -> {
            mLoginView.setError(getString(error));
            mLoginView.requestFocus();
        });
    }

    @Override
    public void passwordInvalid(int error) {
        runOnUiThread(() -> {
            mPasswordView.setError(getString(error));
            mPasswordView.requestFocus();
        });
    }

    @Override
    public void showError(int error) {
        runOnUiThread(() -> {
            int titleRes;
            if (error == R.string.alert_content_connection_failed) {
                titleRes = R.string.alert_title_connection_failed;
            } else {
                titleRes = R.string.alert_login_failed;
            }

            new AlertDialog.Builder(this)
                    .setTitle(getString(titleRes))
                    .setMessage(getString(error))
                    .setPositiveButton(R.string.alert_positive_button, (dialog, which) -> {
                    })
                    .show();
        });
    }
}
