/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.ui.login;

import com.voximplant.demos.audiocall.BasePresenter;
import com.voximplant.demos.audiocall.BaseView;

public interface LoginContract {
    interface View extends BaseView<Presenter> {

        void fillUsername(String username);

        void showProgress(boolean show);

        void loginSuccess(String displayName);

        void usernameInvalid(int error);

        void passwordInvalid(int error);

        void setTokenViewVisibility(boolean visible);

        void showError(int error);
    }

    interface Presenter extends BasePresenter {

        void loginWithPassword(String user, String password);

        void loginWithAccessToken(String user);

        void checkIfTokensExist();

        void stop();
    }
}
