package com.jarvisyin.recorder.Home.VideoRecord.Common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.jarvisyin.recorder.Common.Component.Fragment.BaseFragment;
import com.jarvisyin.recorder.R;

/**
 * Created by jarvisyin on 16/12/13.
 */
public class VideoBaseFragment extends BaseFragment {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (view == null) return;

        if (view.getBackground() == null) {
            view.setBackgroundResource(R.color.video_window_background);
        }

        super.onViewCreated(view, savedInstanceState);
    }
}
