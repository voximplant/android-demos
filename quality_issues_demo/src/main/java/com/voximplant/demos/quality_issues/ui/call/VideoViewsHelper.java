package com.voximplant.demos.quality_issues.ui.call;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.voximplant.demos.quality_issues.R;

import org.webrtc.SurfaceViewRenderer;

import java.util.HashMap;
import java.util.Map;

class VideoViewsHelper {

    private final Context mContext;
    private final LinearLayout mVideoViewsLayout;
    private SurfaceViewRenderer mLocalVideoView;
    boolean mIsExpanded = false;

    // stream id and SurfaceViewRenderers+
    private final HashMap<String, SurfaceViewRenderer> mRemoteVideoViews = new HashMap<>();

    VideoViewsHelper(Context context, LinearLayout videoViewsLayout) {
        mContext = context;
        mVideoViewsLayout = videoViewsLayout;
    }

    SurfaceViewRenderer addLocalVideoView() {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater == null) {
            return null;
        }
        @SuppressLint("InflateParams") View videoRootLayout = layoutInflater.inflate(R.layout.video_view_item, null);
        mLocalVideoView = videoRootLayout.findViewById(R.id.video_view);
        videoRootLayout.setOnClickListener(this::changeExpand);
        TextView endpointName = videoRootLayout.findViewById(R.id.endpoint_name);
        endpointName.setVisibility(View.INVISIBLE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.weight = 1f;
        videoRootLayout.setLayoutParams(lp);
        mVideoViewsLayout.addView(videoRootLayout, 0);
        return mLocalVideoView;
    }

    SurfaceViewRenderer removeLocalVideoView() {
        if (mLocalVideoView != null) {
            mVideoViewsLayout.removeView((View) mLocalVideoView.getParent());
        }
        return mLocalVideoView;
    }

    SurfaceViewRenderer addRemoteVideoView(String id, String displayName) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater == null) {
            return null;
        }
        @SuppressLint("InflateParams") View videoRootLayout = layoutInflater.inflate(R.layout.video_view_item, null);
        SurfaceViewRenderer remoteVideoView = videoRootLayout.findViewById(R.id.video_view);
        videoRootLayout.setOnClickListener(this::changeExpand);
        TextView endpointName = videoRootLayout.findViewById(R.id.endpoint_name);
        endpointName.setText(displayName);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.weight = 1f;
        videoRootLayout.setLayoutParams(lp);
        mRemoteVideoViews.put(id, remoteVideoView);
        mVideoViewsLayout.addView(videoRootLayout);
        return remoteVideoView;
    }

    SurfaceViewRenderer removeRemoteVideoView(String id) {
        SurfaceViewRenderer videoView = null;
        for (Map.Entry<String, SurfaceViewRenderer> entry : mRemoteVideoViews.entrySet()) {
            if (entry.getKey().equals(id)) {
                videoView = entry.getValue();
                mVideoViewsLayout.removeView((View) videoView.getParent());
            }
        }
        if (videoView != null) {
            mRemoteVideoViews.remove(id);
        }
        return videoView;
    }

    void changeExpand(View videoRootLayout) {
        ConstraintLayout constraintLayout = (ConstraintLayout) mVideoViewsLayout.getParent();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        if (!mIsExpanded) {
            for (int i = 0; i < mVideoViewsLayout.getChildCount(); i++) {
                View child = mVideoViewsLayout.getChildAt(i);
                if (child instanceof ConstraintLayout) {
                    if (child != videoRootLayout) {
                        child.setVisibility(View.GONE);
                    }
                }
            }
            constraintSet.connect(R.id.video_views_layout, ConstraintSet.BOTTOM, R.id.current_issues_view, ConstraintSet.TOP);
            constraintSet.applyTo(constraintLayout);
            mIsExpanded = true;
        } else {
            for (int i = 0; i < mVideoViewsLayout.getChildCount(); i++) {
                View child = mVideoViewsLayout.getChildAt(i);
                if (child instanceof ConstraintLayout) {
                    child.setVisibility(View.VISIBLE);
                }
            }
            constraintSet.connect(R.id.video_views_layout, ConstraintSet.BOTTOM, R.id.guideline2, ConstraintSet.TOP);
            constraintSet.applyTo(constraintLayout);
            mIsExpanded = false;
        }

    }

    void removeAllVideoViews() {
        removeLocalVideoView();
        mRemoteVideoViews.clear();
        mVideoViewsLayout.removeAllViews();
    }
}
