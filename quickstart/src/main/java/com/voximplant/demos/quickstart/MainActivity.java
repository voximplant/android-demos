package com.voximplant.demos.quickstart;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.voximplant.sdk.Voximplant;
import com.voximplant.sdk.call.CallException;
import com.voximplant.sdk.call.CallSettings;
import com.voximplant.sdk.call.CallStats;
import com.voximplant.sdk.call.ICall;
import com.voximplant.sdk.call.ICallListener;
import com.voximplant.sdk.call.IEndpoint;
import com.voximplant.sdk.call.IVideoStream;
import com.voximplant.sdk.call.VideoFlags;
import com.voximplant.sdk.client.AuthParams;
import com.voximplant.sdk.client.ClientConfig;
import com.voximplant.sdk.client.IClient;
import com.voximplant.sdk.client.IClientLoginListener;
import com.voximplant.sdk.client.IClientSessionListener;
import com.voximplant.sdk.client.LoginError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity  {
    private static final String TAG = "MainActivity";
    private static final String USERNAME = "user@app.acc.voximplant.com";
    private static final String PASSWORD = "p@ssw0rd";

    private IClient mClient;
    private HashMap<ICall, ICallListener> mCallListeners = new HashMap<>();
    private ICall mCurrentCall;

    private Button mCallButton;
    private Button mCallEndButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCallButton = findViewById(R.id.call_button);
        mCallButton.setOnClickListener((view) -> call());

        mCallEndButton = findViewById(R.id.end_button);
        mCallEndButton.setOnClickListener((view) -> endCall());

        mClient = Voximplant.getClientInstance(Executors.newSingleThreadExecutor(), getApplicationContext(), new ClientConfig());
        setupClientListeners();

        connect();

        if (missingPermissions().size() > 0) {
            ActivityCompat.requestPermissions(this, missingPermissions().toArray(new String[missingPermissions().size()]), PERMISSION_GRANTED);
        }
    }

    private void call() {
        if (missingPermissions().size() == 0) {
            CallSettings callSettings = new CallSettings();
            callSettings.videoFlags = new VideoFlags(false, false);
            ICall call = mClient.call("*",callSettings);
            if (call != null) {
                CallListener callListener = new CallListener();
                call.addCallListener(callListener);
                mCallListeners.put(call, callListener);
                try {
                    call.start();
                    mCurrentCall = call;
                    runOnUiThread(() -> {
                        mCallEndButton.setVisibility(View.VISIBLE);
                        mCallButton.setVisibility(View.GONE);
                    });
                } catch (CallException e) {
                    Log.e(TAG, "CallException: " + e.getMessage());
                }
            }
        } else {
            Log.e(TAG, missingPermissions() + "should be granted");
        }
    }

    private void endCall() {
        mCurrentCall.hangup(null);
    }

    private List<String> missingPermissions() {
        return Voximplant.getMissingPermissions(getApplicationContext(), false);
    }

    private void connect() {
        try {
            mClient.connect();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Connect to Voximplant in invalid state");
        }

    }

    private void setupClientListeners() {
        mClient.setClientSessionListener(new IClientSessionListener() {
            @Override
            public void onConnectionEstablished() {
                Log.i(TAG, "Connection established");
                mClient.login(USERNAME, PASSWORD);
            }

            @Override
            public void onConnectionFailed(String error) {
                Log.e(TAG, "Connection failed: " + error);
            }

            @Override
            public void onConnectionClosed() {
                Log.w(TAG, "Connection closed");
            }
        });

        mClient.setClientLoginListener(new IClientLoginListener() {
            @Override
            public void onLoginSuccessful(String s, AuthParams authParams) {
                Log.i(TAG, "Login successful");
            }

            @Override
            public void onLoginFailed(LoginError loginError) {
                Log.e(TAG, "Login failed: " + loginError);
            }

            @Override
            public void onRefreshTokenFailed(LoginError loginError) {

            }

            @Override
            public void onRefreshTokenSuccess(AuthParams authParams) {

            }

            @Override
            public void onOneTimeKeyGenerated(String s) {

            }
        });

        mClient.setClientIncomingCallListener((call, hasIncomingVideo, headers) -> {
            if (missingPermissions().size() == 0) {
                try {
                    CallSettings callSettings = new CallSettings();
                    callSettings.videoFlags = new VideoFlags(false, false);
                    call.answer(callSettings);
                    CallListener callListener = new CallListener();
                    call.addCallListener(callListener);
                    mCallListeners.put(call, callListener);
                    mCurrentCall = call;
                } catch (CallException e) {
                    Log.e(TAG, "CallException: " + e.getMessage());
                }
            } else {
                Log.e(TAG, missingPermissions() + "should be granted");
            }
        });
    }

    class CallListener implements ICallListener {

        @Override
        public void onCallConnected(ICall call, Map<String, String> headers) {
            Log.i(TAG, "You can hear audio from the cloud");
            runOnUiThread(() -> {
                mCallEndButton.setVisibility(View.VISIBLE);
                mCallButton.setVisibility(View.GONE);
            });
        }

        @Override
        public void onCallDisconnected(ICall call, Map<String, String> headers, boolean b) {
            Log.i(TAG, "The call has ended");
            call.removeCallListener(mCallListeners.get(call));
            mCallListeners.remove(call);
            mCurrentCall = null;
            runOnUiThread(() -> {
                mCallEndButton.setVisibility(View.GONE);
                mCallButton.setVisibility(View.VISIBLE);
            });
        }

        @Override
        public void onCallFailed(ICall call, int code, String error, Map<String, String> headers) {
            Log.e(TAG, "Call failed with error " + error);
            call.removeCallListener(mCallListeners.get(call));
            mCallListeners.remove(call);
        }

        @Override
        public void onCallRinging(ICall call, Map<String, String> headers) {

        }

        @Override
        public void onCallAudioStarted(ICall call) {

        }

        @Override
        public void onSIPInfoReceived(ICall call, String type, String content, Map<String, String> headers) {

        }

        @Override
        public void onMessageReceived(ICall call, String text) {

        }

        @Override
        public void onLocalVideoStreamAdded(ICall call, IVideoStream videoStream) {

        }

        @Override
        public void onLocalVideoStreamRemoved(ICall call, IVideoStream videoStream) {

        }

        @Override
        public void onICETimeout(ICall call) {

        }

        @Override
        public void onICECompleted(ICall call) {

        }

        @Override
        public void onEndpointAdded(ICall call, IEndpoint endpoint) {

        }

        @Override
        public void onCallStatsReceived(ICall iCall, CallStats callStats) {

        }
    }
}
