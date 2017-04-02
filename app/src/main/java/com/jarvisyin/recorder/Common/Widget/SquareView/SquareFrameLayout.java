package com.jarvisyin.recorder.Common.Widget.SquareView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by jarvisyin on 16/3/15.
 */
public class SquareFrameLayout extends FrameLayout {


    public SquareFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);


    }
}
