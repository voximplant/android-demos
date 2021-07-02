/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.ui.main;

import com.voximplant.demos.audiocall.Shared;
import com.voximplant.demos.audiocall.manager.IClientManagerListener;
import com.voximplant.demos.audiocall.manager.VoxCallManager;
import com.voximplant.demos.audiocall.manager.VoxClientManager;

import java.lang.ref.WeakReference;

public class MainPresenter implements MainContract.Presenter, IClientManagerListener {

    private final WeakReference<MainContract.View> mView;
    private final VoxClientManager mClientManager = Shared.getInstance().getClientManager();
    private final VoxCallManager mCallManager = Shared.getInstance().getCallManager();
    private boolean mWaitingForLogout;
    private String mUserToCall;

    MainPresenter(MainContract.View view) {
        mView = new WeakReference<>(view);
    }

    @Override
    public void start() {
        Shared.getInstance().getClientManager().addListener(this);
        String displayName = mClientManager.getDisplayName();
        MainContract.View view = mView.get();
        if (displayName != null && view != null) {
            view.showMyDisplayName(displayName);
        }
    }

    @Override
    public void onConnectionClosed() {
        MainContract.View view = mView.get();
        if (view != null) {
            if (mWaitingForLogout) {
                mWaitingForLogout = false;
                mClientManager.removeListener(this);
                view.notifyLogoutCompleted();
            } else {
                view.notifyConnectionClosed();
            }
        }
    }

    @Override
    public void makeCall(String user) {
        MainContract.View view = mView.get();
        if (view == null) {
            return;
        }
        if (user == null || user.isEmpty()) {
            view.notifyInvalidCallUser();
            return;
        }
        if (mClientManager.getDisplayName() == null) {
            mClientManager.loginWithToken();
            mUserToCall = user;

        } else if (mCallManager.createCall(user)) {
            mUserToCall = null;
            view.startCallActivity(user, false);
        }
    }

    @Override
    public void logout() {
        mClientManager.logout();
        mWaitingForLogout = true;
    }

    @Override
    public void onLoginSuccess(String displayName) {
        if (mUserToCall != null) {
            makeCall(mUserToCall);
        }
    }

    @Override
    public void onConnectionFailed() {
        MainContract.View view = mView.get();
        if (view != null) {
            view.notifyCannotMakeCall();
        }
    }
}
