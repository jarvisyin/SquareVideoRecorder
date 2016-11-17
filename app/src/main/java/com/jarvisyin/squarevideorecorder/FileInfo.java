package com.jarvisyin.squarevideorecorder;

import java.io.File;

/**
 * Created by jarvisyin on 16/11/18.
 */
public class FileInfo {
    private File audioFile;
    private File videoFile;
    private File resultFile;

    public File getAudioFile() {
        return audioFile;
    }

    public File getVideoFile() {
        return videoFile;
    }

    public File getResultFile() {
        return resultFile;
    }

    public FileInfo() {
        File file = new File("/sdcard/SquareVideoRecord");
        if (!file.exists()) {
            file.mkdirs();
        }
        audioFile = new File(file, "audio");
        videoFile = new File(file, "video");
        resultFile = new File(file, "result.mp4");
    }
}
