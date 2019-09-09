/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.ui.incomingcall;

import com.voximplant.demos.audiocall.BasePresenter;
import com.voximplant.demos.audiocall.BaseView;

public interface IncomingCallContract {
    interface View extends BaseView<Presenter> {

        void startCallActivity(String user, boolean isIncoming);

        void finishActivity();
    }

    interface Presenter extends BasePresenter {

        void answerCall();

        void rejectCall();
    }
}
