package com.jarvisyin.squarevideorecorder;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "MainActivity";

    private final static int WIDTH = 640;
    private final static int HEIGHT = 640;
    private final static int DESIRED_PREVIEW_FPS = 30 * 1000;

    private int mCameraPreviewThousandFps;

    private Camera mCamera;

    private SurfaceCallback mSurfaceCallback;

    private SurfaceView surfaceView;
    private Button btnRecord;

    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceCallback = new SurfaceCallback();
        surfaceView.getHolder().addCallback(mSurfaceCallback);

        btnRecord = (Button) findViewById(R.id.record);
        btnRecord.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

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
        //   CameraUtils.choosePreviewSize(parameters, WIDTH, HEIGHT);

        //3. 尝试设置特定的帧率
        mCameraPreviewThousandFps = CameraUtils.chooseFixedPreviewFps(parameters, DESIRED_PREVIEW_FPS);

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
                //Log.d(TAG, "drawFrame");
                if (mEglCore == null) {
                    Log.d(TAG, "Skipping drawFrame after shutdown");
                    return;
                }

                // Latch the next frame from the camera.
                mDisplaySurface.makeCurrent();
                mCameraTexture.updateTexImage();
                mCameraTexture.getTransformMatrix(mTmpMatrix);

                // Fill the SurfaceView with it.
                int viewWidth = surfaceView.getWidth();
                int viewHeight = surfaceView.getHeight();
                //Log.i(TAG, "viewWidth = " + viewWidth + "   viewHeight = " + viewHeight);
                GLES20.glViewport(0, 0, viewWidth, viewHeight);
                mFullFrameBlit.drawFrame(mTextureId, mTmpMatrix);
                mDisplaySurface.swapBuffers();

                //TODO Send it to the video encoder.
                /*if (!mFileSaveInProgress && isRecording) {
                    mEncoderSurface.makeCurrent();
                    GLES20.glViewport(0, 0, VIDEO_HEIGHT, VIDEO_WIDTH);
                    mFullFrameBlit.drawFrame(mTextureId, mTmpMatrix);
                    mCircEncoder.frameAvailableSoon();
                    mEncoderSurface.setPresentationTime(mCameraTexture.getTimestamp());
                    mEncoderSurface.swapBuffers();
                }*/
                //mFrameNum++;
            }
        }
    }
}
