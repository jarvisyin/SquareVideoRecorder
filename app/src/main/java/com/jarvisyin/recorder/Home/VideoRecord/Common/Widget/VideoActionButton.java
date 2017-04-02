package com.jarvisyin.recorder.Home.VideoRecord.Common.Widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.jarvisyin.recorder.R;


/**
 * Created by jarvisyin on 16/11/30.
 */
public class VideoActionButton extends View {

    public static final String TAG = "VideoActionButton";

    private int mColorYellowN, mColorYellowP, mColorRed,mColorWindowBackground;
    private Paint mPaint;

    private final int TOUCH_IN_RECORD = 1;
    private final int TOUCH_IN_UNRECORD = 2;
    private final int TOUCH_OUT_RECORD = 3;
    private final int TOUCH_OUT_UNRECORD = 4;
    private final int UNABLE_RECORD = 5;

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
        mColorYellowN = getContext().getResources().getColor(
                R.color.yellow);
        mColorYellowP = getContext().getResources().getColor(R.color.yellow_d);
        mColorRed = getContext().getResources().getColor(R.color.red);
        mColorWindowBackground = getContext().getResources().getColor(R.color.video_window_background);
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


        int centerColor = 0;
        int outsideColor = 0;

        if (statu == TOUCH_OUT_UNRECORD ||
                statu == UNABLE_RECORD ||
                statu == TOUCH_IN_UNRECORD) {
            centerColor = mColorYellowN;
            outsideColor = 0xffffffff;
        } else if (statu == TOUCH_IN_RECORD) {
            centerColor = mColorYellowP;
            outsideColor = 0xffffffff;
        } else if (statu == TOUCH_OUT_RECORD) {
            centerColor = mColorRed;
            outsideColor = mColorRed;
        }

        mPaint.setColor(outsideColor);
        canvas.drawCircle(width / 2, height / 2, length / 2, mPaint);

        mPaint.setColor(mColorWindowBackground);
        canvas.drawCircle(width / 2, height / 2, length / 2 * 0.9f, mPaint);

        mPaint.setColor(centerColor);
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
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (statu == TOUCH_OUT_UNRECORD) {
                    actionTime = AnimationUtils.currentAnimationTimeMillis();
                    statu = TOUCH_IN_RECORD;
                    if (mActionListener != null) mActionListener.startRecord();
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
                        if (mActionListener != null) mActionListener.stopRecord();
                    }
                    invalidate();
                } else if (statu == TOUCH_OUT_RECORD) {
                    long stopTime = AnimationUtils.currentAnimationTimeMillis();
                    if (stopTime - actionTime > 1000) {
                        statu = TOUCH_OUT_UNRECORD;
                        if (mActionListener != null) mActionListener.stopRecord();
                        invalidate();
                    }
                } else if (TOUCH_IN_UNRECORD == statu) {
                    statu = TOUCH_OUT_UNRECORD;
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
        invalidate();
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public void startFail() {
        if (statu == TOUCH_IN_RECORD) {
            statu = TOUCH_IN_UNRECORD;
        } else if (statu == TOUCH_OUT_RECORD) {
            statu = TOUCH_OUT_UNRECORD;
        }
        invalidate();
    }

    public interface ActionListener {
        void startRecord();

        void stopRecord();
    }
}
