package com.jarvisyin.recorder;

import android.support.multidex.MultiDexApplication;
import com.jarvisyin.recorder.Buz.JYImageLoader;
import com.jarvisyin.recorder.Common.Component.Activity.BaseActivity;


/**
 * Created by jarvisyin on 16/2/23.
 */
public class AppContext extends MultiDexApplication {

    private static AppContext app;

    public static AppContext getApp() {
        return app;
    }


    private BaseActivity mCurrentActivity = null;

    public BaseActivity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(BaseActivity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.app = this;
        JYImageLoader.getInstance().init();
    }
}
