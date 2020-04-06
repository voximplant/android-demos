/*
 * Copyright (c) 2011- 2020, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.videoconf.ui.connecting;

import com.voximplant.demos.videoconf.Shared;
import com.voximplant.demos.videoconf.manager.IClientManagerListener;
import com.voximplant.demos.videoconf.manager.VoxClientManager;

import java.lang.ref.WeakReference;

public class ConnectingPresenter implements ConnectingContract.Presenter, IClientManagerListener {
    private final WeakReference<ConnectingContract.View> mView;
    private VoxClientManager mClientManager = Shared.getInstance().getClientManager();

    ConnectingPresenter(ConnectingContract.View view) {
        mView = new WeakReference<>(view);
    }

    @Override
    public void start() {
        mClientManager.setClientManagerListener(this);
    }


    @Override
    public void loginUser(String meetingId, String displayName) {
        mClientManager.loginUser(meetingId, displayName);
    }

    @Override
    public void onLoginSuccessful() {
        ConnectingContract.View view = mView.get();
        if (view != null) {
            view.navigateToConfScreen();
        }
    }

    @Override
    public void onLoginFailed(String error) {
        ConnectingContract.View view = mView.get();
        if (view != null) {
            view.showError(error);
        }
    }

    @Override
    public void onConnectionClosed() {
        ConnectingContract.View view = mView.get();
        if (view != null) {
            view.showError("Please check your internet connection and try again");
        }
    }
}
