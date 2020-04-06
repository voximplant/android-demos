/*
 * Copyright (c) 2011- 2020, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.videoconf.manager;

import android.util.Log;

import com.voximplant.sdk.call.CallSettings;
import com.voximplant.sdk.call.ICall;
import com.voximplant.sdk.call.VideoCodec;
import com.voximplant.sdk.call.VideoFlags;
import com.voximplant.sdk.client.AuthParams;
import com.voximplant.sdk.client.ClientState;
import com.voximplant.sdk.client.IClient;
import com.voximplant.sdk.client.IClientLoginListener;
import com.voximplant.sdk.client.IClientSessionListener;
import com.voximplant.sdk.client.LoginError;

import static com.voximplant.demos.videoconf.Constants.APP_TAG;
import static com.voximplant.demos.videoconf.Constants.MEETING_PREFIX;
import static com.voximplant.demos.videoconf.Constants.VOX_ACCOUNT;

public class VoxClientManager implements IClientSessionListener, IClientLoginListener {
    private final IClient mClient;
    private final NetworkManager mNetworkManager;
    private String mUsername = null;
    private String mPassword = null;
    private String mDisplayName = null;
    private IClientManagerListener mListener;

    public VoxClientManager(IClient client) {
        mClient = client;
        mNetworkManager = new NetworkManager();
        mClient.setClientSessionListener(this);
        mClient.setClientLoginListener(this);
    }

    public void setClientManagerListener(IClientManagerListener listener) {
        mListener = listener;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void loginUser(String meetingId, String username) {
        mDisplayName = username;
        mNetworkManager.requestLoginCredentials(meetingId, username, new NetworkManager.ICredentialsRequestResult() {
            @Override
            public void onSuccess(String login, String password) {
                login(login + VOX_ACCOUNT, password);
            }

            @Override
            public void onFailure() {
                Log.e(APP_TAG, "VoxClientManager: failed to get login credentials");
                if (mListener != null) {
                    mListener.onConnectionClosed();
                }
            }
        });
    }

    public void disconnect() {
        mUsername = null;
        mPassword = null;
        mDisplayName = null;
        mClient.disconnect();
    }

    private void login(String username, String password) {
        mUsername = username;
        mPassword = password;
        if (mClient != null) {
            if (mClient.getClientState() == ClientState.DISCONNECTED) {
                try {
                    mClient.connect();
                } catch (IllegalStateException e) {
                    Log.e(APP_TAG, "login: exception on connect: " + e);
                }
            }
            if (mClient.getClientState() == ClientState.CONNECTED) {
                mClient.login(username, password);
            }
        }
    }

    public ICall createConferenceCall(String meetingId) {
        CallSettings callSettings = new CallSettings();
        callSettings.videoFlags = new VideoFlags(true, true);
        callSettings.preferredVideoCodec = VideoCodec.VP8;
        return mClient.callConference(MEETING_PREFIX + meetingId, callSettings);
    }

    @Override
    public void onLoginSuccessful(String s, AuthParams authParams) {
        if (mListener != null) {
            mListener.onLoginSuccessful();
        }
    }

    @Override
    public void onLoginFailed(LoginError loginError) {
        if (mListener != null) {
            mListener.onLoginFailed(loginError.toString());
        }
    }

    @Override
    public void onRefreshTokenFailed(LoginError loginError) {

    }

    @Override
    public void onRefreshTokenSuccess(AuthParams authParams) {

    }

    @Override
    public void onOneTimeKeyGenerated(String s) {

    }

    @Override
    public void onConnectionEstablished() {
        if (mClient == null) {
            return;
        }
        if (mUsername != null && mPassword != null) {
            mClient.login(mUsername, mPassword);
        } else {
            Log.e(APP_TAG, "VoxClientManager: onConnectionEstablished: username or password is null");
        }
    }

    @Override
    public void onConnectionFailed(String error) {
        Log.e(APP_TAG, "VoxClientManager: onConnectionFailed: " + error);
        if (mListener != null) {
            mListener.onConnectionClosed();
        }
    }

    @Override
    public void onConnectionClosed() {
        if (mListener != null) {
            mListener.onConnectionClosed();
        }
    }
}
