/*
 * Copyright (c) 2011- 2020, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.videoconf.ui.connecting;

import com.voximplant.demos.videoconf.ui.BasePresenter;
import com.voximplant.demos.videoconf.ui.BaseView;

public interface ConnectingContract {
    interface View extends BaseView<Presenter> {
        void showError(String error);
        void navigateToConfScreen();
    }

    interface Presenter extends BasePresenter {
        void loginUser(String meetingId, String displayName);
    }
}
