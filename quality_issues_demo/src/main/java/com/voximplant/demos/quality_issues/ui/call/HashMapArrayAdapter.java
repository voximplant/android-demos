/*
 * Copyright (c) 2011- 2018, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.quality_issues.ui.call;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.voximplant.demos.quality_issues.R;
import com.voximplant.sdk.call.QualityIssue;
import com.voximplant.sdk.call.QualityIssueLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class HashMapArrayAdapter extends BaseAdapter {
    private final List<Map.Entry<QualityIssue, QualityIssueLevel>> mData = new ArrayList<>();
    private final Context mContext;

    HashMapArrayAdapter(Context context, Map<QualityIssue, QualityIssueLevel> data) {
        mContext = context;
        updateData(data);
    }

    void updateData(Map<QualityIssue, QualityIssueLevel> data) {
        mData.clear();
        for (Map.Entry<QualityIssue, QualityIssueLevel> entry : data.entrySet()) {
            if (entry.getKey().equals(QualityIssue.LOW_BANDWIDTH)) {
                continue;
            }
            mData.add(entry);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<QualityIssue, QualityIssueLevel> getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_grid_view, parent, false);
        } else {
            result = convertView;
        }

        Map.Entry<QualityIssue, QualityIssueLevel> item = getItem(position);
        final String issue;
        switch (item.getKey()) {
            case PACKET_LOSS:
                issue = mContext.getResources().getString(R.string.quality_issue_packet_loss);
                break;
            case LOCAL_VIDEO_DEGRADATION:
                issue = mContext.getResources().getString(R.string.quality_issue_local_video_degradation);
                break;
            case NO_AUDIO_SIGNAL:
                issue = mContext.getResources().getString(R.string.quality_issue_no_audio_signal);
                break;
            case CODEC_MISMATCH:
                issue = mContext.getResources().getString(R.string.quality_issue_codec_mismatch);
                break;
            case HIGH_MEDIA_LATENCY:
                issue = mContext.getResources().getString(R.string.quality_issue_high_media_latency);
                break;
            case ICE_DISCONNECTED:
                issue = mContext.getResources().getString(R.string.quality_issue_ice_disconnected);
                break;
            case NO_AUDIO_RECEIVE:
                issue = mContext.getResources().getString(R.string.quality_issue_no_audio_receive);
                break;
            case NO_VIDEO_RECEIVE:
                issue = mContext.getResources().getString(R.string.quality_issue_no_video_receive);
                break;
            default:
                issue = mContext.getResources().getString(R.string.quality_issue_unknown);
                break;
        }

        String level = mContext.getResources().getString(R.string.quality_issue_level_none);
        int color = mContext.getResources().getColor(R.color.colorGreen);
        switch (item.getValue()) {
            case MINOR:
                level = mContext.getResources().getString(R.string.quality_issue_level_minor);
                color = mContext.getResources().getColor(R.color.colorYellow);
                break;
            case MAJOR:
                level = mContext.getResources().getString(R.string.quality_issue_level_major);
                color = mContext.getResources().getColor(R.color.colorOrange);
                break;
            case CRITICAL:
                level = mContext.getResources().getString(R.string.quality_issue_level_critical);
                color = mContext.getResources().getColor(R.color.colorRed);
                break;
            case NONE:
                level = mContext.getResources().getString(R.string.quality_issue_level_none);
                color = mContext.getResources().getColor(R.color.colorGreen);
                break;
        }

        ((TextView) result.findViewById(R.id.textQualityIssue)).setText(issue);
        ((TextView) result.findViewById(R.id.textQualityLevel)).setText(level);
        ((TextView) result.findViewById(R.id.textQualityLevel)).setTextColor(color);

        return result;
    }
}
