package com.jarvisyin.squarevideorecorder.Common.Widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.jarvisyin.squarevideorecorder.R;

/**
 * Created by jarvisyin on 16/11/30.
 */
public class VideoActionButton extends View {

    public static final String TAG = "VideoActionButton";

    private int mColorYellowN, mColorYellowP, mColorRed;
    private Paint mPaint;

    private final int TOUCH_IN_RECORD = 1;
    private final int TOUCH_OUT_RECORD = 2;
    private final int TOUCH_OUT_UNRECORD = 3;
    private final int UNABLE_RECORD = 4;

    private int statu = TOUCH_OUT_UNRECORD;

    private long actionTime;
    private ActionListener mActionListener;

    public VideoActionButton(Context context) {
        super(context);
        init();
    }

    public VideoActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mColorYellowN = getContext().getResources().getColor(R.color.yellow);
        mColorYellowP = getContext().getResources().getColor(R.color.yellow_d);
        mColorRed = getContext().getResources().getColor(R.color.red);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int width = getWidth();
        final int height = getHeight();

        int length = width;
        if (width > height) {
            length = height;
        }


        int color = 0;

        if (statu == TOUCH_OUT_UNRECORD || statu == UNABLE_RECORD) {
            color = mColorYellowN;
        } else if (statu == TOUCH_IN_RECORD) {
            color = mColorYellowP;
        } else if (statu == TOUCH_OUT_RECORD) {
            color = mColorRed;
        }

        mPaint.setColor(color);
        canvas.drawCircle(width / 2, height / 2, length / 2, mPaint);

        mPaint.setColor(0xff000000);
        canvas.drawCircle(width / 2, height / 2, length / 2 * 0.9f, mPaint);

        mPaint.setColor(color);
        canvas.drawCircle(width / 2, height / 2, length / 2 * 0.8f, mPaint);

        //(float left, float top, float right, float bottom)
        if (statu == TOUCH_OUT_RECORD) {
            mPaint.setColor(0xffffffff);
            canvas.drawRect(length * 0.35f, length * 0.3f, length * 0.45f, length * 0.7f, mPaint);
            canvas.drawRect(length * 0.55f, length * 0.3f, length * 0.65f, length * 0.7f, mPaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(TAG, "statu = " + statu);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (statu == TOUCH_OUT_UNRECORD) {
                    actionTime = AnimationUtils.currentAnimationTimeMillis();
                    mActionListener.startRecord();
                    statu = TOUCH_IN_RECORD;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (statu == TOUCH_IN_RECORD) {
                    long stopTime = AnimationUtils.currentAnimationTimeMillis();
                    if (stopTime - actionTime < 1000) {
                        statu = TOUCH_OUT_RECORD;
                    } else {
                        statu = TOUCH_OUT_UNRECORD;
                        mActionListener.stopRecord();
                    }
                    invalidate();
                } else if (statu == TOUCH_OUT_RECORD) {
                    long stopTime = AnimationUtils.currentAnimationTimeMillis();
                    if (stopTime - actionTime > 1000) {
                        statu = TOUCH_OUT_UNRECORD;
                        mActionListener.stopRecord();
                        invalidate();
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            statu = TOUCH_OUT_UNRECORD;
        } else {
            statu = UNABLE_RECORD;
        }
        Log.i(TAG, "statu = " + statu);
        invalidate();
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public interface ActionListener {
        void startRecord();

        void stopRecord();
    }
}
