/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.ui.callfailed;

import com.voximplant.demos.audiocall.BasePresenter;
import com.voximplant.demos.audiocall.BaseView;

public interface CallFailedContract {
    interface View extends BaseView<CallFailedContract.Presenter> {

        void startCallActivity(String user, boolean isIncoming);

        void startMainActivity();
    }

    interface Presenter extends BasePresenter {

        void makeCall(String user);

        void cancelCall();
    }
}
