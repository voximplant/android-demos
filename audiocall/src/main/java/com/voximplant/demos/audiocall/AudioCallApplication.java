/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall;

import android.app.Application;

import com.voximplant.demos.audiocall.manager.VoxCallManager;
import com.voximplant.demos.audiocall.manager.VoxClientManager;
import com.voximplant.demos.audiocall.utils.FileLogger;
import com.voximplant.demos.audiocall.utils.NotificationHelper;
import com.voximplant.demos.audiocall.utils.SharedPreferencesHelper;
import com.voximplant.sdk.Voximplant;
import com.voximplant.sdk.client.ClientConfig;
import com.voximplant.sdk.client.IClient;

import java.util.concurrent.Executors;

public class AudioCallApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferencesHelper.init(this);
        NotificationHelper.init(this);
        FileLogger.create(this);

        ClientConfig clientConfig = new ClientConfig();
        IClient client = Voximplant.getClientInstance(Executors.newSingleThreadExecutor(), getApplicationContext(), clientConfig);
        Shared.getInstance().setClientManager(new VoxClientManager(client));
        Shared.getInstance().setCallManager(new VoxCallManager(client, getApplicationContext()));
    }
}
