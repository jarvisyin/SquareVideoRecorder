package com.jarvisyin.recorder.Common.Component.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;

import com.jarvisyin.recorder.Common.Component.Activity.BaseActivity;
import com.jarvisyin.recorder.R;

/**
 * Created by jarvisyin on 16/2/16.
 */
public class BaseFragment extends Fragment {

    private boolean setBackClickListener = true;
    protected BaseActivity mActivity;

    @Override
    public void onAttach(Activity context) {
        super.onAttach( context);
        mActivity = (BaseActivity) getActivity();
    }

    public BaseActivity getBaseActivity() {
        return mActivity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (view == null) return;

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        if (view.getBackground() == null) {
            view.setBackgroundResource(R.color.window_background);
        }

    }


    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setSetBackClickListener(boolean bool) {
        this.setBackClickListener = bool;
    }

}
