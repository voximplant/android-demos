/*
 * Copyright (c) 2011- 2020, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.videoconf.manager;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.voximplant.demos.videoconf.Constants.APP_TAG;

public class NetworkManager {
    interface ICredentialsRequestResult {
        void onSuccess(String login, String password);
        void onFailure();
    }

    private OkHttpClient mOkHttpClient;

    NetworkManager() {
        mOkHttpClient = new OkHttpClient.Builder().build();
    }

    void requestLoginCredentials(String meetingId, String username, ICredentialsRequestResult result) {
        if (result == null) {
            Log.e(APP_TAG, "NetworkManager: requestLoginCredentials: result object is not provided, cancel request");
            return;
        }
        Request request = new Request.Builder()
                .url("https://demos05.voximplant.com/conf/api/?id=" + meetingId + "&displayName=" +
                        username + "&email=&conferenceId=" + meetingId)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                result.onFailure();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(APP_TAG, "NetworkManager: onResponse");
                if (response.code() == 200) {
                    Log.i(APP_TAG, "NetworkManager: onResponse: response code is successful");
                    ResponseBody responseBody = response.body();
                    try {
                        if (responseBody != null) {
                            JSONObject object = new JSONObject(responseBody.string());
                            String login = object.getString("login");
                            String password = object.getString("password");
                            responseBody.close();
                            result.onSuccess(login, password);
                        }
                    } catch (JSONException e) {
                        Log.e(APP_TAG, "NetworkManager: onResponse: failed to get credentials from response");
                    }

                } else {
                    Log.e(APP_TAG, "NetworkManager: onResponse: code = " + response.code());
                    result.onFailure();
                }

            }
        });
    }
}
