package com.jarvisyin.squarevideorecorder;

import com.jarvisyin.squarevideorecorder.Utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by jarvisyin on 16/11/18.
 */
public class RecordContext {

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

    public RecordContext() {
        FileUtils.deleteFile(mFileStore);
    }

    public BlockInfo createFileInfo() {
        File file = new File(mFileStore, getRandom());
        if (!file.exists()) {
            file.mkdirs();
        }
        BlockInfo fileInfo = new BlockInfo(file);
        mBlockInfos.add(fileInfo);
        return fileInfo;
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
