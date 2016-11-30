package com.jarvisyin.squarevideorecorder;

import android.os.Bundle;

import com.jarvisyin.squarevideorecorder.Common.Component.Activity.BaseActivity;
import com.jarvisyin.squarevideorecorder.Common.Utils.FileUtils;
import com.jarvisyin.squarevideorecorder.Record.RecordFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends BaseActivity {

    private final static String TAG = "MainActivity";


    private final String mFileStore = "/sdcard/SquareVideoRecord";
    private final List<BlockInfo> mBlockInfos = new ArrayList<>();


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
        FileUtils.deleteFile(mFileStore);
        addFragmentWhenActStart(new RecordFragment());
    }

    public BlockInfo createBlockInfo() {
        File file = new File(mFileStore, getRandom());
        if (!file.exists()) {
            file.mkdirs();
        }
        BlockInfo blockInfo = new BlockInfo(file);
        mBlockInfos.add(blockInfo);
        return blockInfo;
    }

    public void deleteBlockInfo() {
        if (mBlockInfos != null && !mBlockInfos.isEmpty())
            mBlockInfos.remove(mBlockInfos.size() - 1);
    }

    public BlockInfo getLastFileInfo() {
        return mBlockInfos.get(mBlockInfos.size() - 1);
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
            count += blockInfo.getTimeSpan();
        }
        return count;
    }

}
