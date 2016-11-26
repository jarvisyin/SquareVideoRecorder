package com.jarvisyin.squarevideorecorder.Common.Widget.ActionBar;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jarvisyin on 16/8/17.
 */
public class TopView extends View {
    public static int statusBarHeight = 0;


    public TopView(Context context) {
        super(context);
        init(context);
    }

    public TopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (statusBarHeight == 0
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                ) {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                //statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
        }
        if (getBackground() == null) {
            setBackgroundColor(0xFF000000);
        }
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        params.height = statusBarHeight;
        super.setLayoutParams(params);
    }
}
