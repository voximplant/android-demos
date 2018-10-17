/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.proximity_sensor.ui.incomingcall;

import com.voximplant.demos.proximity_sensor.BasePresenter;
import com.voximplant.demos.proximity_sensor.BaseView;

interface IncomingCallContract {
    interface View extends BaseView<Presenter> {
        void onCallEnded(String callId);
    }

    interface Presenter extends BasePresenter {
        boolean isVideoCall();
        String getCallId();
        void answerCall();
        void rejectCall();
    }
}
