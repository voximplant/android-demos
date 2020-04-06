/*
 * Copyright (c) 2011- 2020, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.videoconf.ui.start;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.voximplant.demos.videoconf.R;
import com.voximplant.demos.videoconf.ui.connecting.ConnectingActivity;

import static com.voximplant.demos.videoconf.Constants.MEETING_ID;
import static com.voximplant.demos.videoconf.Constants.USERNAME;

public class StartConfActivity extends AppCompatActivity {

    private EditText mMeetingIdView;
    private EditText mUsernameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_conf);

        mMeetingIdView = findViewById(R.id.edit_text_room);
        mUsernameView = findViewById(R.id.edit_text_name);

        Button joinButton = findViewById(R.id.button_join);
        joinButton.setOnClickListener(view -> {
            String meetingId = mMeetingIdView.getText().toString();
            if (meetingId.isEmpty()) {
                mMeetingIdView.setError(getString(R.string.required));
                mMeetingIdView.requestFocus();
                return;
            }
            String username  = mUsernameView.getText().toString();
            if (username.isEmpty()) {
                mUsernameView.setError(getString(R.string.required));
                mUsernameView.requestFocus();
                return;
            }

            Intent intent = new Intent(getApplicationContext(), ConnectingActivity.class);
            intent.putExtra(MEETING_ID, meetingId);
            intent.putExtra(USERNAME, username);
            startActivity(intent);
        });
    }


}
