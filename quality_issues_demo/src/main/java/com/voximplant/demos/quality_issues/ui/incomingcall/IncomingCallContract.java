/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.quality_issues.ui.incomingcall;

import com.voximplant.demos.quality_issues.BasePresenter;
import com.voximplant.demos.quality_issues.BaseView;

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
