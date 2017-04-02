package com.jarvisyin.recorder.Common.Widget.Dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.jarvisyin.recorder.R;

/**
 * Created by jarvisyin on 16/3/1.
 */
public class Horizontal1ButtonDialog extends BaseDialog implements View.OnClickListener {


    private TextView tvContent;
    private View btn;

    public Horizontal1ButtonDialog(Context context, String content) {
        super(context);
        init();
        setContent(content);
    }


    protected void init() {
        View mainView = LayoutInflater.from(mContext).inflate(R.layout.layout_dialog_button1, null);
        tvContent = (TextView) mainView.findViewById(R.id.content);

        btn = mainView.findViewById(R.id.button);
        btn.setOnClickListener(this);

        setContentView(mainView);
    }

    public void setContent(String content) {
        tvContent.setText(content);
    }

    public void setBtnClickListener(View.OnClickListener listener) {
        btn.setOnClickListener(listener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                btnClick(v);
                break;
        }
    }

    public void btnClick(View v) {
        dismiss();
    }

}
