package com.jarvisyin.recorder.Home.VideoRecord.Edit;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.view.Surface;

import com.jarvisyin.recorder.Home.VideoRecord.BlockInfo;
import com.jarvisyin.recorder.Home.VideoRecord.VideoRecordActivity;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by jarvisyin on 16/12/1.
 */
public class VideoSurfaceView extends GLSurfaceView {
    public static final String TAG = "VShopVideo VideoSurfaceView";

    private final MediaPlayer mMediaPlayer;
    private final VideoRender mRenderer;

    public VideoSurfaceView(VideoRecordActivity context, MediaPlayer mediaPlayer) {
        super(context);

        List<BlockInfo> blockInfos = context.getBlockInfos();
        long[] timeMarks = new long[blockInfos.size()];
        long sum = 0;
        for (int i = 0; i < blockInfos.size(); i++) {
            BlockInfo block = blockInfos.get(i);
            sum += (block.getDuration() / 1000L);
            timeMarks[i] = sum;
        }

        mMediaPlayer = mediaPlayer;
        setEGLContextClientVersion(2);
        mRenderer = new VideoRender(context, timeMarks);
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

        private final long[] mTimeMarks;

        public VideoRender(VideoRecordActivity context, long[] timeMarks) {
            mTextureRender = new TextureRender(context);
            mTimeMarks = timeMarks;
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

            int currentTime = mMediaPlayer.getCurrentPosition();
            int currentPart = -1;
            for (int i = mTimeMarks.length - 1; i >= 0; i--) {
                long timeMark = mTimeMarks[i];
                if (timeMark >= currentTime) {
                    currentPart = i;
                } else {
                    break;
                }
            }
            mTextureRender.draw(mSurfaceTexture, currentPart);
        }

        public void setMediaPlayer(MediaPlayer mediaPlayer) {
            this.mMediaPlayer = mediaPlayer;
        }
    }
}
