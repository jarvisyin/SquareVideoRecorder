package com.jarvisyin.recorder.Home.VideoRecord.Edit.Music;

import android.os.AsyncTask;

import com.jarvisyin.recorder.Buz.FileManager;
import com.jarvisyin.recorder.Common.Utils.Trinea.FileUtils;
import com.jarvisyin.recorder.Common.Utils.JYLog;
import com.jarvisyin.recorder.Home.VideoRecord.Common.Utils.FFmpeg.VShopFFmpeg;
import com.jarvisyin.recorder.Home.VideoRecord.VideoRecordActivity;
import com.yixia.camera.UtilityAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 根据 音乐的 ResourseId 生成 目标背景音乐
 * <p/>
 * 1.将ResourseId复制到本地Cache
 * 2.切割成合适长度
 * <p/>
 * Created by jarvisyin on 16/12/7.
 */
public class MusicProcessor extends Thread {
    private static final String TAG = "VShopVideo MusicProcessor";

    private final VideoRecordActivity mContext;
    private File mLocalMusicFile;
    private MusicInfo mMusicInfo;

    private Callback mCallback;

    public MusicProcessor(VideoRecordActivity context,
                          MusicInfo musicInfo) {
        mContext = context;
        mMusicInfo = musicInfo;
    }

    public void run() {
        fileCopy();
    }

    /**
     * 1.将raw文件复制到本地
     */
    private void fileCopy() {
        JYLog.i(TAG, "Copy File");

        mLocalMusicFile = new File(FileManager.getMusicPath(), mMusicInfo.getFileName());
        if (mLocalMusicFile.exists()) {
            cutAudio();
            return;
        }

        try {
            InputStream inputStream = mContext.getResources().openRawResource(mMusicInfo.getResourceId());
            OutputStream outputStream = new FileOutputStream(mLocalMusicFile);

            byte[] bytes = new byte[1024 * 512];//512kb 缓存
            int x;
            while ((x = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, x);
            }
            inputStream.close();
            outputStream.close();

            cutAudio();
        } catch (Exception e) {
            e.printStackTrace();
            if (mCallback != null) mCallback.onFailure(e.getMessage());
        }
    }


    private void cutAudio() {

        JYLog.i(TAG, "Cut Audio");

        File mInputFile = mLocalMusicFile;
        File mOutputFile = new File(mContext.getBackgroundAudioPath());
        if (mOutputFile.exists()) FileUtils.deleteFile(mOutputFile.getPath());

        double time = mContext.getCurrentWholeTimeSpan() / 1000000f;
        JYLog.i(TAG, "time = " + time);

        String cmd = String.format(" -i %s -ss 0 -t %4.2f -acodec copy %s",
                mInputFile.getPath(),
                time,
                mOutputFile.getPath());
        JYLog.i(TAG, "ffmpeg cmd = " + cmd);

        VShopFFmpeg.getInstance().execute(cmd, new VShopFFmpeg.Callback() {

            @Override
            public void onProgress(String message) {
                JYLog.d(TAG, "ffmpeg cmd: onProgress");
            }

            @Override
            public void onFailure(String message) {
                JYLog.d(TAG, "ffmpeg cmd: onFailure");
                if (mCallback != null) mCallback.onFailure(message);
            }

            @Override
            public void onSuccess(String message) {
                JYLog.d(TAG, "ffmpeg cmd: onSuccess");
                merge();
            }
        });
    }

    private void merge() {
        new AsyncTask<Void, Void, Integer>() {


            @Override
            protected Integer doInBackground(Void... params) {
                String cmd = String.format(" -i %s -i %s -filter_complex amix=inputs=2:duration=first:dropout_transition=2  -f mp4 %s",
                        mContext.getBackgroundAudioPath(),
                        mContext.getSourceAudioPath(),
                        mContext.getProcessingAudioPath());

                int result = UtilityAdapter.FFmpegRun("", cmd);
                return result;
            }

            @Override
            protected void onPostExecute(Integer result) {
                JYLog.i(TAG, "result = %s", result);
                if (result == 1) {
                    if (mCallback != null) mCallback.onFailure("unable to merge");
                } else {
                    if (mCallback != null) mCallback.onSuccess();
                }
            }
        }.execute();
    }

    public interface Callback {
        void onSuccess();

        void onProgress(String message);

        void onFailure(String message);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }
}
