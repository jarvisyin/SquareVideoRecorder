package com.jarvisyin.recorder.Common.Widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.jarvisyin.recorder.R;

/**
 * Created by jarvisyin on 16/2/29.
 */
public class ProgressView extends FrameLayout implements View.OnClickListener {
    private TextView errorMsg;
    private View progressBar;
    private View errorView;
    private View tryAgain;
    private OnClickListener tryAgainListener;
    private boolean touchAble = true;

    public ProgressView(Context context) {
        super(context);
        init();
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundResource(R.color.window_background);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_progress_view, this);
        progressBar = view.findViewById(R.id.progress_bar);
        errorView = view.findViewById(R.id.error_view);
        errorMsg = (TextView) view.findViewById(R.id.error_msg);
        tryAgain = view.findViewById(R.id.try_again);
        tryAgain.setOnClickListener(this);

        setBackgroundColor(0xffffffff);
    }

    public void setTryAgainListener(OnClickListener listener) {
        this.tryAgainListener = listener;
        if (listener == null) {
            tryAgain.setVisibility(View.GONE);
        } else {
            tryAgain.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchAble;
    }

    @Override
    public void onClick(View v) {
        if (tryAgainListener != null) {
            tryAgainListener.onClick(v);
            showProgressDialog();
        }
    }

    public void showErrorMsg(String message) {
        setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        errorMsg.setText(message);
        tryAgain.setVisibility(VISIBLE);
    }

    public void setMsg(String message) {
        setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        errorMsg.setText(message);
        tryAgain.setVisibility(GONE);
    }

    public void showProgressDialog() {
        setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        tryAgain.setVisibility(GONE);
    }

    public void hide() {
        setVisibility(View.GONE);
    }

}
