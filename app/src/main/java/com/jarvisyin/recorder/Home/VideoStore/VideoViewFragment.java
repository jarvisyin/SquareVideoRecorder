package com.jarvisyin.recorder.Home.VideoStore;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.jarvisyin.recorder.Common.Component.Fragment.BaseFragment;
import com.jarvisyin.recorder.Common.Utils.DisplayUtils;

import java.io.File;

public class VideoViewFragment extends BaseFragment {


    private VideoView videoView;
    private MediaController mediaco;
    private String videoPath;

    public VideoViewFragment() {
        // Required empty public constructor
    }

    public static VideoViewFragment newInstance(String videoPath) {

        Bundle args = new Bundle();
        args.putString("videoPath", videoPath);
        VideoViewFragment fragment = new VideoViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoPath = getArguments().getString("videoPath");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FrameLayout view = new FrameLayout(mActivity);
        view.setBackgroundColor(0xff000000);

        File file = new File(videoPath);
        if (file.exists()) {
            videoView = new VideoView(mActivity);

            int width = DisplayUtils.getDisplayWidth(mActivity);
            int height = DisplayUtils.getDisplayHeight(mActivity);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, (height - width) / 2 + width);
            params.topMargin = (height - width) / 2;
            //params.gravity = Gravity.BOTTOM;
            view.addView(videoView, params);

            mediaco = new MediaController(mActivity);
            //VideoView与MediaController进行关联
            videoView.setVideoPath(file.getAbsolutePath());
            videoView.setMediaController(mediaco);
            mediaco.setMediaPlayer(videoView);
            mediaco.show();
            //让VideiView获取焦点
            videoView.requestFocus();
            videoView.start();
        }


        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoView != null) videoView.pause();
    }
}
