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

    private WeakReference<MainContract.View> mView;
    private VoxClientManager mClientManager = Shared.getInstance().getClientManager();
    private VoxCallManager mCallManager = Shared.getInstance().getCallManager();

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
            view.notifyConnectionClosed();
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
        if (mCallManager.createCall(user)) {
            view.startCallActivity(user, false);
        }
    }


    @Override
    public void logout() {
        mClientManager.removeListener(this);
        mClientManager.logout();
    }
}
