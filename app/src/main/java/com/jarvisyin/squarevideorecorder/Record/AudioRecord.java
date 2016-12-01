package com.jarvisyin.squarevideorecorder.Record;

import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by jarvisyin.
 */
public class AudioRecord {
    private MediaRecorder mRecorder = null;
    private ActionHandler mActionHandler;

    public AudioRecord() {
        new ActionThread().start();
    }

    public void prepare(String fileName) {
        Message msg = mActionHandler.obtainMessage();
        msg.what = ActionHandler.STATU_PREPARE;
        msg.obj = fileName;
        mActionHandler.sendMessage(msg);
    }

    public void start() {
        mActionHandler.sendEmptyMessage(ActionHandler.STATU_START);
    }

    public void stop() {
        mActionHandler.sendEmptyMessage(ActionHandler.STATU_STOP);
    }

    public void release() {
        mActionHandler.sendEmptyMessage(ActionHandler.MSG_SHUTDOWN);
    }

    private class ActionThread extends Thread {
        @Override
        public void run() {
            Looper.prepare();
            mActionHandler = new ActionHandler();
            Looper.loop();

            mActionHandler.removeMessages(ActionHandler.STATU_START);
            mActionHandler.removeMessages(ActionHandler.STATU_STOP);
            mActionHandler.removeMessages(ActionHandler.MSG_SHUTDOWN);
            mActionHandler.removeMessages(ActionHandler.STATU_PREPARE);
            mActionHandler = null;
        }
    }

    private class ActionHandler extends Handler {

        private final static int STATU_START = 1;
        private final static int STATU_STOP = 2;
        private final static int MSG_SHUTDOWN = 3;
        private final static int STATU_PREPARE = 4;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATU_START:
                    start();
                    break;
                case STATU_STOP:
                    stop();
                    break;
                case MSG_SHUTDOWN:
                    Looper.myLooper().quit();
                    break;
                case STATU_PREPARE:
                    prepare(msg.obj.toString());
                    break;
            }
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
                stop();
            }
        }

        public void stop() {
            try {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
