package com.jarvisyin.recorder.Home.VideoRecord.Common.Utils.FFmpeg;

import android.content.Context;

/**
 * Created by jarvisyin on 16/12/12.
 */
public abstract class VShopFFmpeg {

    public final static String TAG = "VShopVideo FFmpeg";

    private static VShopFFmpeg instance;

    public static VShopFFmpeg getInstance() {
        if (instance == null) {
            synchronized (VShopFFmpeg.class) {
                if (instance == null) {
                    instance = new FFmpegHiteshsondhi88();
                }
            }
        }
        return instance;
    }

    public abstract void initialize(Context context, Callback callback);

    public abstract void execute(String cmd, Callback callback);

    public interface Callback {
        void onProgress(String message);

        void onFailure(String message);

        void onSuccess(String message);
    }

}
