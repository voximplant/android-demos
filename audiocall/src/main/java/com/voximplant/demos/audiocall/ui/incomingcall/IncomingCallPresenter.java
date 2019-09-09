/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.ui.incomingcall;

import android.util.Log;

import com.voximplant.demos.audiocall.Shared;
import com.voximplant.demos.audiocall.manager.VoxCallManager;
import com.voximplant.sdk.call.ICall;
import com.voximplant.sdk.call.ICallListener;

import java.lang.ref.WeakReference;
import java.util.Map;

import static com.voximplant.demos.audiocall.utils.Constants.APP_TAG;

public class IncomingCallPresenter implements IncomingCallContract.Presenter, ICallListener {
    private WeakReference<IncomingCallContract.View> mView;
    private WeakReference<ICall> mCall;
    private VoxCallManager mCallManager = Shared.getInstance().getCallManager();

    IncomingCallPresenter(IncomingCallContract.View view) {
        mView = new WeakReference<>(view);
        ICall call = mCallManager.getCall();
        if (call != null) {
            mCall = new WeakReference<>(call);
            call.addCallListener(this);
        }
    }

    @Override
    public void start() {
    }

    private void stop() {
        IncomingCallContract.View view = mView.get();
        ICall call = mCall.get();
        if (view != null && call != null) {
            call.removeCallListener(this);
            view.finishActivity();
        }
    }

    @Override
    public void answerCall() {
        IncomingCallContract.View view = mView.get();
        ICall call = mCall.get();
        if (view != null && call != null && !call.getEndpoints().isEmpty()) {
            view.startCallActivity(call.getEndpoints().get(0).getUserName(), false);
            call.removeCallListener(this);
        }
    }

    @Override
    public void rejectCall() {
        ICall call = mCall.get();
        if (call == null) {
            Log.e(APP_TAG, "IncomingCallPresenter: rejectCall: invalid call");
            return;
        }
        mCallManager.rejectCall(call);
    }

    @Override
    public void onCallFailed(ICall call, int code, String description, Map<String, String> headers) {
        stop();
    }

    @Override
    public void onCallDisconnected(ICall call, Map<String, String> headers, boolean answeredElsewhere) {
        stop();
    }

}
