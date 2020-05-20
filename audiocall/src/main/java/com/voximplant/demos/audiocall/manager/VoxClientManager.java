/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.manager;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.voximplant.demos.audiocall.utils.MD5;
import com.voximplant.demos.audiocall.utils.SharedPreferencesHelper;
import com.voximplant.sdk.client.AuthParams;
import com.voximplant.sdk.client.ClientState;
import com.voximplant.sdk.client.IClient;
import com.voximplant.sdk.client.IClientLoginListener;
import com.voximplant.sdk.client.IClientSessionListener;
import com.voximplant.sdk.client.LoginError;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.voximplant.demos.audiocall.utils.Constants.APP_TAG;
import static com.voximplant.demos.audiocall.utils.Constants.LOGIN_ACCESS_EXPIRE;
import static com.voximplant.demos.audiocall.utils.Constants.LOGIN_ACCESS_TOKEN;
import static com.voximplant.demos.audiocall.utils.Constants.LOGIN_REFRESH_EXPIRE;
import static com.voximplant.demos.audiocall.utils.Constants.LOGIN_REFRESH_TOKEN;
import static com.voximplant.demos.audiocall.utils.Constants.MILLISECONDS_IN_SECOND;
import static com.voximplant.demos.audiocall.utils.Constants.REFRESH_TIME;
import static com.voximplant.demos.audiocall.utils.Constants.USERNAME;
import static com.voximplant.sdk.client.ClientState.LOGGED_IN;

public class VoxClientManager implements IClientSessionListener, IClientLoginListener {

    private IClient mClient = null;
    private CopyOnWriteArrayList<IClientManagerListener> mListeners = new CopyOnWriteArrayList<>();
    private String mUsername = null;
    private String mPassword = null;
    private String mFireBaseToken;
    private String mDisplayName;

    public VoxClientManager(IClient client) {
        setClient(client);
        // uncomment this line to enable push notifications
//        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
//            if (!task.isSuccessful() || task.getResult() == null) {
//                return;
//            }
//            mFireBaseToken = task.getResult().getToken();
//        });
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    private void setClient(IClient client) {
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

    //region Login
    public void login(String username, String password) {
        mUsername = username;
        mPassword = password;
        if (mClient != null) {
            if (mClient.getClientState() == ClientState.DISCONNECTED) {
                try {
                    mClient.connect();
                } catch (IllegalStateException e) {
                    Log.e(APP_TAG, "exception on connect: " + e);
                }
            } else if (mClient.getClientState() == ClientState.CONNECTED) {
                mClient.login(username, password);
            }
        }
    }

    public void loginWithToken() {
        if (mClient != null) {
            if (mClient.getClientState() == LOGGED_IN && mDisplayName != null) {
                for (IClientManagerListener listener : mListeners) {
                    listener.onLoginSuccess(mDisplayName);
                }
            } else if (mClient.getClientState() == ClientState.DISCONNECTED) {
                try {
                    mClient.connect();
                } catch (IllegalStateException e) {
                    Log.e(APP_TAG, "exception on connect: " + e);
                }
            } else if (mClient.getClientState() == ClientState.CONNECTED) {
                if (loginTokensExist()) {
                    mUsername = SharedPreferencesHelper.get().getStringFromPrefs(USERNAME);
                    if (isTokenExpired(SharedPreferencesHelper.get().getLongFromPrefs(LOGIN_ACCESS_EXPIRE))) {
                        mClient.loginWithAccessToken(mUsername,
                                SharedPreferencesHelper.get().getStringFromPrefs(LOGIN_ACCESS_TOKEN));
                    } else if (isTokenExpired(SharedPreferencesHelper.get().getLongFromPrefs(LOGIN_REFRESH_EXPIRE))) {
                        mClient.refreshToken(mUsername,
                                SharedPreferencesHelper.get().getStringFromPrefs(LOGIN_REFRESH_TOKEN));
                    }
                }
            }
        }
    }

    private void loginWithKey(String username, String password) {
        mUsername = username;
        mPassword = password;
        if (mClient != null) {
            if (mClient.getClientState() == ClientState.DISCONNECTED) {
                try {
                    mClient.connect();
                } catch (IllegalStateException e) {
                    Log.e(APP_TAG, "loginWithKey: exception on connect: " + e);
                }
            }
            if (mClient.getClientState() == ClientState.CONNECTED) {
                mClient.requestOneTimeKey(username);
            }
        }
    }
    //endregion

    public void logout() {
        if (mClient != null && mClient.getClientState() == LOGGED_IN) {
            enablePushNotifications(false);
            mDisplayName = null;
            removeTokens();
            mClient.disconnect();
        }
    }


    //region Tokens
    private void saveAuthDetailsToSharedPreferences(AuthParams authParams) {
        SharedPreferencesHelper.get().saveToPrefs(REFRESH_TIME, System.currentTimeMillis());
        SharedPreferencesHelper.get().saveToPrefs(LOGIN_ACCESS_TOKEN, authParams.getAccessToken());
        SharedPreferencesHelper.get().saveToPrefs(LOGIN_ACCESS_EXPIRE, (long) authParams.getAccessTokenTimeExpired());
        SharedPreferencesHelper.get().saveToPrefs(LOGIN_REFRESH_TOKEN, authParams.getRefreshToken());
        SharedPreferencesHelper.get().saveToPrefs(LOGIN_REFRESH_EXPIRE, (long) authParams.getRefreshTokenTimeExpired());
    }

    private void removeTokens() {
        SharedPreferencesHelper.get().removeFromPrefs(REFRESH_TIME);
        SharedPreferencesHelper.get().removeFromPrefs(LOGIN_ACCESS_TOKEN);
        SharedPreferencesHelper.get().removeFromPrefs(LOGIN_ACCESS_EXPIRE);
        SharedPreferencesHelper.get().removeFromPrefs(LOGIN_REFRESH_TOKEN);
        SharedPreferencesHelper.get().removeFromPrefs(LOGIN_REFRESH_EXPIRE);
    }

    private boolean isTokenExpired(long lifeTime) {
        return System.currentTimeMillis() - SharedPreferencesHelper.get().getLongFromPrefs(REFRESH_TIME) <= lifeTime * MILLISECONDS_IN_SECOND;
    }

    public boolean loginTokensExist() {
        return SharedPreferencesHelper.get().getStringFromPrefs(LOGIN_ACCESS_TOKEN) != null &&
                SharedPreferencesHelper.get().getStringFromPrefs(LOGIN_REFRESH_TOKEN) != null;
    }
    //endregion

    //region PushNotifications
    public void firebaseTokenRefreshed(String token) {
        mFireBaseToken = token;
        enablePushNotifications(true);
    }

    public void pushNotificationReceived(Map<String, String> message) {
        mClient.handlePushNotification(message);
        loginWithToken();
    }

    private void enablePushNotifications(boolean enable) {
        if (enable) {
            mClient.registerForPushNotifications(mFireBaseToken, null);
        } else {
            mClient.unregisterFromPushNotifications(mFireBaseToken, null);
        }
    }
    //endregion

    //region IClientSessionListener
    @Override
    public void onConnectionEstablished() {
        if (mClient == null) {
            return;
        }
        if (mUsername != null && mPassword != null) {
            mClient.login(mUsername, mPassword);
        } else {
            loginWithToken();
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
    //endregion

    //region IClientLoginListener
    @Override
    public synchronized void onLoginSuccessful(String displayName, AuthParams authParams) {
        enablePushNotifications(true);
        mDisplayName = displayName;
        if (authParams != null) {
            saveAuthDetailsToSharedPreferences(authParams);
        }
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
    public void onOneTimeKeyGenerated(String key) {
        String hash = MD5.get(key + "|" + MD5.get(mUsername.substring(0, mUsername.indexOf("@")) + ":voximplant.com:" + mPassword));
        mClient.loginWithOneTimeKey(mUsername, hash);
    }

    @Override
    public void onRefreshTokenFailed(LoginError reason) {
        for (IClientManagerListener listener : mListeners) {
            listener.onLoginFailed(reason);
        }
    }

    @Override
    public void onRefreshTokenSuccess(AuthParams authParams) {
        saveAuthDetailsToSharedPreferences(authParams);
        loginWithToken();
    }
    //endregion
}
