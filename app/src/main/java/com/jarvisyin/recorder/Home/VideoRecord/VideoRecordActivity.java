package com.jarvisyin.recorder.Home.VideoRecord;

import android.os.Bundle;
import android.view.ViewGroup;


import com.jarvisyin.recorder.Buz.FileManager;
import com.jarvisyin.recorder.Common.Component.Activity.BaseActivity;
import com.jarvisyin.recorder.Common.Utils.Trinea.FileUtils;
import com.jarvisyin.recorder.Common.Utils.JYToast;
import com.jarvisyin.recorder.Home.VideoRecord.Common.Utils.FFmpeg.VShopFFmpeg;
import com.jarvisyin.recorder.Home.VideoRecord.Edit.Music.MusicInfo;
import com.jarvisyin.recorder.Home.VideoRecord.Edit.Music.MusicSource;
import com.jarvisyin.recorder.Home.VideoRecord.Record.RecordFragment;
import com.jarvisyin.recorder.R;
import com.yixia.camera.VCamera;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class VideoRecordActivity extends BaseActivity {

    private final static String TAG = "MainActivity";

    public final List<BlockInfo> mBlockInfos = new ArrayList<>();

    private String mCachePath;
    private String mBackgroundAudioPath;
    private String mSourceVideoPath;
    private String mSourceAudioPath;
    private String mProcessingAudioPath;
    private String mProcessingVideoPath;
    private String mCoverPath;
    private String mFinalPath;

    private MusicInfo backgroundAudioInfo = MusicSource.getEmptyMusicInfo();

    /**
     * Should be a multiple of 16.
     */
    public final int DESIRED_WIDTH = 640;
    /**
     * Usually a multiple of 16 (1080 is ok).
     */
    public final int DESIRED_HEIGHT = 640;
    /**
     * frame rate.
     */
    public final int DESIRED_PREVIEW_FPS = 30 * 1000;
    /**
     * Target bit rate, in bits.
     */
    public final int DESIRED_BIT_RATE = 1024000;


    public final long lessTimeSpan = 6 * 1000000L;
    public final long wholeTimeSpan = 10 * 1000000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundResource(R.color.video_window_background);
        setContentView(R.layout.activity_video);

        initFile();
        initFFmpeg();
    }

    private void initFile() {
        //TODO 检查外存容量是否足够

        mCachePath = FileManager.getCachePath().getPath();

        File cacheFile = new File(mCachePath);
        if (cacheFile.exists()) {
            if (!cacheFile.canWrite()) {
                //TODO 文件不可写
                JYToast.show("文件不可写");
                return;
            }

            if (!cacheFile.canRead()) {
                //TODO 文件不可读
                JYToast.show("文件不可写");
                return;
            }
        }

        FileUtils.deleteFile(mCachePath);
        mCachePath = new File(mCachePath, UUID.randomUUID().toString().replace("-", "")).getPath();
        mBackgroundAudioPath = new File(mCachePath, "backgroundAudio.m4a").getPath();
        mSourceVideoPath = new File(mCachePath, "sourceVideo.mp4").getPath();
        mSourceAudioPath = new File(mCachePath, "sourceAudio.m4a").getPath();
        mProcessingVideoPath = new File(mCachePath, "processingVideo.mp4").getPath();
        mProcessingAudioPath = new File(mCachePath, "processingAudio.m4a").getPath();
        mCoverPath = new File(mCachePath, "cover.png").getPath();
        mFinalPath = new File(mCachePath, "final.mp4").getPath();
    }

    private void initFFmpeg() {
        VCamera.initialize(getApplicationContext());
        VShopFFmpeg.getInstance().initialize(getApplicationContext(), new VShopFFmpeg.Callback() {

            @Override
            public void onProgress(String message) {

            }

            @Override
            public void onFailure(String msg) {
                ((ViewGroup) findViewById(R.id.content)).removeAllViews();
                //TODO show dialog
            }

            @Override
            public void onSuccess(String msg) {
                ((ViewGroup) findViewById(R.id.content)).removeAllViews();
                addFragmentWhenActStart(new RecordFragment());
            }
        });

    }

    public BlockInfo createBlockInfo() {
        File file = new File(mCachePath, getRandom());
        if (!file.exists()) {
            file.mkdirs();
        }
        BlockInfo blockInfo = new BlockInfo(file);
        mBlockInfos.add(blockInfo);
        return blockInfo;
    }

    public BlockInfo getLastBlockInfo() {
        return mBlockInfos.get(mBlockInfos.size() - 1);
    }

    public void deleteBlockInfo() {
        if (mBlockInfos != null && !mBlockInfos.isEmpty())
            mBlockInfos.remove(mBlockInfos.size() - 1);
    }

    public MusicInfo getBackgroundAudioInfo() {
        return backgroundAudioInfo;
    }

    public void setBackgroundAudioInfo(MusicInfo backgroundAudioInfo) {
        this.backgroundAudioInfo = backgroundAudioInfo;
    }

    public List<BlockInfo> getBlockInfos() {
        return mBlockInfos;
    }

    private static String getRandom() {
        return new Date().getTime() + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    public long getCurrentWholeTimeSpan() {
        List<BlockInfo> blockInfos = getBlockInfos();
        long count = 0;
        for (BlockInfo blockInfo : blockInfos) {
            count += blockInfo.getDuration();
        }
        return count;
    }

    public String getCachePath() {
        return mCachePath;
    }

    public String getBackgroundAudioPath() {
        return mBackgroundAudioPath;
    }

    public String getSourceVideoPath() {
        return mSourceVideoPath;
    }

    public String getSourceAudioPath() {
        return mSourceAudioPath;
    }

    public String getProcessingAudioPath() {
        return mProcessingAudioPath;
    }

    public String getProcessingVideoPath() {
        return mProcessingVideoPath;
    }

    public String getCoverPath() {
        return mCoverPath;
    }

    public String getFinalPath() {
        return mFinalPath;
    }
}
