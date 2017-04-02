package com.jarvisyin.recorder.Common.Widget.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.jarvisyin.recorder.R;

/**
 * Created by jarvisyin on 16/2/23.
 */
public class ProgressDialog extends Dialog {


    private TextView message;

    public ProgressDialog(Context context) {
        super(context, R.style.dialogStyle);
    }

    public ProgressDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected ProgressDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_progress_dialog);
        message = (TextView) findViewById(R.id.message);
        setCanceledOnTouchOutside(false);
    }


    public void setMessage(String msg) {
        message.setText(msg);
        message.setVisibility(View.VISIBLE);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

}
