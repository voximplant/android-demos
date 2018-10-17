/*
 * Copyright (c) 2017, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.proximity_sensor;

import android.app.Application;

import com.voximplant.demos.proximity_sensor.utils.NotificationHelper;
import com.voximplant.sdk.call.VideoCodec;
import com.voximplant.sdk.client.ClientConfig;
import com.voximplant.sdk.client.IClient;
import com.voximplant.sdk.Voximplant;
import com.voximplant.demos.proximity_sensor.manager.VoxCallManager;
import com.voximplant.demos.proximity_sensor.manager.VoxClientManager;
import com.voximplant.demos.proximity_sensor.utils.FileLogger;
import com.voximplant.demos.proximity_sensor.utils.ForegroundCheck;
import com.voximplant.demos.proximity_sensor.utils.SharedPreferencesHelper;

import java.util.concurrent.Executors;

public class SDKDemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ForegroundCheck.init(this);
        SharedPreferencesHelper.init(getApplicationContext());
        NotificationHelper.init(getApplicationContext());

        FileLogger.create(getApplicationContext());

        ClientConfig clientConfig = new ClientConfig();
        IClient client = Voximplant.getClientInstance(Executors.newSingleThreadExecutor(), getApplicationContext(), clientConfig);
        VoxClientManager clientManager = new VoxClientManager();
        clientManager.setClient(client);
        VoxCallManager callManager = new VoxCallManager(client, getApplicationContext());
        Shared.getInstance().setClientManager(clientManager);
        Shared.getInstance().setCallManager(callManager);
    }
}
