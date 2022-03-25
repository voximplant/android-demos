/*
 * Copyright (c) 2011- 2020, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.videoconf;

import android.app.Application;

import androidx.multidex.MultiDexApplication;

import com.voximplant.demos.videoconf.manager.VoxClientManager;
import com.voximplant.sdk.Voximplant;
import com.voximplant.sdk.client.ClientConfig;
import com.voximplant.sdk.client.IClient;

import java.util.concurrent.Executors;

public class App extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        ClientConfig clientConfig = new ClientConfig();
        IClient client = Voximplant.getClientInstance(Executors.newSingleThreadExecutor(), getApplicationContext(), clientConfig);
        VoxClientManager clientManager = new VoxClientManager(client);
        Shared.getInstance().setClientManager(clientManager);
        NotificationHelper.init(getApplicationContext());
    }
}
