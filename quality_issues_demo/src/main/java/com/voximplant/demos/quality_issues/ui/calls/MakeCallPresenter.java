/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.quality_issues.ui.calls;

import com.voximplant.demos.quality_issues.utils.SharedPreferencesHelper;
import com.voximplant.sdk.call.ICall;
import com.voximplant.sdk.call.VideoFlags;
import com.voximplant.demos.quality_issues.Shared;
import com.voximplant.demos.quality_issues.manager.IClientManagerListener;
import com.voximplant.demos.quality_issues.manager.VoxCallManager;
import com.voximplant.demos.quality_issues.manager.VoxClientManager;

import java.lang.ref.WeakReference;

import static com.voximplant.demos.quality_issues.utils.Constants.IS_CONFERENCE;
import static com.voximplant.demos.quality_issues.utils.Constants.OUTGOING_USERNAME;

public class MakeCallPresenter implements MakeCallContract.Presenter, IClientManagerListener {
    private static class CallDescriptor {
        private final String mCallId;
        private final boolean mWithVideo;
        private final String mUser;
        private final boolean mIsIncoming;

        CallDescriptor(String callId, boolean withVideo, String user, boolean isIncoming) {
            mCallId = callId;
            mWithVideo = withVideo;
            mUser = user;
            mIsIncoming = isIncoming;
        }

        String getCallId() {
            return mCallId;
        }

        boolean getIsVideo() {
            return mWithVideo;
        }

        String getUser() {
            return mUser;
        }

        boolean getIsIncoming() {
            return mIsIncoming;
        }
    }

    private final WeakReference<MakeCallContract.View> mView;
    private final VoxCallManager mCallManager = Shared.getInstance().getCallManager();
    private final VoxClientManager mClientManager = Shared.getInstance().getClientManager();
    private CallDescriptor mCallWaitingPermissions = null;

    MakeCallPresenter(MakeCallContract.View view) {
        mView = new WeakReference<>(view);
    }

    @Override
    public void start() {
        Shared.getInstance().getClientManager().addListener(this);
    }

    @Override
    public void onConnectionClosed() {
        MakeCallContract.View view = mView.get();
        if (view != null) {
            view.notifyConnectionClosed();
        }
    }

    @Override
    public void answerCall(String callId, boolean withVideo) {
        ICall call = mCallManager.getCallById(callId);
        if (call != null) {
            MakeCallContract.View view = mView.get();
            if (view != null) {
                if (view.checkPermissionsGrantedForCall(withVideo)) {
                    view.startCallActivity(callId, withVideo, call.getEndpoints().get(0).getUserDisplayName(), true);
                } else {
                    mCallWaitingPermissions = new CallDescriptor(callId, withVideo, call.getEndpoints().get(0).getUserDisplayName(), true);
                }
            }

        }
    }

    @Override
    public void makeCall(String user, boolean withVideo, boolean isConf) {
        SharedPreferencesHelper.get().saveToPrefs(OUTGOING_USERNAME, user);
        SharedPreferencesHelper.get().saveToPrefs(IS_CONFERENCE, isConf);
        MakeCallContract.View view = mView.get();
        if (view == null) {
            return;
        }
        if (user == null || user.isEmpty()) {
            view.notifyInvalidCallUser();
            return;
        }
        String callId = mCallManager.createCall(user, new VideoFlags(withVideo, withVideo), isConf);
        if (view.checkPermissionsGrantedForCall(withVideo)) {
            view.startCallActivity(callId, withVideo, user, false);
        } else {
            mCallWaitingPermissions = new CallDescriptor(callId, withVideo, user, false);
        }
    }

    @Override
    public void permissionsAreGrantedForCall() {
        MakeCallContract.View view = mView.get();
        if (mCallWaitingPermissions != null && view != null) {
            view.startCallActivity(mCallWaitingPermissions.getCallId(),
                    mCallWaitingPermissions.getIsVideo(),
                    mCallWaitingPermissions.getUser(),
                    mCallWaitingPermissions.getIsIncoming());
        }
    }


    @Override
    public void logout() {
        mCallManager.endAllCalls();
        mClientManager.removeListener(this);
        mClientManager.logout();
    }
}
