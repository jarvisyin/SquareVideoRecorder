package com.jarvisyin.squarevideorecorder.Edit;

import android.media.MediaPlayer;
import android.util.Log;

import com.jarvisyin.squarevideorecorder.BlockInfo;
import com.jarvisyin.squarevideorecorder.MainActivity;

import java.util.List;

/**
 * Created by jarvisyin on 16/12/1.
 */
public class AudioPlayer implements MediaPlayer.OnCompletionListener {

    private static final String TAG = "AudioPlayer";

    private final MainActivity mContext;
    private MediaPlayer mMediaPlayer;
    private List<BlockInfo> mBlockInfos;
    private int position = 0;

    public AudioPlayer(MainActivity context) {
        mContext = context;
        mBlockInfos = mContext.getBlockInfos();
    }

    public void preprare() throws Exception {
        BlockInfo blockInfo = mBlockInfos.get(position);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDataSource(blockInfo.getAudioFile().getPath()/*"/sdcard/z/z_music/audio_caihong.m4a"*/);
        mMediaPlayer.prepare();
        mMediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        try {
            position++;
            if (position >= mBlockInfos.size()) {
                return;
            }
            BlockInfo blockInfo = mBlockInfos.get(position);
            mp.reset();
            mp.setDataSource(blockInfo.getAudioFile().getPath()/*"/sdcard/z/z_music/audio_pugongyindeyueding.m4a"*/);
            mp.prepare();

            mp.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        mMediaPlayer.start();
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void release() {
        mMediaPlayer.release();
    }

}
