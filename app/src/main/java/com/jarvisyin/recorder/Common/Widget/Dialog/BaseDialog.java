package com.jarvisyin.recorder.Common.Widget.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.jarvisyin.recorder.Common.Utils.DisplayUtils;
import com.jarvisyin.recorder.R;

/**
 * Created by jarvisyin on 16/2/22.
 */
public class BaseDialog extends Dialog {
    protected final Context mContext;

    public BaseDialog(Context context) {
        this(context, R.style.dialogStyle);
    }

    public BaseDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    protected BaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setFullScreemContentView(int resId) {
        setFullScreemContentView(
                LayoutInflater.from(mContext).
                        inflate(resId,
                                null));
    }

    protected void setFullScreemContentView(View view) {
        setContentView(
                view,
                new FrameLayout.LayoutParams(
                        DisplayUtils.getDisplayWidth(mContext),
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
    }
}
