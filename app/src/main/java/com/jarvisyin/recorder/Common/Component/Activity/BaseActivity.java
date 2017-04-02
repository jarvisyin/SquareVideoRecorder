package com.jarvisyin.recorder.Common.Component.Activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;

import com.jarvisyin.recorder.AppContext;
import com.jarvisyin.recorder.Common.Component.Fragment.BaseFragment;
import com.jarvisyin.recorder.R;

/**
 * Created by jarvisyin on 16/2/16.
 */
public class BaseActivity extends FragmentActivity {
    protected AppContext appContext;
    private FragmentController mFragmentController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentController = new FragmentController(this);
        getWindow().getDecorView().setBackgroundResource(R.color.window_background);
        appContext = (AppContext) this.getApplicationContext();
    }


    protected void onResume() {
        super.onResume();
        appContext.setCurrentActivity(this);
    }

    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    protected void onDestroy() {
        clearReferences();
        mFragmentController.release();
        super.onDestroy();
    }

    private void clearReferences() {
        BaseActivity currActivity = appContext.getCurrentActivity();
        if (this.equals(currActivity))
            appContext.setCurrentActivity(null);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    public void onBackPressed() {
        mFragmentController.popBackStack();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected BaseActivity getActivity() {
        return this;
    }

    public void addFragmentWithAnim(BaseFragment fragment) {
        mFragmentController.addFragmentWithAnim(fragment);
    }

    public void addFragmentWithAnim(BaseFragment fragment, int contentId) {
        mFragmentController.addFragmentWithAnim(fragment, contentId);
    }

    public void addFragmentWithAnim(BaseFragment fragment, int contentId, String TAG) {
        mFragmentController.addFragmentWithAnim(fragment, contentId, TAG);
    }

    public void addFragmentWhenActStart(BaseFragment fragment) {
        mFragmentController.addFragmentWhenActStart(fragment);
    }

    public void addFragmentWhenActStart(BaseFragment fragment, String TAG) {
        mFragmentController.addFragmentWhenActStart(fragment, TAG);
    }

    public void replaceFragmentWithAnim(BaseFragment fragment, int contentId) {
        mFragmentController.replaceFragmentWithAnim(fragment, contentId);
    }

    public void replaceFragmentWithAnim(BaseFragment fragment) {
        mFragmentController.replaceFragmentWithAnim(fragment);
    }

    public void toFirstFragment() {
        mFragmentController.toFirstFragment();
    }

    public void popBackStack() {
        mFragmentController.popBackStack();
    }

    public void popBackStackWithoutAnima() {
        mFragmentController.popBackStackWithoutAnima();
    }

    public void clearStackandAddFragemnt(BaseFragment fragment) {
        mFragmentController.clearStackandAddFragemnt(fragment);
    }

    public void clearStack() {
        mFragmentController.clearStack();
    }

    public Fragment findFragmentByTag(String TAG) {
        return mFragmentController.findFragmentByTag(TAG);
    }

}
