package com.jarvisyin.squarevideorecorder;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jarvisyin.squarevideorecorder.Gles.CameraUtils;
import com.jarvisyin.squarevideorecorder.Gles.Drawable2d;
import com.jarvisyin.squarevideorecorder.Gles.EglCore;
import com.jarvisyin.squarevideorecorder.Gles.FullFrameRect;
import com.jarvisyin.squarevideorecorder.Gles.Texture2dProgram;
import com.jarvisyin.squarevideorecorder.Gles.WindowSurface;
import com.jarvisyin.squarevideorecorder.Widget.VideoProgressBar;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private final static String TAG = "MainActivity";


    private int mCameraPreviewThousandFps;

    private Button btnRecord;

    private SurfaceView mSurfaceView;
    private SurfaceCallback mSurfaceCallback;

    private Camera mCamera;

    private CircularEncoder mCircEncoder;
    private WindowSurface mEncoderSurface;

    private final RecordContext mRecordContext = new RecordContext();

    private final Handler mHandler = new Handler();

    private boolean isRecording = false;
    private AudioRecord mAudioRecord;
    private MuxerAudioVideo mMuxerAudioVideo;
    private VideoProgressBar mVideoProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceCallback = new SurfaceCallback();
        mSurfaceView.getHolder().addCallback(mSurfaceCallback);

        btnRecord = (Button) findViewById(R.id.record);
        btnRecord.setOnTouchListener(this);

        mVideoProgressBar = (VideoProgressBar) findViewById(R.id.video_progress_bar);
        mVideoProgressBar.setRecordContext(mRecordContext);

        mAudioRecord = new AudioRecord();
        mMuxerAudioVideo = new MuxerAudioVideo();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startRecord();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stopRecord();
                break;
        }
        return false;
    }

    private void startRecord() {
        if (isRecording)
            return;

        if (mRecordContext.getCurrentWholeTimeSpan() > mRecordContext.wholeTimeSpan)
            return;

        try {
            BlockInfo blockInfo = mRecordContext.createFileInfo();
            mAudioRecord.prepare(blockInfo.getAudioFile().getPath());
            mAudioRecord.start();

            mCircEncoder = new CircularEncoder(
                    mRecordContext.DESIRED_WIDTH,
                    mRecordContext.DESIRED_HEIGHT,
                    mRecordContext.DESIRED_BIT_RATE,
                    mCameraPreviewThousandFps / 1000,
                    mEncoderCallback,
                    blockInfo);
            mEncoderSurface = new WindowSurface(mSurfaceCallback.mEglCore, mCircEncoder.getInputSurface(), true);

            isRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to record", Toast.LENGTH_LONG).show();
        }
    }

    private void stopRecord() {
        if (!isRecording)
            return;

        try {
            mAudioRecord.stop();
            mCircEncoder.saveVideo();

            isRecording = false;

            mMuxerAudioVideo.setFileInfo(mRecordContext.getLastFileInfo());
            mMuxerAudioVideo.start();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to record", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        openCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        mSurfaceCallback.release();
    }

    private void openCamera() {
        if (mCamera != null) return;

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        //1. 打开后置摄像头,如果没有则打开默认摄像头
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCamera = Camera.open(i);
                break;
            }
        }

        if (mCamera == null) {
            mCamera.open();
        }

        if (mCamera == null) {
            //打开摄像头失败
            Toast.makeText(this, "Unable to open camera", Toast.LENGTH_LONG).show();
            return;
        }

        Camera.Parameters parameters = mCamera.getParameters();

        //2. 尝试设置特定的宽高
        //   此处筛选出最接近正方形边长的摄像头尺寸,但尺寸的长宽不能小于正方形边长
        //   note:此处有BUG,有时候我们获取的摄像头尺寸与其实际尺寸不符,
        //        比如说:我们获取到960*740的摄像头,但显示在SurfaceView的图像,长宽比却不是960/740
        //
        //   这里可以尝试以下不同机型不同摄像头尺寸所展示的效果
        //        在红米1s中有多个尺寸的摄像头变形,集中在尺寸较小的摄像头中.
        CameraUtils.choosePreviewSize(parameters, mRecordContext.DESIRED_WIDTH, mRecordContext.DESIRED_HEIGHT);

        //3. 尝试设置特定的帧率
        mCameraPreviewThousandFps = CameraUtils.chooseFixedPreviewFps(parameters, mRecordContext.DESIRED_PREVIEW_FPS);

        //4. 告知摄像头,应用将要录影,能改善帧率
        parameters.setRecordingHint(true);

        //5. 尝试设置摄像头旋转角度
        int orientation = CameraUtils.setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);

        mCamera.setParameters(parameters);

        //6. 设置渲染器宽高
        Camera.Size previewSize = parameters.getPreviewSize();
        Drawable2d.getInstance().setCoords(previewSize.width, previewSize.height, orientation);

        Log.i(TAG, String.format("width = %s , height = %s , orientation = %s , fps = %s",
                previewSize.width,
                previewSize.height,
                orientation,
                mCameraPreviewThousandFps));
    }

    private void releaseCamera() {
        if (mCamera != null) {
            try {
                mCamera.startPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private CircularEncoder.Callback mEncoderCallback = new CircularEncoder.Callback() {
        @Override
        public void fileSaveComplete(int status) {
            Toast.makeText(MainActivity.this, "录制成功", Toast.LENGTH_LONG).show();
        }

        @Override
        public void bufferStatus(long totalTimeMsec, int frameNum) {
            getWindow().getDecorView().getHandler().post(refreshProgressBar);
            if (mRecordContext.getCurrentWholeTimeSpan() > mRecordContext.wholeTimeSpan) {
                stopRecord();
            }

        }
    };

    private Runnable refreshProgressBar = new Runnable() {
        @Override
        public void run() {
            mVideoProgressBar.invalidate();
        }
    };

    private class SurfaceCallback implements SurfaceHolder.Callback {

        private EglCore mEglCore;
        private WindowSurface mDisplaySurface;
        private FullFrameRect mFullFrameBlit;
        private int mTextureId;
        private SurfaceTexture mCameraTexture;

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
                mDisplaySurface = new WindowSurface(mEglCore, holder.getSurface(), false);
                mDisplaySurface.makeCurrent();

                mFullFrameBlit = new FullFrameRect(new Texture2dProgram());
                mTextureId = mFullFrameBlit.createTextureObject();
                mCameraTexture = new SurfaceTexture(mTextureId);
                mCameraTexture.setOnFrameAvailableListener(new OnFrameAvailableListener());

                mCamera.setPreviewTexture(mCameraTexture);
                mCamera.startPreview();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCamera != null)
                            mCamera.autoFocus(null);
                    }
                }, 800);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Unable to open camera", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

        public void release() {
            if (mCameraTexture != null) {
                try {
                    mCameraTexture.release();
                    mCameraTexture = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mDisplaySurface != null) {
                try {
                    mDisplaySurface.release();
                    mDisplaySurface = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mFullFrameBlit != null) {
                try {
                    mFullFrameBlit.release(false);
                    mFullFrameBlit = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mEglCore != null) {
                try {
                    mEglCore.release();
                    mEglCore = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private class OnFrameAvailableListener implements SurfaceTexture.OnFrameAvailableListener, Runnable {

            private final float[] mTmpMatrix = new float[16];

            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mHandler.post(this);
            }

            @Override
            public void run() {
                drawFrame();
            }

            private void drawFrame() {
                if (mEglCore == null) {
                    Log.d(TAG, "Skipping drawFrame after shutdown");
                    return;
                }

                // Latch the next frame from the camera.
                mDisplaySurface.makeCurrent();
                mCameraTexture.updateTexImage();
                mCameraTexture.getTransformMatrix(mTmpMatrix);

                // Fill the SurfaceView with it.
                int viewWidth = mSurfaceView.getWidth();
                int viewHeight = mSurfaceView.getHeight();
                //Log.i(TAG, "viewWidth = " + viewWidth + "   viewHeight = " + viewHeight);
                GLES20.glViewport(0, 0, viewWidth, viewHeight);
                mFullFrameBlit.drawFrame(mTextureId, mTmpMatrix);
                mDisplaySurface.swapBuffers();

                if (isRecording) {
                    mEncoderSurface.makeCurrent();
                    GLES20.glViewport(0, 0, mRecordContext.DESIRED_WIDTH, mRecordContext.DESIRED_HEIGHT);
                    mFullFrameBlit.drawFrame(mTextureId, mTmpMatrix);
                    mCircEncoder.frameAvailableSoon();
                    mEncoderSurface.setPresentationTime(mCameraTexture.getTimestamp());
                    mEncoderSurface.swapBuffers();
                }
            }
        }
    }
}
