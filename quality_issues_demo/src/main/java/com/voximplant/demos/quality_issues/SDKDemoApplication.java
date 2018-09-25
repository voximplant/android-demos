/*
 * Copyright (c) 2017, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.quality_issues;

import android.app.Application;

import com.voximplant.demos.quality_issues.utils.NotificationHelper;
import com.voximplant.sdk.client.ClientConfig;
import com.voximplant.sdk.client.IClient;
import com.voximplant.sdk.Voximplant;
import com.voximplant.demos.quality_issues.manager.VoxCallManager;
import com.voximplant.demos.quality_issues.manager.VoxClientManager;
import com.voximplant.demos.quality_issues.utils.ForegroundCheck;
import com.voximplant.demos.quality_issues.utils.SharedPreferencesHelper;

import java.util.concurrent.Executors;

public class SDKDemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ForegroundCheck.init(this);
        SharedPreferencesHelper.init(getApplicationContext());
        NotificationHelper.init(getApplicationContext());

        ClientConfig clientConfig = new ClientConfig();
//        clientConfig.enableDebugLogging = true;
        IClient client = Voximplant.getClientInstance(Executors.newSingleThreadExecutor(), getApplicationContext(), clientConfig);
        VoxClientManager clientManager = new VoxClientManager();
        clientManager.setClient(client);
        VoxCallManager callManager = new VoxCallManager(client, getApplicationContext());
        Shared.getInstance().setClientManager(clientManager);
        Shared.getInstance().setCallManager(callManager);
    }
}
