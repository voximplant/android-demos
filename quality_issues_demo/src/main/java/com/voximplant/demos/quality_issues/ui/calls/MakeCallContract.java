/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.quality_issues.ui.calls;

import com.voximplant.demos.quality_issues.BasePresenter;
import com.voximplant.demos.quality_issues.BaseView;

interface MakeCallContract {
    interface View extends BaseView<Presenter> {
        void notifyConnectionClosed();
        void notifyInvalidCallUser();

        void startCallActivity(String callId, boolean withVideo, String user, boolean isIncoming);

        boolean checkPermissionsGrantedForCall(boolean isVideoCall);
    }

    interface Presenter extends BasePresenter {
        void answerCall(String callId, boolean withVideo);
        void makeCall(String user, boolean withVideo);

        void permissionsAreGrantedForCall();

        void logout();
    }
}
