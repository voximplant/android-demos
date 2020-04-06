/*
 * Copyright (c) 2011- 2020, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.videoconf.ui.conf;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.webrtc.SurfaceViewRenderer;

public class PercentFrameLayout extends ViewGroup {
    private int mXPercent = 0;
    private int mYPercent = 0;
    private int mWidthPercent = 100;
    private int mHeightPercent = 100;
    private int mPadding = 0;

    public PercentFrameLayout(Context context) {
        super(context);
    }

    public PercentFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PercentFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPosition(int xPercent, int yPercent, int widthPercent, int heightPercent, int padding) {
        mXPercent = xPercent;
        mYPercent = yPercent;
        mWidthPercent = widthPercent;
        mHeightPercent = heightPercent;
        mPadding = padding;
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = getDefaultSize(Integer.MAX_VALUE, widthMeasureSpec);
        final int height = getDefaultSize(Integer.MAX_VALUE, heightMeasureSpec);
        setMeasuredDimension(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));

        final int childWidthMeasureSpec =
                MeasureSpec.makeMeasureSpec(width * mWidthPercent / 100, MeasureSpec.AT_MOST);
        final int childHeightMeasureSpec =
                MeasureSpec.makeMeasureSpec(height * mHeightPercent / 100, MeasureSpec.AT_MOST);
        for (int i = 0; i < getChildCount(); ++i) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = right - left;
        final int height = bottom - top;
        // Sub-rectangle specified by percentage values.
        final int subWidth = width * mWidthPercent / 100;
        final int subHeight = height * mHeightPercent / 100;
        final int subLeft = left + width * mXPercent / 100;
        final int subTop = top + height * mYPercent / 100;

        int rendererLeft = 0, rendererTop = 0;
        for (int i = 0; i < getChildCount(); ++i) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                if (child instanceof SurfaceViewRenderer) {
                    final int childWidth = child.getMeasuredWidth();
                    final int childHeight = child.getMeasuredHeight();
                    // Center child both vertically and horizontally.
                    final int childLeft = subLeft + (subWidth - childWidth) / 2 + mPadding;
                    final int childTop = subTop + (subHeight - childHeight) / 2 + mPadding;
                    rendererLeft = childLeft;
                    rendererTop = childTop;
                    child.layout(childLeft, childTop, childLeft + childWidth - 2 * mPadding, childTop + childHeight - 2 * mPadding);
                }
                if (child instanceof TextView) {
                    final int childWidth = child.getMeasuredWidth();
                    final int childHeight = child.getMeasuredHeight();
                    child.layout(rendererLeft + mPadding, rendererTop + mPadding, rendererLeft + mPadding + childWidth , rendererTop + mPadding + childHeight);
                }
            }
        }
    }
}
