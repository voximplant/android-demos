/*
 * Copyright (c) 2011- 2020, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.videoconf;

import com.voximplant.demos.videoconf.manager.VoxClientManager;

public class Shared {
    private static Shared mInstance = null;
    private VoxClientManager mClientManager;

    public static synchronized Shared getInstance() {
        if (mInstance == null) {
            mInstance = new Shared();
        }
        return mInstance;
    }

    void setClientManager(VoxClientManager clientManager) {
        mClientManager = clientManager;
    }
    public VoxClientManager getClientManager() {
        return mClientManager;
    }
}
