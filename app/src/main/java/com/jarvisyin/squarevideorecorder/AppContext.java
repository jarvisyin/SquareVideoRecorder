package com.jarvisyin.squarevideorecorder;

import android.app.Application;

/**
 * Created by jarvisyin on 16/11/18.
 */
public class AppContext extends Application {

    private static AppContext mAppContext;

    public static AppContext getApp() {
        return mAppContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = this;
    }
}
