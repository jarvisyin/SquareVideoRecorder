package com.jarvisyin.squarevideorecorder.Record;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jarvisyin.squarevideorecorder.BlockInfo;
import com.jarvisyin.squarevideorecorder.Common.Component.Fragment.BaseFragment;
import com.jarvisyin.squarevideorecorder.Common.Utils.JToast;
import com.jarvisyin.squarevideorecorder.Common.Widget.VideoActionButton;
import com.jarvisyin.squarevideorecorder.Common.Widget.VideoProgressBar;
import com.jarvisyin.squarevideorecorder.MainActivity;
import com.jarvisyin.squarevideorecorder.Play.PlayFragment;
import com.jarvisyin.squarevideorecorder.R;
import com.jarvisyin.squarevideorecorder.Record.Gles.CameraUtils;
import com.jarvisyin.squarevideorecorder.Record.Gles.Drawable2d;
import com.jarvisyin.squarevideorecorder.Record.Gles.EglCore;
import com.jarvisyin.squarevideorecorder.Record.Gles.FullFrameRect;
import com.jarvisyin.squarevideorecorder.Record.Gles.Texture2dProgram;
import com.jarvisyin.squarevideorecorder.Record.Gles.WindowSurface;


/**
 * Created by Jarvis.
 */
public class RecordFragment extends BaseFragment implements View.OnClickListener, VideoActionButton.ActionListener {
    public static final String TAG = RecordFragment.class.getName();

    private int mCameraPreviewThousandFps;

    private VideoActionButton btnRecord;

    private SurfaceView mSurfaceView;
    private SurfaceCallback mSurfaceCallback;

    private Camera mCamera;

    private CircularEncoder mCircEncoder;
    private WindowSurface mEncoderSurface;

    private MainActivity mContext;

    private final Handler mHandler = new Handler();

    private boolean isRecording = false;
    private AudioRecord mAudioRecord;
    private VideoProgressBar mVideoProgressBar;
    private Button btnNext, btnDelete;

    private EncoderCallback mEncoderCallback = new EncoderCallback();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSurfaceView = (SurfaceView) view.findViewById(R.id.surface_view);
        mSurfaceCallback = new SurfaceCallback();
        mSurfaceView.getHolder().addCallback(mSurfaceCallback);

        btnRecord = (VideoActionButton) view.findViewById(R.id.record);
        btnRecord.setActionListener(this);

        btnNext = (Button) view.findViewById(R.id.next);
        btnNext.setOnClickListener(this);
        btnDelete = (Button) view.findViewById(R.id.delete);
        btnDelete.setOnClickListener(this);

        mVideoProgressBar = (VideoProgressBar) view.findViewById(R.id.video_progress_bar);

        mAudioRecord = new AudioRecord();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                getBaseActivity().addFragmentWithAnim(new PlayFragment());
                break;
            case R.id.delete:
                mVideoProgressBar.invalidate();
                mContext.deleteBlockInfo();
                btnRecord.setEnabled(true);
                break;
        }
    }

    @Override
    public void startRecord() {
        if (isRecording)
            return;

        if (mContext.getCurrentWholeTimeSpan() > mContext.wholeTimeSpan)
            return;

        try {
            BlockInfo blockInfo = mContext.createBlockInfo();
            mAudioRecord.prepare(blockInfo.getAudioFile().getPath());
            mAudioRecord.start();

            mCircEncoder = new CircularEncoder(
                    mContext.DESIRED_WIDTH,
                    mContext.DESIRED_HEIGHT,
                    mContext.DESIRED_BIT_RATE,
                    mCameraPreviewThousandFps / 1000,
                    mEncoderCallback,
                    blockInfo);
            mEncoderSurface = new WindowSurface(mSurfaceCallback.mEglCore, mCircEncoder.getInputSurface(), true);

            isRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
            JToast.show("Unable to record");

            btnRecord.startFail();
            try {
                mCircEncoder.shutdown();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void stopRecord() {

        if (!isRecording)
            return;

        try {
            mCircEncoder.saveVideo();
            mAudioRecord.stop();

            isRecording = false;

            mCircEncoder.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            JToast.show("Unable to record");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        openCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
        mSurfaceCallback.release();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
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
            JToast.show("Unable to open camera");
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
        CameraUtils.choosePreviewSize(parameters, mContext.DESIRED_WIDTH, mContext.DESIRED_HEIGHT);

        //3. 尝试设置特定的帧率
        mCameraPreviewThousandFps = CameraUtils.chooseFixedPreviewFps(parameters, mContext.DESIRED_PREVIEW_FPS);

        //4. 告知摄像头,应用将要录影,能改善帧率
        parameters.setRecordingHint(true);

        //5. 尝试设置摄像头旋转角度
        int orientation = CameraUtils.setCameraDisplayOrientation(getBaseActivity(), Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);

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


    private class EncoderCallback implements CircularEncoder.Callback, Runnable {
        @Override
        public void fileSaveComplete(int status) {
            //JToast.show("录制成功");
        }

        @Override
        public void bufferStatus(long totalTimeMesec, int frameNum) {
            mHandler.post(refreshProgressBar);
            if (mContext.getCurrentWholeTimeSpan() > mContext.wholeTimeSpan) {
                mHandler.post(this);
                stopRecord();
            }
        }

        @Override
        public void run() {
            btnRecord.setEnabled(false);
        }
    }

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
                JToast.show("Unable to open camera");
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
                    GLES20.glViewport(0, 0, mContext.DESIRED_WIDTH, mContext.DESIRED_HEIGHT);
                    mFullFrameBlit.drawFrame(mTextureId, mTmpMatrix);
                    mCircEncoder.frameAvailableSoon();
                    mEncoderSurface.setPresentationTime(mCameraTexture.getTimestamp());
                    mEncoderSurface.swapBuffers();
                }
            }
        }
    }
}
