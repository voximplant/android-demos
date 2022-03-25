/*
 * Copyright (c) 2011- 2020, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.videoconf.ui.conf;

import android.app.Activity;
import android.util.Log;

import com.voximplant.demos.videoconf.Shared;
import com.voximplant.demos.videoconf.manager.VoxClientManager;
import com.voximplant.sdk.Voximplant;
import com.voximplant.sdk.call.CallException;
import com.voximplant.sdk.call.ICall;
import com.voximplant.sdk.call.ICallCompletionHandler;
import com.voximplant.sdk.call.ICallListener;
import com.voximplant.sdk.call.IEndpoint;
import com.voximplant.sdk.call.IEndpointListener;
import com.voximplant.sdk.call.ILocalVideoStream;
import com.voximplant.sdk.call.IRemoteVideoStream;
import com.voximplant.sdk.call.IVideoStream;
import com.voximplant.sdk.call.RenderScaleType;
import com.voximplant.sdk.hardware.AudioDevice;
import com.voximplant.sdk.hardware.IAudioDeviceEventsListener;
import com.voximplant.sdk.hardware.IAudioDeviceManager;
import com.voximplant.sdk.hardware.ICameraManager;
import com.voximplant.sdk.hardware.VideoQuality;

import org.webrtc.SurfaceViewRenderer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.voximplant.demos.videoconf.Constants.APP_TAG;
import static com.voximplant.demos.videoconf.Constants.SELF_ENDPOINT_ID;

public class ConfPresenter implements ConfContract.Presenter, ICallListener, IEndpointListener, IAudioDeviceEventsListener {

    private final WeakReference<ConfContract.View> mView;
    private final String mMeetingId;
    private final VoxClientManager mClientManager = Shared.getInstance().getClientManager();
    private ICall mConferenceCall;
    private boolean mIsAudioMuted;
    private boolean mIsVideoSent;

    private final HashMap<IVideoStream, String> mEndpointVideoStreams = new HashMap<>();

    private final IAudioDeviceManager mAudioDeviceManager;

    private final ICameraManager mCameraManager;
    private int mCameraType;
    private final VideoQuality mVideoQuality = VideoQuality.VIDEO_QUALITY_LOW;

    ConfPresenter(ConfContract.View view, String meetingId) {
        mView = new WeakReference<>(view);
        mMeetingId = meetingId;
        mIsAudioMuted = false;
        mIsVideoSent = true;
        mCameraType = 1;
        mCameraManager = Voximplant.getCameraManager(((Activity)mView.get()));
        mCameraManager.setCamera(mCameraType, mVideoQuality);
        mAudioDeviceManager = Voximplant.getAudioDeviceManager();
    }


    @Override
    public void start() {
        ConfContract.View view = mView.get();
        if (view == null) {
            return;
        }
        List<AudioDevice> audioDevices = mAudioDeviceManager.getAudioDevices();
        if (!audioDevices.contains(AudioDevice.BLUETOOTH) && !audioDevices.contains(AudioDevice.WIRED_HEADSET)) {
            mAudioDeviceManager.selectAudioDevice(AudioDevice.SPEAKER);
        }
        mConferenceCall = mClientManager.createConferenceCall("myconf3");
        if (mConferenceCall != null) {
            try {
                mConferenceCall.addCallListener(this);
                mConferenceCall.start();
                mAudioDeviceManager.addAudioDeviceEventsListener(this);
                view.updateAudioDeviceButton(mAudioDeviceManager.getActiveDevice());
            } catch (CallException e) {
                Log.e(APP_TAG, "ConfPresenter: start: failed to start call " + e.getErrorCode());
                view.showError("Failed to connect " + e.getErrorCode());
            }
        } else {
            view.showError("Failed to connect, please try again");
        }
    }

    @Override
    public void muteAudio() {
        if (mConferenceCall != null) {
            mIsAudioMuted = !mIsAudioMuted;
            mConferenceCall.sendAudio(!mIsAudioMuted);
            ConfContract.View view = mView.get();
            if (view != null) {
                view.updateMicButton(mIsAudioMuted);
            }
        }
    }

    @Override
    public void sendVideo() {
        mConferenceCall.sendVideo(!mIsVideoSent, new ICallCompletionHandler() {
            @Override
            public void onComplete() {
                mIsVideoSent = !mIsVideoSent;
                ConfContract.View view = mView.get();
                if (view != null) {
                    view.updateSendVideoButton(mIsVideoSent);
                }
            }

            @Override
            public void onFailure(CallException e) {

            }
        });
    }

    @Override
    public void stopCall() {
        mConferenceCall.hangup(null);
    }

    @Override
    public void receivedVideoViewForEndpoint(String endpointId, SurfaceViewRenderer renderer) {
        for (Map.Entry<IVideoStream, String> entry : mEndpointVideoStreams.entrySet()) {
            if (entry.getValue().equals(endpointId)) {
                entry.getKey().addVideoRenderer(renderer, RenderScaleType.SCALE_FIT);
            }
        }
    }

    @Override
    public void switchCamera() {
        mCameraType = (mCameraType == 0) ? 1 : 0;
        mCameraManager.setCamera(mCameraType, mVideoQuality);
    }

    @Override
    public List<String> getAudioDevices() {
        final List<String> audioDevices = new ArrayList<>();
        for (AudioDevice device : Voximplant.getAudioDeviceManager().getAudioDevices()) {
            audioDevices.add(device.toString());
        }
        return audioDevices;
    }


    //Call events
    @Override
    public void onCallDisconnected(ICall call, Map<String, String> headers, boolean answeredElsewhere) {
        mConferenceCall.removeCallListener(this);
        mClientManager.setClientManagerListener(null);
        mClientManager.disconnect();
        mAudioDeviceManager.removeAudioDeviceEventsListener(this);
        ConfContract.View view = mView.get();
        if (view != null) {
            view.stopForegroundService();
            view.removeAllVideoViews();
            view.callDisconnected();
        }
    }

    @Override
    public void onCallConnected(ICall call, Map<String, String> headers) {
        ConfContract.View view = mView.get();
        if (view != null) {
            view.startForegroundService();
        }
    }

    @Override
    public void onCallFailed(ICall call, int code, String description, Map<String, String> headers) {
        mConferenceCall.removeCallListener(this);
        mClientManager.setClientManagerListener(null);
        mClientManager.disconnect();
        mAudioDeviceManager.removeAudioDeviceEventsListener(this);
        ConfContract.View view = mView.get();
        if (view != null) {
            view.stopForegroundService();
            view.removeAllVideoViews();
            view.callDisconnected();
        }
    }

    @Override
    public void onCallReconnecting(ICall call) {
        Log.d(APP_TAG, "onCallReconnecting");
    }

    @Override
    public void onCallReconnected(ICall call) {
        Log.d(APP_TAG, "onCallReconnected");
    }

    @Override
    public void onLocalVideoStreamAdded(ICall call, ILocalVideoStream videoStream) {
        Log.i(APP_TAG, "onLocalVideoStreamAdded: " + call.getCallId());
        ConfContract.View view = mView.get();
        if (view != null) {
            mEndpointVideoStreams.put(videoStream, SELF_ENDPOINT_ID);
            view.createVideoView(SELF_ENDPOINT_ID, mClientManager.getDisplayName() + "(you)");
            view.requestVideoViewForEndpoint(SELF_ENDPOINT_ID);
        }
    }

    @Override
    public void onLocalVideoStreamRemoved(ICall call, ILocalVideoStream videoStream) {
        Log.i(APP_TAG, "onLocalVideoStreamRemoved: " + call.getCallId());
        mEndpointVideoStreams.remove(videoStream);
        ConfContract.View view = mView.get();
        if (view != null) {
            view.removeVideoView(SELF_ENDPOINT_ID);
        }
    }



    //Endpoint events
    @Override
    public void onEndpointAdded(ICall call, IEndpoint endpoint) {
        if (endpoint != null && !endpoint.getEndpointId().equals(call.getCallId())) {
            endpoint.setEndpointListener(this);
            Log.i(APP_TAG, "onRemoteVideoStreamAdded: "+ endpoint.getEndpointId());
        }
    }

    @Override
    public void onRemoteVideoStreamAdded(IEndpoint endpoint, IRemoteVideoStream videoStream) {
        mEndpointVideoStreams.put(videoStream, endpoint.getEndpointId());
        ConfContract.View view = mView.get();
        if (view != null) {
            view.createVideoView(endpoint.getEndpointId(), endpoint.getUserDisplayName());
            view.requestVideoViewForEndpoint(endpoint.getEndpointId());
        }
    }

    @Override
    public void onRemoteVideoStreamRemoved(IEndpoint endpoint, IRemoteVideoStream videoStream) {
        mEndpointVideoStreams.remove(videoStream);
        ConfContract.View view = mView.get();
        if (view != null) {
            view.removeVideoView(endpoint.getEndpointId());
        }
    }

    @Override
    public void onEndpointRemoved(IEndpoint endpoint) {
        if (endpoint != null) {
            endpoint.setEndpointListener(null);
            Log.i(APP_TAG, "onRemoteVideoStreamRemoved: " + endpoint.getEndpointId());
            ConfContract.View view = mView.get();
            if (view != null) {
                view.removeVideoView(endpoint.getEndpointId());
            }
        }
    }

    @Override
    public void onEndpointInfoUpdated(IEndpoint endpoint) {
        if (mConferenceCall != null) {
            Log.i(APP_TAG, "onEndpointInfoUpdated: " + mConferenceCall.getCallId() + " " + endpoint.getEndpointId());
            ConfContract.View view = mView.get();
            if (view != null) {
                view.updateVideoView(endpoint.getEndpointId(), endpoint.getUserDisplayName());
            }
        }
    }

    @Override
    public void onAudioDeviceChanged(AudioDevice currentAudioDevice) {
        ConfContract.View view = mView.get();
        if (view != null) {
            view.updateAudioDeviceButton(currentAudioDevice);
        }
    }

    @Override
    public void onAudioDeviceListChanged(List<AudioDevice> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        if (!list.contains(AudioDevice.BLUETOOTH) && !list.contains(AudioDevice.WIRED_HEADSET)) {
            mAudioDeviceManager.selectAudioDevice(AudioDevice.SPEAKER);
        }
    }
}
