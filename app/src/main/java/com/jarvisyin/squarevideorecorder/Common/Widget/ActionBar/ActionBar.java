package com.jarvisyin.squarevideorecorder.Common.Widget.ActionBar;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jarvisyin.squarevideorecorder.Common.Component.Activity.BaseActivity;
import com.jarvisyin.squarevideorecorder.R;

/**
 * Created by jarvisyin on 16/8/10.
 */
/*
<com.hepai.vshopbuyer.Library.Widget.ActionBar.ActionBar
        xmlns:vshopbuyer="http://schemas.android.com/apk/res-auto"
        android:id="@+id/action_bar"
        android:layout_width="match_parent"
        android:layout_height="@dimen/actionbar_height"
        vshopbuyer:rightBtnText="编辑"
        vshopbuyer:title="商品收藏"/> */

public class ActionBar extends LinearLayout {
    private TextView tvTitle;
    private Button btnBack;
    private Button btnRight;

    private RelativeLayout relativeLayout;
    private View topPaddingView;

    private final int btnTextSize = 16;
    private final int titleTextSize = 16;

    private int appPadding;
    private int textColor0;
    private int actionBarHeight;

    public ActionBar(Context context) {
        super(context);
        init(context);
        initChildWidget(context);
    }

    public ActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        initChildWidget(context);
        initFromAttributes(context, attrs);
    }

    public ActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        initChildWidget(context);
        initFromAttributes(context, attrs);
    }

    private void init(Context context) {
        setBackgroundColor(0xff131313);
        //textColor0 = context.getResources().getColor(R.color.text_color0);
        textColor0 = 0xffffffff;
        appPadding = (int) (context.getResources().getDimension(R.dimen.app_padding) + 0.5f);
        actionBarHeight = (int) (context.getResources().getDimension(R.dimen.action_bar_height) + 0.5f);

        setOrientation(VERTICAL);
    }

    private void initChildWidget(Context context) {
        LayoutParams lParams;

        topPaddingView = new TopView(context);
        lParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(topPaddingView, lParams);

        relativeLayout = new RelativeLayout(context);
        lParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                actionBarHeight);
        addView(relativeLayout, lParams);

        RelativeLayout.LayoutParams rParams;
        tvTitle = new TextView(context);
        rParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        setButtonStyle(context, tvTitle, rParams);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, titleTextSize);
        relativeLayout.addView(tvTitle, rParams);

        btnBack = new Button(context);
        rParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rParams.addRule(RelativeLayout.CENTER_VERTICAL);
        btnBack.setText("返回");
        btnBack.setOnClickListener(mBtnBackOnClickListener);
        setButtonStyle(context, btnBack, rParams);
        relativeLayout.addView(btnBack, rParams);

        View line = new View(context);
        line.setId(R.id.line);
        rParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1);
        rParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //line.setBackgroundResource(R.color.action_bar_line_color);
        relativeLayout.addView(line, rParams);
    }

    private void initFromAttributes(Context context, AttributeSet attrs) {
        if (attrs == null) return;
        TypedArray typedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.ActionBar);

        String title = typedArray.getString(R.styleable.ActionBar_title);
        tvTitle.setText(title);

        String rightBtnText = typedArray.getString(R.styleable.ActionBar_rightBtnText);
        if (rightBtnText != null) {
            addRightBtn(rightBtnText);
        }

        boolean isTopViewShow = typedArray.getBoolean(R.styleable.ActionBar_isTopViewShow, true);
        if (isTopViewShow) {
            topPaddingView.setVisibility(VISIBLE);
        } else {
            topPaddingView.setVisibility(GONE);
        }

        typedArray.recycle();
    }

    public void addRightBtn(String text) {
        Context context = getContext();
        btnRight = new Button(getContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        setButtonStyle(context, btnRight, params);
        btnRight.setText(text);
        relativeLayout.addView(btnRight, params);
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        params.height = LayoutParams.WRAP_CONTENT;
        params.width = LayoutParams.MATCH_PARENT;
        super.setLayoutParams(params);
    }

    private void setButtonStyle(Context context, View view, RelativeLayout.LayoutParams params) {
        view.setBackgroundResource(R.drawable.x_button_bg_empty0);
        view.setPadding(appPadding, 0, appPadding, 0);
        if (view instanceof TextView || view instanceof Button) {
            TextView textView = (TextView) view;
            textView.setTextColor(textColor0);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, btnTextSize);
        }
    }

    public final OnClickListener mBtnBackOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            Context context = getContext();
            if (context instanceof BaseActivity) {
                BaseActivity activity = (BaseActivity) context;
                activity.popBackStack( );
            }
        }
    };

    public void setBtnBackOnClickListener(OnClickListener listener) {
        if (btnBack != null) {
            btnBack.setOnClickListener(listener);
        }
    }

    public void setTitile(String titile) {
        this.tvTitle.setText(titile);
    }

    public void setBtnRightOnClickListenet(OnClickListener listenet) {
        if (btnRight != null) {
            btnRight.setOnClickListener(listenet);
        }
    }

    public Button getBtnRight() {
        return btnRight;
    }
}
