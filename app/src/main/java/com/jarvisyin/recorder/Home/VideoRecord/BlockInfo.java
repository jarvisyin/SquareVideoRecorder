package com.jarvisyin.recorder.Home.VideoRecord;

import java.io.File;

/**
 * Created by jarvisyin on 16/11/22.
 */
public class BlockInfo {
    /**
     * 所处目录
     */
    private File parentFile;
    /**
     * 音频目录
     */
    private File audioFile;
    /**
     * 视频目录
     */
    private File videoFile;
    /**
     * 音视频Muxer的文件目录
     */
    private File resultFile;
    /**
     * 视频封面截图
     */
    private File coverFile;
    /**
     * 字幕图片目录
     */
    private File explainFile;
    /**
     * 字幕
     */
    private String explain;

    private long startTime;
    private long stopTime;
    private int frameCount;


    public BlockInfo(File parent) {
        parentFile = parent;
        audioFile = new File(parent, "audio.m4a");
        videoFile = new File(parent, "video.mp4");
        coverFile = new File(parent, "cover.png");
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

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public long getDuration() {
        return stopTime - startTime;
    }

    public void setExplainFile(File explainFile) {
        this.explainFile = explainFile;
    }

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

    public File getExplainFile() {
        return explainFile;
    }

    public File getParentFile() {
        return parentFile;
    }

    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public File getCoverFile() {
        return coverFile;
    }
}
