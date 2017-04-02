package com.jarvisyin.recorder.Home.VideoStore;

import android.os.Bundle;

import com.jarvisyin.recorder.Common.Component.Activity.BaseActivity;

public class VideoStoreActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String videoPath = getIntent().getStringExtra("videoPath");
        String imagePath = getIntent().getStringExtra("imagePath");

        if (videoPath != null && imagePath != null) {
            /*PushGoodsFragment fragment =  PushGoodsFragment.newInstance(videoPath,imagePath);
            mActivity.addFragmentWhenActStart(this, fragment);*/

        } else {
            addFragmentWhenActStart(new VideoStoreFragment());
        }
    }
}
