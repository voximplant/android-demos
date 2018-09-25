/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.quality_issues.ui.call;

import com.voximplant.sdk.call.QualityIssue;
import com.voximplant.sdk.call.QualityIssueLevel;
import com.voximplant.sdk.hardware.AudioDevice;
import com.voximplant.demos.quality_issues.BasePresenter;
import com.voximplant.demos.quality_issues.BaseView;

import org.webrtc.SurfaceViewRenderer;

import java.util.List;
import java.util.Map;

interface CallContract {
    interface View extends BaseView<Presenter> {
        void updateMicButton(boolean pressed);
        void updateHoldButton(boolean pressed);
        void updateSendVideoCheckbox(boolean checked);
        void updateReceiveVideoCheckbox(boolean checked);
        void updateCameraButton(boolean isFront);
        void updateAudioDeviceButton(AudioDevice audioDevice);

        void createLocalVideoView();
        void removeLocalVideoView();

        void createRemoteVideoView(String streamId, String displayName);
        void removeRemoteVideoView(String streamId);
        void removeAllVideoViews();

        void callDisconnected();
        void callFailed(String error);
        void showError(int resError, String param1, String param2);

        void qualityIssueDetected(QualityIssueLevel level, String issue);
    }

    interface Presenter extends BasePresenter {
        void muteAudio();
        void sendVideo(boolean send);
        void receiveVideo();
        void hold();
        void stopCall();

        void localVideoViewCreated(SurfaceViewRenderer renderer);
        void localVideoViewRemoved(SurfaceViewRenderer renderer);

        void remoteVideoViewCreated(String streamId, SurfaceViewRenderer renderer);
        void remoteVideoViewRemoved(String streamId, SurfaceViewRenderer renderer);

        void switchCamera();
        List<String> getAudioDevices();

        Map<QualityIssue, QualityIssueLevel> getCurrentQualityIssues();
    }
}
