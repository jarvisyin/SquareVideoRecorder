package com.jarvisyin.recorder.Buz;

import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.jarvisyin.recorder.AppContext;
import com.jarvisyin.recorder.Common.Utils.FileSizeUtils;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;

/**
 * Created by yeyq on 16/8/5.
 */
public class JYImageLoader {

    private static JYImageLoader ourInstance = new JYImageLoader();

    private JYImageLoader() {
    }

    public static JYImageLoader getInstance() {
        return ourInstance;
    }

    public void init() {
        DisplayImageOptions uilOptions = new DisplayImageOptions
                .Builder()
                .cacheInMemory(true) // default:false
                .cacheOnDisk(true) // default:false
                .imageScaleType(ImageScaleType.NONE)
                .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                .displayer(new SimpleBitmapDisplayer()) // default
                .handler(new Handler()) // default
                .build();

        ImageLoaderConfiguration uilConfig = new ImageLoaderConfiguration
                .Builder(AppContext.getApp().getApplicationContext())
                .threadPoolSize(3).threadPriority(Thread.NORM_PRIORITY - 1)
                .tasksProcessingOrder(QueueProcessingType.FIFO).denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(10 * 1024 * 1024)) //建议内存设在5-10M,可以有比较好的表现
                .diskCacheSize(64 * 1024 * 1024)
                .diskCacheFileCount(100)
                .defaultDisplayImageOptions(uilOptions) // default
                .build();

        ImageLoader.getInstance().init(uilConfig);
    }

    public ImageLoader getImageLoader() {
        return ImageLoader.getInstance();
    }

    public void displayImage(String url, final ImageView imageView) {
        ImageAware imageAware = new ImageViewAware(imageView, false);
        ImageLoader.getInstance().displayImage(url, imageAware);
    }

    public void loadImage(String url, final ImageCallback callback) {
        ImageLoader.getInstance().loadImage(url,
                new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        Error error = new Error(failReason.toString());
                        callback.onImageLoadFailed(error);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        callback.onImageLoadSuccess(loadedImage);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        Error error = new Error("load cancel");
                        callback.onImageLoadFailed(error);
                    }
                });
    }

    public void clearDiskCache() {
        ImageLoader.getInstance().clearDiskCache();
    }

    /**
     *
     * @return 单位:MB
     */
    public long diskCacheSize() {
        File cacheDir = ImageLoader.getInstance().getDiskCache().getDirectory();

        double size = FileSizeUtils.getFileOrFilesSize(cacheDir.getPath(), FileSizeUtils.SIZETYPE_MB);

        return (long) (size+0.5f);
    }

    public interface ImageCallback {
        void onImageLoadSuccess(Bitmap bitmap);

        void onImageLoadFailed(Error error);
    }
}