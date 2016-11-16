package com.jarvisyin.squarevideorecorder;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Camera mCamera;

    private SurfaceView sv;
    private Button btnRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sv = (SurfaceView) findViewById(R.id.surface_view);
        sv.getHolder().addCallback(new SurfaceCallback());

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
    }

    private void openCamera() {
        if (mCamera != null) return;

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

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
            Toast.makeText(this,"Unable to open camera",Toast.LENGTH_LONG);
            return;
        }

        Camera.Parameters parameters = mCamera.getParameters();

        CameraUtils.
    }

    private void releaseCamera() {


    }


    private class SurfaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }
}
