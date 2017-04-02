package com.jarvisyin.recorder.Home.VideoRecord.Common.Utils.FFmpeg;

import android.content.Context;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.jarvisyin.recorder.Common.Utils.JYLog;

/**
 * Created by jarvisyin on 16/12/12.
 */
class FFmpegHiteshsondhi88 extends VShopFFmpeg {
    private Context mContext;

    @Override
    public void initialize(Context context, final Callback callback) {
        mContext = context.getApplicationContext();
        FFmpeg ffmpeg = FFmpeg.getInstance(mContext);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                    if (callback != null) callback.onFailure(null);
                }

                @Override
                public void onSuccess() {
                    if (callback != null) callback.onSuccess(null);
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
            if (callback != null) callback.onFailure(e.getMessage());
            JYLog.d(TAG, "loadFFMPEG: FFmpegNotSupportedException: %s", e.toString());
        }
    }

    @Override
    public void execute(String cmd, final Callback callback) {
        FFmpeg ffmpeg = FFmpeg.getInstance(mContext);
        try {
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onProgress(String message) {
                    if (callback != null) callback.onProgress(message);
                }

                @Override
                public void onFailure(String message) {
                    if (callback != null) callback.onFailure(message);
                }

                @Override
                public void onSuccess(String message) {
                    if (callback != null) callback.onSuccess(message);
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            if (callback != null) callback.onFailure(e.getMessage());
            JYLog.d(TAG, "loadFFMPEG: FFmpegNotSupportedException: %s", e.toString());
        }
    }
}
