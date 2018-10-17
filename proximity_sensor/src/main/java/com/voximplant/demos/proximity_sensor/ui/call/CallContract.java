/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.proximity_sensor.ui.call;

import com.voximplant.sdk.call.QualityIssue;
import com.voximplant.sdk.call.QualityIssueLevel;
import com.voximplant.sdk.hardware.AudioDevice;
import com.voximplant.demos.proximity_sensor.BasePresenter;
import com.voximplant.demos.proximity_sensor.BaseView;

import org.webrtc.SurfaceViewRenderer;

import java.util.List;
import java.util.Map;

interface CallContract {
    interface View extends BaseView<Presenter> {

        void createLocalVideoView();
        void removeLocalVideoView();

        void createRemoteVideoView(String streamId, String displayName);
        void removeRemoteVideoView(String streamId);
        void removeAllVideoViews();

        void callDisconnected();
        void callFailed(String error);
        void showError(int resError, String param1, String param2);
    }

    interface Presenter extends BasePresenter {
        void stopCall();

        void localVideoViewCreated(SurfaceViewRenderer renderer);
        void localVideoViewRemoved(SurfaceViewRenderer renderer);

        void remoteVideoViewCreated(String streamId, SurfaceViewRenderer renderer);
        void remoteVideoViewRemoved(String streamId, SurfaceViewRenderer renderer);
    }
}
