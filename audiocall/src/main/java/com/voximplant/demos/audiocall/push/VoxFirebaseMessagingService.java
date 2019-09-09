/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.push;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.voximplant.demos.audiocall.Shared;
import com.voximplant.demos.audiocall.manager.VoxClientManager;

import java.util.Map;

public class VoxFirebaseMessagingService extends FirebaseMessagingService {
    private VoxClientManager mClientManager = Shared.getInstance().getClientManager();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> push = remoteMessage.getData();
        if (push.containsKey("voximplant")) {
            mClientManager.pushNotificationReceived(push);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        mClientManager.firebaseTokenRefreshed(token);
    }
}
