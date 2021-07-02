/*
 * Copyright (c) 2011 - 2019, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.audiocall.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.widget.ImageButton;

import com.voximplant.demos.audiocall.R;

public class UIHelper {
    @SuppressLint("UseCompatLoadingForDrawables")
    public static void changeButtonColor(Context context, ImageButton button, int action, boolean isRed) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                button.setColorFilter(context.getResources().getColor(R.color.colorWhite));
                button.setBackground(isRed
                        ? context.getResources().getDrawable(R.drawable.button_image_red_active)
                        : context.getResources().getDrawable(R.drawable.button_image_active));
                break;
            case MotionEvent.ACTION_UP:
                button.setColorFilter(isRed
                        ? context.getResources().getColor(R.color.colorRed)
                        : context.getResources().getColor(R.color.colorButton));
                button.setBackground(isRed
                        ? context.getResources().getDrawable(R.drawable.button_image_red_passive)
                        : context.getResources().getDrawable(R.drawable.button_image_passive));
                break;
        }
    }
}
