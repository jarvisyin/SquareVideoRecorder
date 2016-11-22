package com.jarvisyin.squarevideorecorder;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.text.TextUtils;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by jarvisyin on 16/11/18.
 */
public class MuxerAudioVideo {
    private static final String TAG = "MuxerAudioVedio";
    private BlockInfo mFileInfo;

    public void setFileInfo(BlockInfo fileInfo) {
        mFileInfo = fileInfo;
    }

    public void start() throws Exception {
        MediaMuxer muxer = null;
        MediaExtractor audioExtractor;
        MediaExtractor videoExtractor;
        try {
            audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(mFileInfo.getAudioFile().getPath());

            videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(mFileInfo.getVideoFile().getPath());

            muxer = new MediaMuxer(mFileInfo.getResultFile().getPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            int count, videoTrackIndex = -1, audioTrackIndex = -1;

            count = audioExtractor.getTrackCount();
            for (int i = 0; i < count; i++) {
                MediaFormat format = audioExtractor.getTrackFormat(i);
                String mine = format.getString(MediaFormat.KEY_MIME);
                Log.i(TAG, "audio mine = " + mine);
                if (!TextUtils.isEmpty(mine) && mine.startsWith("audio/")) {
                    audioTrackIndex = muxer.addTrack(format);
                    audioExtractor.selectTrack(i);
                    break;
                }
            }

            count = videoExtractor.getTrackCount();
            for (int i = 0; i < count; i++) {
                MediaFormat format = videoExtractor.getTrackFormat(i);
                String mine = format.getString(MediaFormat.KEY_MIME);
                Log.i(TAG, "video mine = " + mine);
                if (!TextUtils.isEmpty(mine) && mine.startsWith("video/")) {
                    videoTrackIndex = muxer.addTrack(format);
                    videoExtractor.selectTrack(i);
                    break;
                }
            }

            int offset = 100;

            int MAX_SAMPLE_SIZE = 256 * 1024;
            ByteBuffer buffer = ByteBuffer.allocate(MAX_SAMPLE_SIZE);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            muxer.start();
            while (true) {
                bufferInfo.size = audioExtractor.readSampleData(buffer, offset);
                if (bufferInfo.size < 0)
                    break;

                bufferInfo.offset = offset;
                bufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                bufferInfo.flags = audioExtractor.getSampleFlags();
                muxer.writeSampleData(audioTrackIndex, buffer, bufferInfo);
                audioExtractor.advance();
            }

            while (true) {
                bufferInfo.size = videoExtractor.readSampleData(buffer, offset);
                if (bufferInfo.size < 0)
                    break;

                bufferInfo.offset = offset;
                bufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                bufferInfo.flags = videoExtractor.getSampleFlags();
                muxer.writeSampleData(videoTrackIndex, buffer, bufferInfo);
                videoExtractor.advance();
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (muxer != null) {
                try {
                    muxer.stop();
                    muxer.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
