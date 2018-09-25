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
import java.util.Map;

class HashMapArrayAdapter extends BaseAdapter {
    private ArrayList mData = new ArrayList();
    private Context mContext;

    HashMapArrayAdapter(Context context, Map<QualityIssue, QualityIssueLevel> data) {
        mContext = context;
        mData.addAll(data.entrySet());
    }

    void updateData(Map<QualityIssue, QualityIssueLevel> data) {
        mData.clear();
        mData.addAll(data.entrySet());
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<QualityIssue, QualityIssueLevel> getItem(int position) {
        return (Map.Entry) mData.get(position);
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
            case LOW_BANDWIDTH:
                issue = mContext.getResources().getString(R.string.quality_issue_low_bandwidth);
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

        ((TextView) result.findViewById(R.id.text1)).setText(issue);
        ((TextView) result.findViewById(R.id.text2)).setText(level);
        ((TextView) result.findViewById(R.id.text2)).setTextColor(color);

        return result;
    }
}
