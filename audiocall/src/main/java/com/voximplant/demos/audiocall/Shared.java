/*
 * Copyright (c) 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall;

import com.voximplant.demos.audiocall.manager.VoxCallManager;
import com.voximplant.demos.audiocall.manager.VoxClientManager;

public class Shared {
    private static Shared mInstance = null;
    private VoxClientManager mClientManager;
    private VoxCallManager mCallManager;

    public static synchronized Shared getInstance() {
        if (mInstance == null) {
            mInstance = new Shared();
        }
        return mInstance;
    }

    void setClientManager(VoxClientManager clientManager) {
        mClientManager = clientManager;
    }

    void setCallManager(VoxCallManager callManager) {
        mCallManager = callManager;
    }

    public VoxClientManager getClientManager() {
        return mClientManager;
    }

    public VoxCallManager getCallManager() {
        return mCallManager;
    }
}
