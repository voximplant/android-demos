/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.quality_issues.ui.incomingcall;

import android.util.Log;

import com.voximplant.sdk.call.CallException;
import com.voximplant.sdk.call.ICall;
import com.voximplant.sdk.call.RejectMode;
import com.voximplant.demos.quality_issues.Shared;
import com.voximplant.demos.quality_issues.manager.ICallEventsListener;
import com.voximplant.demos.quality_issues.manager.VoxCallManager;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static com.voximplant.demos.quality_issues.utils.Constants.APP_TAG;

public class IncomingCallPresenter implements IncomingCallContract.Presenter, ICallEventsListener {
    private final WeakReference<IncomingCallContract.View> mView;
    private WeakReference<ICall> mCall;
    private final VoxCallManager mCallManager = Shared.getInstance().getCallManager();
    private final HashMap<String, String> mHeaders = null;


    IncomingCallPresenter(IncomingCallContract.View view, String callId) {
        mView = new WeakReference<>(view);
        if (callId != null && mCallManager != null) {
            mCall = new WeakReference<>(mCallManager.getCallById(callId));
            mCallManager.addCallEventListener(callId, this);
        } else {
            Log.e(APP_TAG, "IncomingCallPresenter: failed to get call by id");
        }
    }

    @Override
    public void start() {
    }

    private void stop() {
        ICall call = mCall.get();
        if (call != null) {
            mCallManager.removeCallEventListener(call.getCallId(), this);
            IncomingCallContract.View view = mView.get();
            if (view != null) {
                view.onCallEnded(call.getCallId());
            }
        }
    }

    @Override
    public boolean isVideoCall() {
        return mCall.get() != null && mCall.get().isVideoEnabled();
    }

    @Override
    public String getCallId() {
        return mCall != null ? mCall.get().getCallId() : null;
    }

    @Override
    public void answerCall() {
        ICall call = mCall.get();
        if (call != null) {
            mCallManager.removeCallEventListener(call.getCallId(), this);
        }
    }

    @Override
    public void rejectCall() {
        ICall call = mCall.get();
        if (call == null) {
            Log.e(APP_TAG, "IncomingCallPresenter: rejectCall: invalid call");
            return;
        }
        try {
            call.reject(RejectMode.DECLINE, mHeaders);
        } catch (CallException e) {
            Log.e(APP_TAG, "IncomingCallPresenter: reject call exception: " + e.getMessage());
            stop();
        }
    }

    @Override
    public void onCallFailed(int code, String description, Map<String, String> headers) {
        stop();
    }

    @Override
    public void onCallDisconnected(Map<String, String> headers, boolean answeredElsewhere) {
        stop();
    }

}
