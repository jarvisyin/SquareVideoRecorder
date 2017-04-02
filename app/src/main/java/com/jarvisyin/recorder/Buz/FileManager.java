package com.jarvisyin.recorder.Buz;

import android.os.Environment;

import com.jarvisyin.recorder.AppContext;
import com.jarvisyin.recorder.R;

import java.io.File;

/**
 * 管理应用目录
 * system/Cache/Galleryfinal
 * <p/>
 * <p/>
 * sdcard/OyesSeller/
 * .      |
 * .      |------> Image
 * <p/>
 * <p/>
 * sdcard/OyesSeller/.nomedia
 * .      |
 * .      |------> VideoStore
 * .      |
 * .      |------> Music
 * .      |
 * .      |------> Cache
 * <p/>
 * <p/>
 * Created by jarvisyin on 16/4/4.
 */
public class FileManager {


    /**
     * 获取 Galleryfinal 缓存 所在目录
     * note:使用系统缓存文件夹,文件有被系统删除而无法找到的风险
     *
     * @return
     */
    public static File getGalleryfinalCacheDir() {
        File cache;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cache = AppContext.getApp().getExternalCacheDir();
        } else {
            cache = AppContext.getApp().getCacheDir();
        }
        File file = new File(cache, "Galleryfinal");
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 获取 缓存目录
     *
     * @return
     */
    public static File getCachePath() {
        String path = Environment.getExternalStorageDirectory()
                + "/"
                + AppContext.getApp().getResources().getString(R.string.app_name_en)
                + "/.nomedia/Cache/";
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 获取 视频库 所在目录的路径
     *
     * @return
     */
    public static File getVideoStorePath() {
        String path = Environment.getExternalStorageDirectory()
                + "/"
                + AppContext.getApp().getResources().getString(R.string.app_name_en)
                + "/.nomedia/VideoStore/";
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 获取 Music 所在目录的路径
     *
     * @return
     */
    public static File getMusicPath() {
        String path = Environment.getExternalStorageDirectory()
                + "/"
                + AppContext.getApp().getResources().getString(R.string.app_name_en)
                + "/.nomedia//Music/";
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 获取 图片 所在目录
     * 1.保存二维码生成的图片
     * 2.保存拍照成功后头像照片
     *
     * @return
     */
    public static File getImagePath() {
        String path = Environment.getExternalStorageDirectory()
                + "/"
                + AppContext.getApp().getResources().getString(R.string.app_name_en)
                + "/Image/";

        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }
}