/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.proximity_sensor.ui.call;

import android.util.Log;

import com.voximplant.demos.proximity_sensor.Shared;
import com.voximplant.demos.proximity_sensor.manager.ICallEventsListener;
import com.voximplant.demos.proximity_sensor.manager.VoxCallManager;
import com.voximplant.sdk.Voximplant;
import com.voximplant.sdk.call.CallException;
import com.voximplant.sdk.call.CallSettings;
import com.voximplant.sdk.call.ICall;
import com.voximplant.sdk.call.IEndpoint;
import com.voximplant.sdk.call.IEndpointListener;
import com.voximplant.sdk.call.IVideoStream;
import com.voximplant.sdk.call.RenderScaleType;
import com.voximplant.sdk.call.VideoFlags;
import com.voximplant.sdk.hardware.ICameraManager;
import com.voximplant.sdk.hardware.VideoQuality;

import org.webrtc.SurfaceViewRenderer;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static com.voximplant.demos.proximity_sensor.utils.Constants.APP_TAG;

public class CallPresenter implements CallContract.Presenter, ICallEventsListener, IEndpointListener {
    private final WeakReference<CallContract.View> mView;
    private WeakReference<ICall> mCall;
    private VoxCallManager mCallManager = Shared.getInstance().getCallManager();

    private boolean mIsIncoming;
    private boolean mIsVideoSent;
    private boolean mIsVideoReceived;

    private IVideoStream mLocalVideoStream;
    private HashMap<IVideoStream, IEndpoint> mEndpointVideoStreams = new HashMap<>();

    CallPresenter(CallContract.View view, String callId, boolean isIncoming, boolean withVideo) {
        mView = new WeakReference<>(view);
        mCall = new WeakReference<>(mCallManager.getCallById(callId));
        mIsIncoming = isIncoming;
        mIsVideoSent = withVideo;
        mIsVideoReceived = withVideo;
        int cameraType = 1;

        ICameraManager cameraManager = Voximplant.getCameraManager(((CallActivity) mView.get()));
        cameraManager.setCamera(cameraType, VideoQuality.VIDEO_QUALITY_MEDIUM);
    }

    @Override
    public void start() {
        ICall call = mCall.get();
        if (call == null) {
            Log.e(APP_TAG, "CallPresenter: start: call is invalid");
            return;
        }

        // the listeners are removed on call disconnected/failed
        setupListeners(true);
        if (mIsIncoming) {
            try {
                CallSettings callSettings = new CallSettings();
                callSettings.videoFlags = new VideoFlags(mIsVideoReceived, mIsVideoSent);
                call.answer(callSettings);
            } catch (CallException e) {
                Log.e(APP_TAG, "CallPresenter: answer exception: " + e.getMessage());
                CallContract.View view = mView.get();
                if (view != null) {
                    view.callDisconnected();
                }
            }
        } else {
            try {
                call.start();
            } catch (CallException e) {
                Log.e(APP_TAG, "CallPresenter: startException: " + e.getMessage());
                CallContract.View view = mView.get();
                if (view != null) {
                    view.callDisconnected();
                }
            }
        }
    }

    private void setupListeners(boolean set) {
        ICall call = mCall.get();
        if (call == null) {
            Log.e(APP_TAG, "CallPresenter: start: call is invalid");
            return;
        }
        IEndpoint endpoint = null;
        if (call.getEndpoints().size() > 0) {
            endpoint = call.getEndpoints().get(0);
        }
        if (set) {
            if (endpoint != null) {
                endpoint.setEndpointListener(this);
            }
            mCallManager.addCallEventListener(call.getCallId(), this);
        } else {
            if (endpoint != null) {
                endpoint.setEndpointListener(null);
            }
            mCallManager.removeCallEventListener(call.getCallId(), this);
            call.setQualityIssueListener(null);
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
    public void localVideoViewCreated(SurfaceViewRenderer renderer) {
        if (mLocalVideoStream != null) {
            mLocalVideoStream.addVideoRenderer(renderer, RenderScaleType.SCALE_FILL);
        }
    }

    @Override
    public void localVideoViewRemoved(SurfaceViewRenderer renderer) {
        if (mLocalVideoStream != null) {
            mLocalVideoStream.removeVideoRenderer(renderer);
            mLocalVideoStream = null;
        }
    }

    @Override
    public synchronized void remoteVideoViewCreated(String streamId, SurfaceViewRenderer renderer) {
        for (Map.Entry<IVideoStream, IEndpoint> entry : mEndpointVideoStreams.entrySet()) {
            if (entry.getKey().getVideoStreamId().equals(streamId)) {
                entry.getKey().addVideoRenderer(renderer, RenderScaleType.SCALE_FILL);
            }
        }
    }

    @Override
    public synchronized void remoteVideoViewRemoved(String streamId, SurfaceViewRenderer renderer) {
        IVideoStream videoStream = null;
        for (Map.Entry<IVideoStream, IEndpoint> entry : mEndpointVideoStreams.entrySet()) {
            if (entry.getKey().getVideoStreamId().equals(streamId)) {
                entry.getKey().removeVideoRenderer(renderer);
                videoStream = entry.getKey();
            }
        }
        if (videoStream != null) {
            mEndpointVideoStreams.remove(videoStream);
        }
    }

    //region Call Events
    @Override
    public void onCallConnected(Map<String, String> headers) {
        ICall call = mCall.get();
        if (call == null) {
            return;
        }
        mCallManager.startForegroundCallService();
        Log.i(APP_TAG, "onCallConnected: " + call.getCallId());
    }

    @Override
    public void onCallDisconnected(Map<String, String> headers, boolean answeredElsewhere) {
        mCallManager.stopForegroundService();
        ICall call = mCall.get();
        if (call != null) {
            Log.i(APP_TAG, "onCallDisconnected: " + call.getCallId());
            setupListeners(false);
            CallContract.View view = mView.get();
            if (view != null) {
                view.removeAllVideoViews();
                view.callDisconnected();
            }
        }
    }

    @Override
    public void onCallFailed(int code, final String description, Map<String, String> headers) {
        ICall call = mCall.get();
        if (call != null) {
            Log.i(APP_TAG, "onCallFailed: " + call.getCallId() + ", code: " + code);
            setupListeners(false);
        }
        if (code != 409) {
            CallContract.View view = mView.get();
            if (view != null) {
                view.callFailed(description);
            }
        }
    }

    @Override
    public void onLocalVideoStreamAdded(IVideoStream videoStream) {
        ICall call = mCall.get();
        if (call != null) {
            Log.i(APP_TAG, "onLocalVideoStreamAdded: " + call.getCallId());
            mLocalVideoStream = videoStream;
            CallContract.View view = mView.get();
            if (view != null) {
                view.createLocalVideoView();
            }
        }
    }

    @Override
    public void onLocalVideoStreamRemoved(IVideoStream videoStream) {
        ICall call = mCall.get();
        if (call != null) {
            Log.i(APP_TAG, "onLocalVideoStreamRemoved: " + call.getCallId());
            CallContract.View view = mView.get();
            if (view != null) {
                view.removeLocalVideoView();
            }
        }
    }

    @Override
    public void onEndpointAdded(IEndpoint endpoint) {
        ICall call = mCall.get();
        if (call != null) {
            Log.i(APP_TAG, "onEndpointAdded: " + call.getCallId() + " " + endpoint.getEndpointId());
            endpoint.setEndpointListener(this);
        }
    }
    //endregion

    //region Endpoint events
    @Override
    public synchronized void onRemoteVideoStreamAdded(IEndpoint endpoint, IVideoStream videoStream) {
        if (endpoint != null && videoStream != null) {
            Log.i(APP_TAG, "onRemoteVideoStreamAdded: "+ endpoint.getEndpointId() + ", stream: " + videoStream.getVideoStreamId());
            mEndpointVideoStreams.put(videoStream, endpoint);
            CallContract.View view = mView.get();
            if (view != null) {
                view.createRemoteVideoView(videoStream.getVideoStreamId(), endpoint.getUserDisplayName());
            }
        }
    }

    @Override
    public void onRemoteVideoStreamRemoved(IEndpoint endpoint, IVideoStream videoStream) {
        if (endpoint != null && videoStream != null) {
            Log.i(APP_TAG, "onRemoteVideoStreamRemoved: " + endpoint.getEndpointId() + ", stream: " + videoStream.getVideoStreamId());
            CallContract.View view = mView.get();
            if (view != null) {
                view.removeRemoteVideoView(videoStream.getVideoStreamId());
            }
        }
    }

    @Override
    public void onEndpointRemoved(IEndpoint endpoint) {
        ICall call = mCall.get();
        if (call != null) {
            Log.i(APP_TAG, "onEndpointRemoved: " + call.getCallId() + " " + endpoint.getEndpointId());
            endpoint.setEndpointListener(null);
        }
    }

    @Override
    public void onEndpointInfoUpdated(IEndpoint endpoint) { }

    //endregion
}
