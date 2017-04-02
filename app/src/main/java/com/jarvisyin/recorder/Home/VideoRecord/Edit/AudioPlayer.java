package com.jarvisyin.recorder.Home.VideoRecord.Edit;

import android.media.MediaPlayer;


import com.jarvisyin.recorder.Home.VideoRecord.BlockInfo;
import com.jarvisyin.recorder.Home.VideoRecord.VideoRecordActivity;

import java.util.List;

/**
 * Created by jarvisyin on 16/12/1.
 */
public class AudioPlayer {

    private static final String TAG = "VShopVideo AudioPlayer";

    private final VideoRecordActivity mContext;
    private MediaPlayer mRecordAudioPlayer;
    private List<BlockInfo> mBlockInfos;

    public AudioPlayer(VideoRecordActivity context) {
        mContext = context;
        mBlockInfos = mContext.getBlockInfos();
    }

    public void preprare() throws Exception {
        mRecordAudioPlayer = new MediaPlayer();
        mRecordAudioPlayer.setDataSource(mContext.getProcessingAudioPath());
        mRecordAudioPlayer.prepare();
    }

    public void start() {
        if (mRecordAudioPlayer != null) mRecordAudioPlayer.start();
    }

    public void pause() {
        if (mRecordAudioPlayer != null) mRecordAudioPlayer.pause();
    }

    public void release() {
        if (mRecordAudioPlayer != null) mRecordAudioPlayer.release();
    }
}
