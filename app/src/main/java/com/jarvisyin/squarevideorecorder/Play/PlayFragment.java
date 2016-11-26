package com.jarvisyin.squarevideorecorder.Play;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jarvisyin.squarevideorecorder.Common.Component.Fragment.BaseFragment;
import com.jarvisyin.squarevideorecorder.MainActivity;
import com.jarvisyin.squarevideorecorder.R;

/**
 * Created by Jarvis.
 */
public class PlayFragment extends BaseFragment {
    public static final String TAG = PlayFragment.class.getName();
    private MainActivity mContext;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //mSurfaceView = view.findViewById(R.id.surface_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        mContext = (MainActivity) getBaseActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
    }
}
