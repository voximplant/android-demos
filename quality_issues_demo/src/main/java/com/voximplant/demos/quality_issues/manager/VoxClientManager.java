/*
 * Copyright (c) 2017, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.quality_issues.manager;

import android.util.Log;

import com.voximplant.sdk.client.AuthParams;
import com.voximplant.sdk.client.ClientState;
import com.voximplant.sdk.client.IClient;
import com.voximplant.sdk.client.IClientLoginListener;
import com.voximplant.sdk.client.IClientSessionListener;
import com.voximplant.sdk.client.LoginError;
import com.voximplant.demos.quality_issues.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.voximplant.demos.quality_issues.utils.Constants.APP_TAG;

import static com.voximplant.demos.quality_issues.utils.Constants.USERNAME;

public class VoxClientManager implements IClientSessionListener, IClientLoginListener {
    private IClient mClient = null;
    private CopyOnWriteArrayList<IClientManagerListener> mListeners = new CopyOnWriteArrayList<>();

    private String mUsername = null;
    private String mPassword = null;
    private ArrayList<String> mServers = new ArrayList<>();

    public VoxClientManager() {

    }

    public void setClient(IClient client) {
        mClient = client;
        mClient.setClientLoginListener(this);
        mClient.setClientSessionListener(this);
    }

    synchronized public void addListener(IClientManagerListener listener) {
        mListeners.add(listener);
    }

    synchronized public void removeListener(IClientManagerListener listener) {
        mListeners.remove(listener);
    }

    public void login(String username, String password) {
        mUsername = username;
        mPassword = password;
        if (mClient != null) {
            if (mClient.getClientState() == ClientState.DISCONNECTED) {
                try {
                    mClient.connect(false, mServers);
                } catch (IllegalStateException e) {
                    Log.e(APP_TAG, "exception on connect: " + e);
                }
            }
            if (mClient.getClientState() == ClientState.CONNECTED) {
                mClient.login(username, password);
            }
        }
    }

    public void logout() {
        if (mClient.getClientState() == ClientState.LOGGED_IN && mClient != null) {
            mClient.disconnect();
        }
    }

    @Override
    public void onConnectionEstablished() {
        if (mClient != null && mUsername != null && mPassword != null) {
            mClient.login(mUsername, mPassword);
        }
    }

    @Override
    public synchronized void onConnectionFailed(String error) {
        for (IClientManagerListener listener : mListeners) {
            listener.onConnectionFailed();
        }
    }

    @Override
    public synchronized void onConnectionClosed() {
        for (IClientManagerListener listener : mListeners) {
            listener.onConnectionClosed();
        }
    }

    @Override
    public synchronized void onLoginSuccessful(String displayName, AuthParams authParams) {
        SharedPreferencesHelper.get().saveToPrefs(USERNAME, mUsername);
        for (IClientManagerListener listener : mListeners) {
            listener.onLoginSuccess(displayName);
        }
    }

    @Override
    public synchronized void onLoginFailed(LoginError reason) {
        for (IClientManagerListener listener : mListeners) {
            listener.onLoginFailed(reason);
        }
    }

    @Override
    public void onOneTimeKeyGenerated(String key) { }

    @Override
    public void onRefreshTokenFailed(LoginError reason) { }

    @Override
    public void onRefreshTokenSuccess(AuthParams authParams) { }

}
