package com.jarvisyin.squarevideorecorder.Edit;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by jarvisyin on 16/12/1.
 */
public class VideoSurfaceView extends GLSurfaceView {
    private final MediaPlayer mMediaPlayer;
    private final VideoRender mRenderer;

    public VideoSurfaceView(Context context, MediaPlayer mediaPlayer) {
        super(context);

        mMediaPlayer = mediaPlayer;
        setEGLContextClientVersion(2);
        mRenderer = new VideoRender();
        setRenderer(mRenderer);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    public void onResume() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.setMediaPlayer(mMediaPlayer);
            }
        });
        super.onResume();

    }

    private static class VideoRender implements Renderer, SurfaceTexture.OnFrameAvailableListener {
        private TextureRender mTextureRender;
        private SurfaceTexture mSurfaceTexture;
        private boolean updateSurface = false;
        private MediaPlayer mMediaPlayer;

        public VideoRender() {
            mTextureRender = new TextureRender();
        }


        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            updateSurface = true;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mTextureRender.surfaceCreated();

            mSurfaceTexture = new SurfaceTexture(mTextureRender.getTextureId());
            mSurfaceTexture.setOnFrameAvailableListener(this);

            Surface surface = new Surface(mSurfaceTexture);
            mMediaPlayer.setSurface(surface);
            surface.release();

            try {
                mMediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mMediaPlayer.start();

            synchronized (this) {
                updateSurface = false;
            }
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

        }

        @Override
        public void onDrawFrame(GL10 gl) {
            synchronized (this) {
                if (updateSurface) {
                    mSurfaceTexture.updateTexImage();
                    updateSurface = false;
                }
            }
            mTextureRender.draw(mSurfaceTexture);
        }

        public void setMediaPlayer(MediaPlayer mediaPlayer) {
            this.mMediaPlayer = mediaPlayer;
        }
    }
}
