package com.jarvisyin.squarevideorecorder;

import java.io.File;

/**
 * Created by jarvisyin on 16/11/22.
 */
public class BlockInfo {
    private File audioFile;
    private File videoFile;
    private File resultFile;

    private long startTime;
    private long stopTime;


    public BlockInfo(File parent) {
        audioFile = new File(parent, "audio.m4a");
        videoFile = new File(parent, "video.mp4");
        resultFile = new File(parent, "result.mp4");
    }

    public File getAudioFile() {
        return audioFile;
    }

    public File getVideoFile() {
        return videoFile;
    }

    public File getResultFile() {
        return resultFile;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public long getTimeSpan() {
        return stopTime - startTime;
    }
}
