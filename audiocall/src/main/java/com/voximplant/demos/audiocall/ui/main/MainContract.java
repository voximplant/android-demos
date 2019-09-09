/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.ui.main;

import com.voximplant.demos.audiocall.BasePresenter;
import com.voximplant.demos.audiocall.BaseView;

public interface MainContract {
    interface View extends BaseView<Presenter> {

        void notifyConnectionClosed();

        void notifyInvalidCallUser();

        void startCallActivity(String user, boolean isIncoming);

        void showMyDisplayName(String displayName);
    }

    interface Presenter extends BasePresenter {

        void makeCall(String user);

        void logout();
    }
}
