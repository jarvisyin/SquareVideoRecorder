package com.jarvisyin.squarevideorecorder.Common.Utils;


import com.jarvisyin.squarevideorecorder.BuildConfig;

/**
 * Created by jarvisyin on 16/2/8.
 */
public class JLog {

    public static void v(String tag, String msg) {
        if(BuildConfig.DEBUG){
            android.util.Log.v(tag,msg);
        }
    }


    public static void d(String tag, String msg) {
        if(BuildConfig.DEBUG){
            android.util.Log.d(tag, msg);
        }
    }


    public static void i(String tag, String msg) {
        if(BuildConfig.DEBUG){
            android.util.Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
         if(BuildConfig.DEBUG){
             android.util.Log.w(tag,msg);
         }
    }


    public static void e(String tag, String msg) {
        if(BuildConfig.DEBUG) {
            android.util.Log.e(tag, msg);
        }
    }

}
