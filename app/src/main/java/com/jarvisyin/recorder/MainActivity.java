package com.jarvisyin.recorder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.jarvisyin.recorder.Common.Component.Activity.BaseActivity;
import com.jarvisyin.recorder.Home.VideoRecord.VideoRecordActivity;
import com.jarvisyin.recorder.Home.VideoStore.VideoStoreActivity;


/**
 * Created by jarvisyin on 16/2/23.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    public final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_video_record).setOnClickListener(this);
        findViewById(R.id.btn_video_store).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_video_record:
                startActivity(new Intent(this, VideoRecordActivity.class));
                break;
            case R.id.btn_video_store:
                startActivity(new Intent(this, VideoStoreActivity.class));
                break;
        }
    }
}
