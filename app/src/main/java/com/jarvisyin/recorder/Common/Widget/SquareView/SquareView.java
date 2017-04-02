package com.jarvisyin.recorder.Common.Widget.SquareView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jarvisyin on 16/4/4.
 */
public class SquareView extends View {


    public SquareView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
