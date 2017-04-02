package com.jarvisyin.recorder.Common.Component.Activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.jarvisyin.recorder.Common.Component.Fragment.BaseFragment;
import com.jarvisyin.recorder.R;

import java.util.Stack;

/**
 * Created by jarvisyin on 16/11/3.
 */
class FragmentController {

    private BaseActivity mActivity;
    private final Stack<Fragment> mFragments = new Stack<>();

    FragmentController(BaseActivity activity) {
        mActivity = activity;
    }

    public void addFragmentWithAnim(BaseFragment fragment) {
        addFragmentWithAnim(fragment, R.id.content);
    }

    public void addFragmentWithAnim(BaseFragment fragment, int contentId) {
        addFragmentWithAnim(fragment, contentId, null);
    }

    public void addFragmentWithAnim(BaseFragment fragment, int contentId, String TAG) {
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        setAddAnimation(ft);

        Fragment prevFragment = mFragments.peek();
        if (prevFragment != null) {
            ft.hide(prevFragment);
        }

        if (TextUtils.isEmpty(TAG)) {
            ft.add(contentId, fragment);
        } else {
            ft.add(contentId, fragment, TAG);
        }
        ft.commit();
        mFragments.push(fragment);
    }

    public void addFragmentWhenActStart(BaseFragment fragment) {
        addFragmentWhenActStart(fragment, null);
    }

    //TODO 这个要检查逻辑
    public void addFragmentWhenActStart(BaseFragment fragment, String TAG) {
        fragment.setSetBackClickListener(false);

        View view = mActivity.findViewById(R.id.content);
        if(view == null) {
            FrameLayout frameLayout = new FrameLayout(mActivity);
            frameLayout.setId(R.id.content);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mActivity.setContentView(frameLayout, params);
        }
        mActivity.getSupportFragmentManager();
        FragmentTransaction t = mActivity.getSupportFragmentManager().beginTransaction();

        if (TAG != null) {
            t.add(R.id.content, fragment, TAG);
        } else {
            t.add(R.id.content, fragment);
        }
        t.commit();

        mFragments.push(fragment);
    }

    public void replaceFragmentWithAnim(BaseFragment fragment) {
        replaceFragmentWithAnim(fragment, R.id.content);
    }

    public void replaceFragmentWithAnim(BaseFragment nextFragment, int contentId) {
        Fragment prevPragment = mFragments.pop();

        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        setAddAnimation(ft);
        ft.remove(prevPragment);
        ft.add(contentId, nextFragment);
        ft.commit();
        mFragments.push(nextFragment);
    }

    public void toFirstFragment() {
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        setRemoveAnimation(ft);
        while (mFragments.size() > 1) {
            Fragment fragment = mFragments.pop();
            ft.remove(fragment);
        }
        ft.show(mFragments.peek());
        ft.commit();
    }


    public void popBackStackWithoutAnima() {
        int count = mFragments.size();
        if (count < 2) {
            mActivity.finish();
        } else {
            Fragment fragment = mFragments.pop();
            FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
            transaction.remove(fragment);
            Fragment curFragment = mFragments.peek();
            transaction.show(curFragment);
            transaction.commit();
        }
    }

    public void popBackStack() {
        int count = mFragments.size();
        if (count < 2) {
            mActivity.finish();
        } else {
            Fragment fragment = mFragments.pop();
            FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
            setRemoveAnimation(transaction);
            transaction.remove(fragment);
            Fragment curFragment = mFragments.peek();
            transaction.show(curFragment);
            transaction.commit();
        }
    }

    public void clearStackandAddFragemnt(BaseFragment fragment) {
        clearStack();
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        ft.add(R.id.content, fragment);
        ft.commit();
        mFragments.push(fragment);
    }

    public void clearStack() {
        int count = mFragments.size();
        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        setRemoveAnimation(transaction);
        for (int i = 0; i < count; i++) {
            Fragment fragment = mFragments.pop();
            transaction.remove(fragment);
        }
        transaction.commit();
    }


    public Fragment findFragmentByTag(String TAG) {
        Fragment fragment = mActivity.getSupportFragmentManager().findFragmentByTag(TAG);
        return fragment;
    }

    private static void setAddAnimation(FragmentTransaction transaction) {
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
    }


    private static void setRemoveAnimation(FragmentTransaction transaction) {
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    public void release() {
        mFragments.clear();
        mActivity = null;
    }

}
