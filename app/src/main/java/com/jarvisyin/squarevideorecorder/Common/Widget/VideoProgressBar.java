package com.jarvisyin.squarevideorecorder.Common.Widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.jarvisyin.squarevideorecorder.BlockInfo;
import com.jarvisyin.squarevideorecorder.MainActivity;
import com.jarvisyin.squarevideorecorder.R;
import com.jarvisyin.squarevideorecorder.Common.Utils.DisplayUtils;

import java.util.List;

/**
 * Created by jarvisyin on 16/4/8.
 */
public class VideoProgressBar extends View {

    private String TAG = "VideoProgressBar";

    private int orangeColor;
    private int greyColor;
    private int whiteColor;

    private int dp1;

    private Paint paint = new Paint();


    public VideoProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        orangeColor = getContext().getResources().getColor(R.color.yellow);
        greyColor = 0xff909090;
        whiteColor = 0xffffffff;

        paint.setAntiAlias(true); //抗锯齿
        dp1 = DisplayUtils.dip2px(getContext(), 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (!(getContext() instanceof MainActivity)) {
            return;
        }
        MainActivity mContext = (MainActivity)getContext();
        
        List<BlockInfo> blockInfos = mContext.getBlockInfos();
        if (blockInfos == null || blockInfos.isEmpty()) return;

        final float width = canvas.getWidth();
        final float height = canvas.getHeight();
        paint.setStrokeWidth(height);//线的宽度

        paint.setColor(greyColor);
        canvas.drawColor(greyColor);

        final float whiteLinePercent = mContext.lessTimeSpan * 1.0f / mContext.wholeTimeSpan;
        paint.setColor(whiteColor);
        canvas.drawLine(width * whiteLinePercent - dp1, height / 2, width * whiteLinePercent, height / 2, paint);

        long sum = 0L;
        float p1, p2;
        paint.setColor(orangeColor);
        for (BlockInfo block : blockInfos) {

            p1 = sum * 1.0f / mContext.wholeTimeSpan;

            sum = sum + (block.getTimeSpan());
            p2 = sum * 1.0f / mContext.wholeTimeSpan;

            Log.i(TAG, "p1 = " + p1 + " p2 = " + p2 + " sum = " + sum + "  block.getTimeSpan() = " + block.getTimeSpan());

            canvas.drawLine(width * p1, height / 2, width * p2 - dp1 * 1.2f, height / 2, paint);
        }
    }

}
