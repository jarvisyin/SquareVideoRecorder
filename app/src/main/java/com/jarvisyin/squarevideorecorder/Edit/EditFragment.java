package com.jarvisyin.squarevideorecorder.Edit;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jarvisyin.squarevideorecorder.BlockInfo;
import com.jarvisyin.squarevideorecorder.Common.Component.Fragment.BaseFragment;
import com.jarvisyin.squarevideorecorder.Common.Utils.JToast;
import com.jarvisyin.squarevideorecorder.Common.Widget.ActionBar.ActionBar;
import com.jarvisyin.squarevideorecorder.MainActivity;
import com.jarvisyin.squarevideorecorder.R;
import com.jarvisyin.squarevideorecorder.Record.RecordFragment;

import java.util.List;

/**
 * Created by Jarvis.
 */
public class EditFragment extends BaseFragment {
    public static final String TAG = EditFragment.class.getName();
    private MainActivity mContext;
    private MediaPlayer mAudioPlay1, mAudioPlay2;
    private List<BlockInfo> mBlockInfos;
    private int position = 0;
    private EditHandler mHandler = new EditHandler();
    private MediaPlayer mVideoPlayer;
    private AudioPlayer mAudioPlayer;
    private VideoSurfaceView mVideoSurfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (MainActivity) getBaseActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBlockInfos = mContext.getBlockInfos();
        if (mBlockInfos.isEmpty()) {
            //TODO exit and show error msg
            return;
        }
        position = 0;

        ActionBar actionBar = (ActionBar) view.findViewById(R.id.action_bar);
        actionBar.setBtnBackOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBaseActivity().replaceFragmentWithAnim(new RecordFragment());
            }
        });

        mAudioPlayer = new AudioPlayer(mContext);
        try {
            mAudioPlayer.preprare();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO exit and show error msg
        }

        ViewGroup squareLayout = (ViewGroup) view.findViewById(R.id.square_layout);

        mVideoPlayer = new MediaPlayer();
        try {
            BlockInfo blockInfo = mBlockInfos.get(position);
            mVideoPlayer.setDataSource(blockInfo.getVideoFile().getPath());
            mVideoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    try {
                        position++;
                        if (position >= mBlockInfos.size()) {
                            return;
                        }
                        BlockInfo blockInfo = mBlockInfos.get(position);
                        mp.reset();
                        mp.setDataSource(blockInfo.getVideoFile().getPath());
                        mp.prepare();
                        mp.start();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                        //TODO exit and show error msg
                    }
                }
            });

            mVideoSurfaceView = new VideoSurfaceView(getBaseActivity(), mVideoPlayer);
            squareLayout.addView(mVideoSurfaceView);
        } catch (Exception e) {
            e.printStackTrace();
            //TODO exit and show error msg
        }

        mAudioPlayer.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVideoSurfaceView != null) mVideoSurfaceView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
        mAudioPlayer.release();
    }

    private static class EditHandler extends Handler {

        public static final int SHOW_ERROR_MSG = 1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_ERROR_MSG:
                    //TODO show error dialog
                    JToast.show(msg.obj.toString());
                    break;
            }
        }
    }
}
