/*
 * Copyright (c) 2011- 2020, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.videoconf.ui.conf;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import com.voximplant.demos.videoconf.R;

import org.webrtc.SurfaceViewRenderer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.voximplant.demos.videoconf.Constants.APP_TAG;
import static com.voximplant.demos.videoconf.Constants.SELF_ENDPOINT_ID;

class VideoViewsHelper {
    private static final int X = 0;
    private static final int Y = 0;
    private static final int WIDTH = 100;
    private static final int HEIGHT = 100;
    private static final int PADDING = 10;

    private Context mContext;
    private ViewGroup mParentView;

    // endpoint id and SurfaceViewRenderers
    private ConcurrentHashMap<String, SurfaceViewRenderer> mVideoViews = new ConcurrentHashMap<>();

    private int mViewColumns;
    private int mViewRows;

    VideoViewsHelper(Context context, ViewGroup viewGroup) {
        mContext = context;
        mParentView = viewGroup;
    }

    void addVideoView(String id, String displayName) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater == null) {
            return;
        }
        calculateVideoColumnsAndRows(mVideoViews.size() + 1);
        View videoRoot = layoutInflater.inflate(R.layout.video_view, null);
        mParentView.addView(videoRoot);
        PercentFrameLayout videoLayout = videoRoot.findViewById(R.id.video_view_layout);
        videoLayout.setPosition(X, Y, WIDTH, HEIGHT, PADDING);
        SurfaceViewRenderer videoView = videoRoot.findViewById(R.id.video_view);
        TextView nameView = videoRoot.findViewById(R.id.endpoint_name);
        nameView.setText(displayName);

        mVideoViews.put(id, videoView);

        rearrangeViews();
    }

    void removeVideoView(String id) {
        SurfaceViewRenderer removedRenderer = null;
        for (Map.Entry<String, SurfaceViewRenderer> entry : mVideoViews.entrySet()) {
            if (entry.getKey().equals(id)) {
                ViewParent viewParent = entry.getValue().getParent();
                if (viewParent instanceof PercentFrameLayout) {
                    removedRenderer = entry.getValue();
                    mParentView.removeView((View)viewParent);
                }
            }
        }
        if (removedRenderer != null) {
            mVideoViews.remove(id);
        }
        calculateVideoColumnsAndRows(mVideoViews.size());
        rearrangeViews();
    }

    void removeAllVideoViews() {
        for (Map.Entry<String, SurfaceViewRenderer> entry : mVideoViews.entrySet()) {
            removeVideoView(entry.getKey());
        }
    }

    SurfaceViewRenderer getRendererForEndpoint(String endpointId) {
        SurfaceViewRenderer renderer = null;
        for (Map.Entry<String, SurfaceViewRenderer> entry : mVideoViews.entrySet()) {
            if (entry.getKey().equals(endpointId)) {
                ViewParent viewParent = entry.getValue().getParent();
                if (viewParent instanceof PercentFrameLayout) {
                    renderer = entry.getValue();
                    break;
                }
            }
        }
        return renderer;
    }

    void updateVideoView(String id, String displayName) {
        SurfaceViewRenderer videoRenderer = mVideoViews.get(id);
        if (videoRenderer != null) {
            ViewParent parent = videoRenderer.getParent();
            TextView textView = ((View)parent).findViewById(R.id.endpoint_name);
            textView.setText(displayName);
        }
    }

    private void calculateVideoColumnsAndRows(int count) {
        int columns = 1;
        int rows = 1;
        if (count > 0 && count < 17) {
            columns = 4;
            int m = 3;
            while ((count -1) / columns < m) {
                columns--;
                m--;
            }
            rows = ((count - 1) / columns) + 1;
        }

        mViewColumns = columns;
        mViewRows = rows;

        Log.i(APP_TAG, "calculateVideoColumnsAndRows: columns = " + mViewColumns + ", rows = " + mViewRows);
    }

    private void rearrangeViews() {
        int x;
        int y;
        int y_coef = 0;

        int i = 0;
        //always keep local video view first
        SurfaceViewRenderer localView = mVideoViews.get(SELF_ENDPOINT_ID);
        if (localView != null) {
            PercentFrameLayout localParent = (PercentFrameLayout) localView.getParent();
            setPositionForView(localParent, X, Y,
                    WIDTH / mViewColumns,
                    HEIGHT / mViewRows,
                    PADDING);
            i = 1;
            if (100 / mViewColumns >= 99) {
                y_coef++;
            }
        }

        for (Map.Entry<String, SurfaceViewRenderer> entry : mVideoViews.entrySet()) {
            if (entry.getKey().equals(SELF_ENDPOINT_ID)) {
                continue;
            }
            int x_coef = (i % mViewColumns);
            x = X + (x_coef * 100 / mViewColumns);
            y = Y + (y_coef * 100 / mViewRows);
            if (x + (100 / mViewColumns) >= 99) {
                y_coef++;
            }
            PercentFrameLayout parent = (PercentFrameLayout) entry.getValue().getParent();
            setPositionForView(parent, x, y,
                    WIDTH / mViewColumns, HEIGHT / mViewRows, PADDING);

            Log.i(APP_TAG, "rearrangeViews: " + i + ", x = " + x + ", y = " + y  +
                    ", width = " + WIDTH / mViewColumns + ", height = " + HEIGHT / mViewRows);

            i++;
        }
    }

    private void setPositionForView(PercentFrameLayout view, int x, int y, int widthPercent, int heightPercent, int padding) {
        view.setPosition(x, y, widthPercent, heightPercent, padding);
        view.setVisibility(View.VISIBLE);
    }
}
