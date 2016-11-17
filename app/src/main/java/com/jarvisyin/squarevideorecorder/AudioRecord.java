package com.jarvisyin.squarevideorecorder;

import android.media.MediaRecorder;

/**
 * Created by jarvisyin.
 */
public class AudioRecord {
    private MediaRecorder mRecorder = null;

    public AudioRecord() {
    }

    public void prepare(String fileName) {
        try {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setOutputFile(fileName);
            mRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            mRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            mRecorder.stop();
            mRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
