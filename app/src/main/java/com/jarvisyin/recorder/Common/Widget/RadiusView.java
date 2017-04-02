package com.jarvisyin.recorder.Common.Widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.jarvisyin.recorder.R;


/**
 * Created by jarvisyin on 17/1/5.
 */

public class RadiusView extends View {
    private Paint mPaint;
    private RectF mRect;
    private int mRadiusValue;
    private int mOutSideColor;
    private int mLineColor;
    private int mLineWidth;
    private PorterDuffXfermode mMode;

    public RadiusView(Context context) {
        super(context);
        init();
    }

    public RadiusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RadiusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mRect = new RectF();
        mRadiusValue = (int) (7 * getContext().getResources().getDisplayMetrics().density + 0.5f);
        mOutSideColor = getContext().getResources().getColor(R.color.video_window_background);
        mLineColor = getContext().getResources().getColor(R.color.yellow);
        mLineWidth = (int) (3 * getContext().getResources().getDisplayMetrics().density + 0.5f);
        mMode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int sc = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);

        mPaint.setColor(mOutSideColor);//红色
        mRect.set(0, 0, getWidth(), getHeight());
        canvas.drawRect(mRect, mPaint);

        mPaint.setXfermode(mMode);

        mRect.set(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(mRect, mRadiusValue, mRadiusValue, mPaint);

        mPaint.setXfermode(null);

        canvas.restoreToCount(sc);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mLineWidth);
        mPaint.setColor(mLineColor);
        canvas.drawRoundRect(mRect, mRadiusValue, mRadiusValue, mPaint);
    }
}
