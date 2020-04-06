/*
 * Copyright (c) 2011- 2020, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.videoconf.ui.conf;

import com.voximplant.demos.videoconf.ui.BasePresenter;
import com.voximplant.demos.videoconf.ui.BaseView;
import com.voximplant.sdk.hardware.AudioDevice;

import org.webrtc.SurfaceViewRenderer;

import java.util.List;

public interface ConfContract {
    interface View extends BaseView<Presenter> {

        void createVideoView(String endpointId, String displayName);
        void removeVideoView(String endpointId);

        void requestVideoViewForEndpoint(String endpointId);

        void removeAllVideoViews();
        void updateVideoView(String streamId, String displayName);

        void updateAudioDeviceButton(AudioDevice audioDevice);
        void updateMicButton(boolean muted);
        void updateSendVideoButton(boolean send);
        void callDisconnected();

        void showError(String error);

        void startForegroundService();
        void stopForegroundService();
    }

    interface Presenter extends BasePresenter {
        void muteAudio();
        void sendVideo();
        void stopCall();

        void receivedVideoViewForEndpoint(String endpointId, SurfaceViewRenderer renderer);

        void switchCamera();
        List<String> getAudioDevices();
    }
}
