package com.jarvisyin.recorder.Home.VideoRecord.Common.Utils.FFmpeg;

import android.content.Context;
import android.os.AsyncTask;

import com.jarvisyin.recorder.Common.Utils.JYLog;
import com.yixia.camera.UtilityAdapter;
import com.yixia.camera.VCamera;

/**
 * Created by jarvisyin on 16/12/12.
 */
class FFmpegVCamera extends VShopFFmpeg {
    @Override
    public void initialize(final Context context, final Callback callback) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (callback != null) callback.onProgress(null);
                VCamera.initialize(context.getApplicationContext());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (callback != null) callback.onSuccess(null);
            }
        }.execute();
    }

    @Override
    public void execute(final String cmd, final Callback callback) {
        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... params) {
                if (callback != null) callback.onProgress(null);
                int result = UtilityAdapter.FFmpegRun("", "ffmpeg " + cmd);
                return result;
            }

            @Override
            protected void onPostExecute(Integer result) {
                JYLog.i(TAG, "result = %s", result);
                if (result == 1) {
                    if (callback != null) callback.onFailure(null);
                } else {
                    if (callback != null) callback.onSuccess(null);
                }
            }
        }.execute();
    }
}
