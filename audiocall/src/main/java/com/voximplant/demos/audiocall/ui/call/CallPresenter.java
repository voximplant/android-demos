/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.ui.call;

import android.text.format.DateFormat;
import android.util.Log;

import com.voximplant.demos.audiocall.R;
import com.voximplant.demos.audiocall.Shared;
import com.voximplant.sdk.Voximplant;
import com.voximplant.sdk.call.CallException;
import com.voximplant.sdk.call.CallSettings;
import com.voximplant.sdk.call.ICall;
import com.voximplant.sdk.call.ICallCompletionHandler;
import com.voximplant.sdk.call.ICallListener;
import com.voximplant.sdk.call.IEndpoint;
import com.voximplant.sdk.call.IEndpointListener;
import com.voximplant.sdk.call.VideoFlags;
import com.voximplant.sdk.hardware.AudioDevice;
import com.voximplant.sdk.hardware.IAudioDeviceEventsListener;
import com.voximplant.sdk.hardware.IAudioDeviceManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.voximplant.demos.audiocall.utils.Constants.APP_TAG;
import static java.util.concurrent.TimeUnit.SECONDS;

public class CallPresenter implements CallContract.Presenter, ICallListener, IAudioDeviceEventsListener, IEndpointListener {
    private WeakReference<CallContract.View> mView;
    private WeakReference<ICall> mCall;
    private IAudioDeviceManager mAudioDeviceManager;
    private ScheduledExecutorService mTimer = Executors.newSingleThreadScheduledExecutor();

    private boolean mIsIncoming;
    private boolean mIsAudioMuted;
    private boolean mIsCallHeld;

    CallPresenter(CallContract.View view, boolean isIncoming) {
        mView = new WeakReference<>(view);
        mCall = new WeakReference<>(Shared.getInstance().getCallManager().getCall());
        mIsIncoming = isIncoming;
        mAudioDeviceManager = Voximplant.getAudioDeviceManager();
    }

    @Override
    public void start() {
        // the listeners are removed on call disconnected/failed
        if (mIsIncoming) {
            ICall call = mCall.get();
            if (call == null) {
                Log.e(APP_TAG, "CallPresenter: start: call is invalid");
                CallContract.View view = mView.get();
                if (view != null) {
                    view.callDisconnected();
                }
                return;
            }
            CallSettings callSettings = new CallSettings();
            callSettings.videoFlags = new VideoFlags(false, false);
            try {
                call.answer(callSettings);
            } catch (CallException e) {
                Log.e(APP_TAG, "CallPresenter: start exception: " + e.getMessage());
                CallContract.View view = mView.get();
                if (view != null) {
                    view.callDisconnected();
                }
            }
        }
        setupListeners(true);
    }

    private void setupListeners(boolean set) {
        ICall call = mCall.get();
        if (call == null) {
            Log.e(APP_TAG, "CallPresenter: setupListeners: call is invalid");
            CallContract.View view = mView.get();
            if (view != null) {
                view.callDisconnected();
            }
            return;
        }
        IEndpoint endpoint = null;
        if (!call.getEndpoints().isEmpty()) {
            endpoint = call.getEndpoints().get(0);
        }
        if (set) {
            if (endpoint != null) {
                endpoint.setEndpointListener(this);
            }
            call.addCallListener(this);
            mAudioDeviceManager.addAudioDeviceEventsListener(this);
        } else {
            if (endpoint != null) {
                endpoint.setEndpointListener(null);
            }
            mAudioDeviceManager.removeAudioDeviceEventsListener(this);
        }
    }

    @Override
    public void muteAudio() {
        ICall call = mCall.get();
        if (call != null) {
            mIsAudioMuted = !mIsAudioMuted;
            call.sendAudio(!mIsAudioMuted);
            CallContract.View view = mView.get();
            if (view != null) {
                view.updateMicButton(mIsAudioMuted);
            }
        }
    }

    @Override
    public void hold() {
        ICall call = mCall.get();
        if (call != null) {
            call.hold(!mIsCallHeld, new ICallCompletionHandler() {
                @Override
                public void onComplete() {
                    mIsCallHeld = !mIsCallHeld;
                    CallContract.View view = mView.get();
                    if (view != null) {
                        view.updateHoldButton(mIsCallHeld);
                    }
                }

                @Override
                public void onFailure(CallException exception) {
                    CallContract.View view = mView.get();
                    if (view != null) {
                        view.showError(R.string.toast_midcall_operation_failed, "Hold (" + !mIsCallHeld + ")", exception.getErrorCode().toString());
                    }
                }
            });
        }
    }

    @Override
    public void sendDTMF(String DTMF) {
        ICall call = mCall.get();
        if (call != null) {
            call.sendDTMF(DTMF);
        }
    }

    @Override
    public void stopCall() {
        ICall call = mCall.get();
        if (call != null) {
            call.hangup(null);
        }
    }

    @Override
    public List<String> getAudioDevices() {
        final List<String> audioDevices = new ArrayList<>();
        for (AudioDevice device : mAudioDeviceManager.getAudioDevices()) {
            audioDevices.add(device.toString());
        }
        return audioDevices;
    }

    //region Call duration
    private void startTimer() {
        CallContract.View view = mView.get();
        ICall call = mCall.get();
        if (view != null && call != null) {
            Runnable timerTask = () -> view.updateTimer(calculateTime(call.getCallDuration()));
            mTimer.scheduleWithFixedDelay(timerTask, 1, 1, SECONDS);
        }
    }

    private String calculateTime(long timeLong) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(timeLong - TimeZone.getDefault().getRawOffset());
        String dateFormatted = DateFormat.format("HH:mm:ss", calendar.getTime()).toString();
        return dateFormatted.startsWith("00") ? dateFormatted.substring(3) : dateFormatted;
    }
    //endregion

    //region ICallListener
    @Override
    public void onCallConnected(ICall call, Map<String, String> headers) {
        Log.i(APP_TAG, "onCallConnected: " + call.getCallId());
        CallContract.View view = mView.get();
        if (view != null && !call.getEndpoints().isEmpty()) {
            view.updateCallStatus(R.string.call_in_progress);
            view.updateDisplayName(call.getEndpoints().get(0).getUserDisplayName());
            view.enableButtons(true);
            startTimer();
        }
    }

    @Override
    public void onCallDisconnected(ICall call, Map<String, String> headers, boolean answeredElsewhere) {
        Log.i(APP_TAG, "onCallDisconnected: " + call.getCallId());
        setupListeners(false);
        CallContract.View view = mView.get();
        if (view != null) {
            view.callDisconnected();
        }
        mTimer.shutdown();
    }

    @Override
    public void onCallRinging(ICall call, Map<String, String> headers) {
        Log.i(APP_TAG, "onCallRinging: " + call.getCallId());
        CallContract.View view = mView.get();
        if (view != null) {
            view.updateCallStatus(R.string.call_ringing);
        }
    }

    @Override
    public void onCallFailed(ICall call, int code, final String description, Map<String, String> headers) {
        Log.i(APP_TAG, "onCallFailed: " + call.getCallId() + ", code: " + code);
        setupListeners(false);
        CallContract.View view = mView.get();
        if (code != 409 && view != null) {
            view.callFailed(description);
        }
        mTimer.shutdown();
    }
    //endregion

    //region Endpoint events
    @Override
    public void onEndpointAdded(ICall call, IEndpoint endpoint) {
        Log.i(APP_TAG, "onEndpointAdded: " + call.getCallId() + " " + endpoint.getEndpointId());
        endpoint.setEndpointListener(this);
    }

    @Override
    public void onEndpointInfoUpdated(IEndpoint endpoint) {
        ICall call = mCall.get();
        if (call != null) {
            Log.i(APP_TAG, "onEndpointInfoUpdated: " + call.getCallId() + " " + endpoint.getEndpointId());
            CallContract.View view = mView.get();
            String displayName = endpoint.getUserDisplayName();
            if (view != null && displayName != null) {
                view.updateDisplayName(displayName);
            }
        }
    }
    //endregion

    //region IAudioDeviceEventsListener
    @Override
    public void onAudioDeviceChanged(AudioDevice currentAudioDevice) {
        CallContract.View view = mView.get();
        if (view != null) {
            view.updateAudioDeviceButton(currentAudioDevice);
        }
    }

    @Override
    public void onAudioDeviceListChanged(List<AudioDevice> newDeviceList) {
    }
    //endregion
}
