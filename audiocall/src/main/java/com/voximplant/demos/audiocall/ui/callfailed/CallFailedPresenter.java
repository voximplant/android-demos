/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.ui.callfailed;

import com.voximplant.demos.audiocall.Shared;
import com.voximplant.demos.audiocall.manager.VoxCallManager;

import java.lang.ref.WeakReference;

public class CallFailedPresenter implements CallFailedContract.Presenter {
    private WeakReference<CallFailedContract.View> mView;
    private VoxCallManager mCallManager = Shared.getInstance().getCallManager();

    CallFailedPresenter(CallFailedContract.View view) {
        mView = new WeakReference<>(view);
    }

    @Override
    public void start() {
    }

    @Override
    public void makeCall(String user) {
        CallFailedContract.View view = mView.get();
        if (view != null) {
            if (mCallManager.createCall(user)) {
                view.startCallActivity(user, false);
            }
        }
    }

    @Override
    public void cancelCall() {
        CallFailedContract.View view = mView.get();
        if (view != null) {
            view.startMainActivity();
        }
    }
}