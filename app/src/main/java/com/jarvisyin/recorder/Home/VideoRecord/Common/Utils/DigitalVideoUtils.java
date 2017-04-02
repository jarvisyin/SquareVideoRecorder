package com.jarvisyin.recorder.Home.VideoRecord.Common.Utils;

import android.content.Context;
import android.text.TextUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

/**
 * Created by jarvisyin on 16/4/4.
 */
public class DigitalVideoUtils {


    /**
     * 获取视频名字
     * <p>
     * 时间 + UUID
     *
     * @return
     */
    public static String getRandomVideoName() {
        return getRandom() + ".mp4";
    }

    /**
     * 获取随机名字
     * <p>
     * 时间 + UUID
     *
     * @return
     */
    public static String getRandom() {
        return new Date().getTime() + "_" + UUID.randomUUID().toString().replace("-", "");
    }


    /**
     * 根据DV的名字获取iamge(预览图)的名字
     *
     * @param dvName
     * @return
     */
    public static String getImageNameFromDVName(String dvName) {
        if (TextUtils.isEmpty(dvName)) {
            return null;
        }
        int indexOf = dvName.lastIndexOf(".mp4");
        if (indexOf == -1) {
            return dvName + ".png";
        }
        if (indexOf != dvName.length() - 4) {
            return dvName + ".png";
        }
        return dvName.substring(0, indexOf) + ".png";
    }

    /**
     * 文件复制
     *
     * @param context
     * @param rawID
     * @param path
     * @throws IOException
     */
    public static void fileCopy(Context context, int rawID, String path) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(rawID);
        OutputStream outputStream = new FileOutputStream(path);

        /**
         * 512kb 缓存
         */
        byte[] bytes = new byte[1024 * 512];
        int x;
        while ((x = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, x);
        }
        inputStream.close();
        outputStream.close();
    }

}
