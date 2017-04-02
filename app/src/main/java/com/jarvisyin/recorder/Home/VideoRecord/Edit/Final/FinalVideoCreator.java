/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jarvisyin.recorder.Home.VideoRecord.Edit.Final;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.text.TextUtils;
import android.view.Surface;

import com.jarvisyin.recorder.BuildConfig;
import com.jarvisyin.recorder.Buz.FileManager;
import com.jarvisyin.recorder.Common.Utils.CommonUtils;
import com.jarvisyin.recorder.Common.Utils.Trinea.FileUtils;
import com.jarvisyin.recorder.Common.Utils.JYLog;
import com.jarvisyin.recorder.Home.VideoRecord.BlockInfo;
import com.jarvisyin.recorder.Home.VideoRecord.Common.Utils.DigitalVideoUtils;
import com.jarvisyin.recorder.Home.VideoRecord.Common.Utils.VideoUtils;
import com.jarvisyin.recorder.Home.VideoRecord.VideoRecordActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class FinalVideoCreator extends Thread {

    private static final String TAG = "VShopVideo FinalVideoCreator";
    private static final boolean VERBOSE = true && BuildConfig.DEBUG; // lots of logging

    private static final int TIMEOUT_USEC = 10000;

    private static final String OUTPUT_VIDEO_MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
    private static final int OUTPUT_VIDEO_BIT_RATE = 1024000; // 2Mbps
    private static final int OUTPUT_VIDEO_FRAME_RATE = 30; // 15fps
    private static final int OUTPUT_VIDEO_IFRAME_INTERVAL = 10; // 10 seconds between I-frames
    private static final int OUTPUT_VIDEO_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;

    private VideoRecordActivity mContext;
    private long wholeTime;
    private CallBack callBack;

    public FinalVideoCreator(VideoRecordActivity context) {
        this.mContext = context;
    }

    public void run() {
        try {
            //if (callBack != null) callBack.onProgress(0f);
            createVideo();
            muxer();
            createCoverImage();
            toVideoStore();
            deleteCache();
            if (callBack != null) callBack.onSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            if (callBack != null) callBack.onFailure(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    int i = 0;

    /**
     * 生成视频
     */
    private void createVideo() throws Exception {
        MediaCodecInfo videoCodecInfo = VideoUtils.selectCodec(OUTPUT_VIDEO_MIME_TYPE);
        if (videoCodecInfo == null) {
            throw new Exception(String.format("device unable to support this video mine type(%s)", OUTPUT_VIDEO_MIME_TYPE));
        }

        MediaCodec videoEncoder = null;
        MediaMuxer muxer = null;
        InputSurface inputSurface = null;

        try {
            MediaFormat outputVideoFormat = MediaFormat.createVideoFormat(OUTPUT_VIDEO_MIME_TYPE, mContext.DESIRED_WIDTH, mContext.DESIRED_HEIGHT);
            outputVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, OUTPUT_VIDEO_COLOR_FORMAT);
            outputVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, OUTPUT_VIDEO_BIT_RATE);
            outputVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, OUTPUT_VIDEO_FRAME_RATE);
            outputVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, OUTPUT_VIDEO_IFRAME_INTERVAL);

            AtomicReference<Surface> inputSurfaceReference = new AtomicReference<>();
            videoEncoder = MediaCodec.createByCodecName(videoCodecInfo.getName());
            videoEncoder.configure(outputVideoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            inputSurfaceReference.set(videoEncoder.createInputSurface());
            videoEncoder.start();

            inputSurface = new InputSurface(inputSurfaceReference.get());
            inputSurface.makeCurrent();

            muxer = new MediaMuxer(mContext.getProcessingVideoPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            ByteBuffer[] videoEncoderOutputBuffers = videoEncoder.getOutputBuffers();
            MediaCodec.BufferInfo videoEncoderOutputBufferInfo = new MediaCodec.BufferInfo();

            int outputVideoTrack = -1;
            boolean encoderDone = false;

            wholeTime = 0L;

            int blockPosition = 0;
            List<BlockInfo> blockInfos = mContext.getBlockInfos();
            VideoDecoder[] list = new VideoDecoder[blockInfos.size()];

            while (!encoderDone) {
                //解码
                JYLog.i(TAG, "Now is decoding = %4s ,encoderDone = %6s ,blockPosition = %2s ,blockInfos.length = %s",
                        i++, encoderDone, blockPosition, blockInfos.size());
                if (blockPosition < blockInfos.size()) {
                    VideoDecoder vead = list[blockPosition];
                    if (vead == null) {
                        vead = new VideoDecoder(blockInfos.get(blockPosition), inputSurface);
                        list[blockPosition] = vead;
                    }

                    boolean isFinish = vead.decode();

                    JYLog.i(TAG, "isFinish = %6s , blockPosition = %6s", isFinish, blockPosition);

                    if (isFinish) {
                        blockPosition++;
                    }

                    if (blockPosition >= blockInfos.size())
                        videoEncoder.signalEndOfInputStream();
                }

                //释放上一个Decoder
                int j = blockPosition - 1;
                if (j > -1 && j < blockInfos.size() - 1 && list[j] != null) {
                    list[j].release();
                    list[j] = null;
                }


                while (!encoderDone) {
                    int encoderOutputBufferIndex = videoEncoder.dequeueOutputBuffer(videoEncoderOutputBufferInfo, TIMEOUT_USEC);
                    if (encoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break;
                    }

                    if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        videoEncoderOutputBuffers = videoEncoder.getOutputBuffers();
                        break;
                    }

                    if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        MediaFormat encoderOutputVideoFormat = videoEncoder.getOutputFormat();
                        outputVideoTrack = muxer.addTrack(encoderOutputVideoFormat);
                        muxer.start();
                        break;
                    }

                    ByteBuffer encoderOutputBuffer = videoEncoderOutputBuffers[encoderOutputBufferIndex];
                    if ((videoEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        if (VERBOSE) JYLog.d(TAG, "video encoder: codec config buffer");
                        videoEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
                        break;
                    }
                    if (videoEncoderOutputBufferInfo.size != 0) {
                        muxer.writeSampleData(outputVideoTrack, encoderOutputBuffer, videoEncoderOutputBufferInfo);
                    }
                    if ((videoEncoderOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (VERBOSE) JYLog.d(TAG, "video encoder: EOS");
                        encoderDone = true;
                    }
                    videoEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
                    break;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (videoEncoder != null) {
                    videoEncoder.stop();
                    videoEncoder.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (muxer != null) {
                    muxer.stop();
                    muxer.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (inputSurface != null) {
                    inputSurface.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 合并音视频
     */
    private void muxer() throws Exception {
        MediaMuxer muxer = null;
        MediaExtractor audioExtractor;
        MediaExtractor videoExtractor;
        try {
            audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(mContext.getProcessingAudioPath());

            videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(mContext.getProcessingVideoPath());

            muxer = new MediaMuxer(mContext.getFinalPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            int count, videoTrackIndex = -1, audioTrackIndex = -1;

            count = audioExtractor.getTrackCount();
            for (int i = 0; i < count; i++) {
                MediaFormat format = audioExtractor.getTrackFormat(i);
                String mine = format.getString(MediaFormat.KEY_MIME);
                JYLog.i(TAG, "audio mine = " + mine);
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
                JYLog.i(TAG, "video mine = " + mine);
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

    /**
     * 生成封面图片
     */
    private void createCoverImage() throws Exception {
        File videoFile = new File(mContext.getFinalPath());
        Bitmap image = CommonUtils.getVideoThumbnail(videoFile.getPath());
        File imageFile = new File(mContext.getCoverPath());

        if (imageFile.exists()) {
            imageFile.delete();
        }
        JYLog.i(TAG, "cover path = %s", imageFile.getPath());
        FileOutputStream out = new FileOutputStream(imageFile);
        image.compress(Bitmap.CompressFormat.PNG, 90, out);
        out.flush();
        out.close();
    }

    /**
     * 将视频移到视频库
     */
    private void toVideoStore() {

        File videoFile = new File(FileManager.getVideoStorePath(), DigitalVideoUtils.getRandomVideoName());
        File coverFile = new File(FileManager.getVideoStorePath(), DigitalVideoUtils.getImageNameFromDVName(videoFile.getName()));

        new File(mContext.getFinalPath()).renameTo(videoFile);
        new File(mContext.getCoverPath()).renameTo(coverFile);
    }

    /**
     * 删除缓存
     */
    private void deleteCache() {
        FileUtils.deleteFile(mContext.getCachePath());
    }

    private class VideoDecoder {

        private MediaExtractor mMediaExtractor = null;
        private OutputSurface mOutputSurface = null;
        private MediaCodec mMediaDecoder = null;

        private ByteBuffer[] mInputBuffers = null;
        private ByteBuffer[] mOutputBuffers = null;
        private MediaCodec.BufferInfo mOutputBufferInfo = null;

        private InputSurface mInputSurface;
        private long currentTime = 0;

        private boolean extractorDone = false;
        private boolean decoderDone = false;

        private VideoDecoder(BlockInfo block, InputSurface inputSurface) throws Exception {
            this.mInputSurface = inputSurface;

            mMediaExtractor = new MediaExtractor();
            mMediaExtractor.setDataSource(block.getVideoFile().getPath());
            int videoInputTrack = VideoUtils.getAndSelectVideoTrackIndex(mMediaExtractor);
            MediaFormat inputFormat = mMediaExtractor.getTrackFormat(videoInputTrack);

            mOutputSurface = new OutputSurface();
            mOutputSurface.setExplain(block.getExplain());
            mMediaDecoder = MediaCodec.createDecoderByType(VideoUtils.getMimeTypeFor(inputFormat));
            mMediaDecoder.configure(inputFormat, mOutputSurface.getSurface(), null, 0);
            mMediaDecoder.start();

            mInputBuffers = mMediaDecoder.getInputBuffers();
            mOutputBuffers = mMediaDecoder.getOutputBuffers();
            mOutputBufferInfo = new MediaCodec.BufferInfo();
        }

        private boolean decode() {
            while (!extractorDone) {
                int decoderInputBufferIndex = mMediaDecoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (decoderInputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break;
                }
                ByteBuffer decoderInputBuffer = mInputBuffers[decoderInputBufferIndex];
                int size = mMediaExtractor.readSampleData(decoderInputBuffer, 0);
                long presentationTime = mMediaExtractor.getSampleTime();
                if (size >= 0) {
                    mMediaDecoder.queueInputBuffer(
                            decoderInputBufferIndex,
                            0,
                            size,
                            presentationTime,
                            mMediaExtractor.getSampleFlags());
                }
                extractorDone = !mMediaExtractor.advance();
                if (extractorDone) {
                    if (VERBOSE) JYLog.d(TAG, "video extractor: EOS");
                    mMediaDecoder.queueInputBuffer(
                            decoderInputBufferIndex,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                }
                break;
            }

            // Poll output frames from the video decoder and feed the encoder.
            while (!decoderDone) {
                int decoderOutputBufferIndex = mMediaDecoder.dequeueOutputBuffer(mOutputBufferInfo, TIMEOUT_USEC);
                if (decoderOutputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break;
                }
                if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    mOutputBuffers = mMediaDecoder.getOutputBuffers();
                    break;
                }
                if (decoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    //decoderOutputVideoFormat = videoDecoder1.getOutputFormat();
                    break;
                }
                ByteBuffer decoderOutputBuffer = mOutputBuffers[decoderOutputBufferIndex];
                if ((mOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    mMediaDecoder.releaseOutputBuffer(decoderOutputBufferIndex, false);
                    break;
                }

                boolean render = mOutputBufferInfo.size != 0;
                mMediaDecoder.releaseOutputBuffer(decoderOutputBufferIndex, render);
                if (render) {
                    mOutputSurface.awaitNewImage();
                    mOutputSurface.drawImage();
                    currentTime = wholeTime + mOutputBufferInfo.presentationTimeUs * 1000;
                    mInputSurface.setPresentationTime(currentTime);
                    mInputSurface.swapBuffers();
                    if (VERBOSE) JYLog.d(TAG, "video encoder: notified of new frame");
                }
                if ((mOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    decoderDone = true;
                    wholeTime = currentTime;
                    if (VERBOSE) JYLog.d(TAG, "video decoder: EOS");
                }
                break;
            }
            return decoderDone;
        }

        private void release() {
            try {
                if (mMediaExtractor != null) {
                    mMediaExtractor.release();
                }
            } catch (Exception e) {
                throw e;
            }
            try {
                if (mMediaDecoder != null) {
                    mMediaDecoder.stop();
                    mMediaDecoder.release();
                }
            } catch (Exception e) {
                throw e;
            }
            try {
                if (mOutputSurface != null) {
                    mOutputSurface.release();
                }
            } catch (Exception e) {
                throw e;
            }
        }
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface CallBack {
        void onSuccess();

        void onFailure(String msg);

        void onProgress(float p);
    }
}
