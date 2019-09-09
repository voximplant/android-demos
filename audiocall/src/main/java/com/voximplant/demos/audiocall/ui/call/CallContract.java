/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.ui.call;

import com.voximplant.demos.audiocall.BasePresenter;
import com.voximplant.demos.audiocall.BaseView;
import com.voximplant.sdk.hardware.AudioDevice;

import java.util.List;

public interface CallContract {
    interface View extends BaseView<Presenter> {

        void updateCallStatus(int resStatus);

        void updateDisplayName(String displayName);

        void updateMicButton(boolean pressed);

        void updateHoldButton(boolean pressed);

        void updateTimer(String time);

        void enableButtons(boolean enabled);

        void updateAudioDeviceButton(AudioDevice audioDevice);

        void callDisconnected();

        void callFailed(String description);

        void showError(int resError, String param1, String param2);
    }

    interface Presenter extends BasePresenter {

        void muteAudio();

        void hold();

        void stopCall();

        void sendDTMF(String DTMF);

        List<String> getAudioDevices();
    }
}
