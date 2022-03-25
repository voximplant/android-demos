/*
 * Copyright (c) 2011- 2020, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.videoconf.ui.start;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.voximplant.demos.videoconf.R;
import com.voximplant.demos.videoconf.ui.connecting.ConnectingActivity;

public class StartConfActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_conf);

        Intent intent = new Intent(getApplicationContext(), ConnectingActivity.class);
        startActivity(intent);
    }


}
